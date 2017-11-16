package br.com.litecode.service;

import br.com.litecode.domain.model.Chamber;
import br.com.litecode.domain.model.Patient.PatientStats;
import br.com.litecode.domain.model.PatientSession;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.repository.ChamberRepository;
import br.com.litecode.domain.repository.PatientRepository;
import br.com.litecode.domain.repository.SessionRepository;
import br.com.litecode.util.MessageUtil;
import com.google.common.io.ByteStreams;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SessionReportService {
	@Autowired
	private ChamberRepository chamberRepository;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private PatientRepository patientRepository;

	public byte[] generateSessionReport(LocalDate sessionDate) throws DocumentException, IOException {
		Iterable<Chamber> chambers = chamberRepository.findAll();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Document document = new Document(PageSize.A4.rotate());
		PdfWriter.getInstance(document, outputStream);
		document.open();

		Image image = Image.getInstance(ByteStreams.toByteArray(Faces.getResourceAsStream("/resources/images/logo-large.png")));
		image.scalePercent(50, 50);
		image.setAbsolutePosition(document.getPageSize().getWidth() - 140, document.getPageSize().getHeight() - 80);
		document.add(image);

		document.add(new Paragraph(MessageUtil.getMessage("title.appTitle"), new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK)));
		document.add(new Paragraph(sessionDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' YYYY")), new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK)));
		document.add(Chunk.NEWLINE);

		for (Chamber chamber : chambers) {
			PdfPTable table = createSessionsTable(sessionRepository.findSessionsByChamberAndDate(chamber.getChamberId(), sessionDate), sessionDate);

			if (table == null) {
				continue;
			}

			document.add(new Chunk(chamber.getName(), new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY)));
			document.add(Chunk.NEWLINE);
			document.add(table);
			document.add(Chunk.NEWLINE);
		}

		document.close();

		return outputStream.toByteArray();
	}

	private TableCell[] getSessionHeaders() {
		TableCell[] headers = {
				new TableCell("label.session"),
				new TableCell("label.patients"),
				new TableCell("label.time"),
				new TableCell("label.startTime"),
				new TableCell("label.endTime")
		};

		return headers;
	}

	private TableCell[] getPatientHeaders() {
		TableCell[] headers = {
				new TableCell("label.patient", Element.ALIGN_LEFT),
				new TableCell("label.patientSessionPresent", Element.ALIGN_CENTER),
				new TableCell("label.healthInsurance", Element.ALIGN_LEFT),
				new TableCell("label.patientRecord", Element.ALIGN_CENTER),
				new TableCell("label.folderNumber", Element.ALIGN_CENTER),
				new TableCell("label.attendance", Element.ALIGN_CENTER)
		};

		return headers;
	}

	private PdfPTable createSessionsTable(List<Session> sessions, LocalDate sessionDate) throws DocumentException {
		if (sessions.isEmpty()) {
			return null;
		}

		TableCell[] sessionHeaders = getSessionHeaders();
		String patientHeader = MessageUtil.getMessage("label.patients");

		PdfPTable table = new PdfPTable(sessionHeaders.length);
		table.setWidthPercentage(100);
		table.setWidths(new int[] { 1, 6, 1, 1, 1 });

		for (TableCell header : sessionHeaders) {
			if (header.getLabel().equals(patientHeader)) {
				table.addCell(createPatientHeaderCell());
			} else {
				table.addCell(createCell(header));
			}
		}

		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

		int rowIndex = 0;
		for (Session session : sessions) {
			table.addCell(createCell(new TableCell(rowIndex, session.getSessionId()::toString)));
			table.addCell(createPatientsTable(rowIndex, session, sessionDate));
			table.addCell(createCell(new TableCell(rowIndex, () -> session.getScheduledTime().format(timeFormatter))));
			table.addCell(createCell(new TableCell(rowIndex, () -> session.getStartTime().format(timeFormatter))));
			table.addCell(createCell(new TableCell(rowIndex, () -> session.getEndTime().format(timeFormatter))));
			rowIndex++;
		}

		return table;
	}

	private PdfPCell createPatientHeaderCell() throws DocumentException {
		TableCell[] patientHeaders = getPatientHeaders();

		PdfPTable table = new PdfPTable(patientHeaders.length);
		table.setWidths(new int[] { 3, 1, 1, 1, 1, 1 });

		for (TableCell patientHeader : patientHeaders) {
			table.addCell(createCell(patientHeader));
		}

		return new PdfPCell(table);
	}

	private PdfPCell createPatientsTable(int rowIndex, Session session, LocalDate sessionDate) throws DocumentException {
		TableCell[] patientHeaders = getPatientHeaders();
		PdfPTable table = new PdfPTable(patientHeaders.length);
		table.setWidths(new int[] { 3, 1, 1, 1, 1, 1 });

		Map<Integer, PatientStats> patientStats = patientRepository.findPatienStats(session.getSessionId(), sessionDate.plusDays(1).atStartOfDay()).stream().collect(Collectors.toMap(PatientStats::getPatientId, Function.identity()));

		for (PatientSession patientSession : session.getPatientSessions()) {
			TableCell patientName = new TableCell(rowIndex, patientSession.getPatient()::getName, Element.ALIGN_LEFT);
			if (patientSession.isAbsent()) {
				patientName.setColor(BaseColor.DARK_GRAY);
			}

			table.addCell(createCell(patientName));
			table.addCell(createCell(new TableCell(rowIndex, () -> patientSession.isAbsent() ? "" : "x", Element.ALIGN_CENTER)));
			table.addCell(createCell(new TableCell(rowIndex, patientSession.getPatient()::getHealthInsurance, Element.ALIGN_LEFT)));
			table.addCell(createCell(new TableCell(rowIndex, patientSession.getPatient()::getPatientRecord, Element.ALIGN_CENTER)));
			table.addCell(createCell(new TableCell(rowIndex, patientSession.getPatient()::getFolderNumber, Element.ALIGN_CENTER)));

			int completedSessions = patientStats.get(patientSession.getPatient().getPatientId()).getCompletedSessions();
			table.addCell(createCell(new TableCell(rowIndex, () -> String.valueOf(completedSessions), Element.ALIGN_CENTER)));
		}

		PdfPCell cell = new PdfPCell(table);
		cell.setPadding(0);

		return cell;
	}

	private PdfPCell createCell(TableCell tableCell) {
		String text = tableCell.getValueSupplier() != null ? tableCell.getValue() : tableCell.getLabel();
		PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.HELVETICA, tableCell.getFontSize(), tableCell.getFontStyle(), tableCell.getColor())));
		cell.setBackgroundColor(tableCell.getBackgroundColor());
		cell.setHorizontalAlignment(tableCell.getTextAlign());
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPadding(4);
		return cell;
	}

	@Getter
	@Setter
	private static class TableCell {
		private String header;
		private Supplier<String> valueSupplier;
		private int fontSize = 8;
		private int fontStyle = Font.NORMAL;
		private int textAlign = Element.ALIGN_CENTER;
		private BaseColor color = BaseColor.BLACK;
		private BaseColor backgroundColor = BaseColor.WHITE;

		public TableCell(String header) {
			this.header = header;
			fontStyle = Font.BOLD;
			backgroundColor = new BaseColor(220, 220, 220);
		}

		public TableCell(String header, int textAlign) {
			this(header);
			this.textAlign = textAlign;
		}

		public TableCell(int rowIndex, Supplier<String> valueSupplier) {
			this.valueSupplier = valueSupplier;

			if (rowIndex % 2 > 0) {
				backgroundColor = new BaseColor(245, 245, 245);
			}
		}

		public TableCell(int rowIndex, Supplier<String> valueSupplier, int textAlign) {
			this(rowIndex, valueSupplier);
			this.textAlign = textAlign;
		}

		public String getValue() {
			return valueSupplier.get();
		}

		public String getLabel() {
			return MessageUtil.getMessage(header);
		}
	}
}