package br.com.litecode.service;

import br.com.litecode.domain.model.Chamber;
import br.com.litecode.domain.model.Patient.PatientStats;
import br.com.litecode.domain.model.PatientSession;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.repository.ChamberRepository;
import br.com.litecode.domain.repository.PatientRepository;
import br.com.litecode.domain.repository.SessionRepository;
import br.com.litecode.util.MessageUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PdfService {
	@Autowired
	private ChamberRepository chamberRepository;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private PatientRepository patientRepository;

	public byte[] generateSessionReport(LocalDate sessionDate) throws DocumentException {
		Iterable<Chamber> chambers = chamberRepository.findAll();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Document document = new Document(PageSize.A4.rotate());
		PdfWriter.getInstance(document, outputStream);
		document.open();
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
				new TableCell("label.name", 8, Element.ALIGN_LEFT),
				new TableCell("label.patientSessionPresent", 8, Element.ALIGN_CENTER),
				new TableCell("label.healthInsurance", 8, Element.ALIGN_LEFT),
				new TableCell("label.patientRecord", 8, Element.ALIGN_CENTER),
				new TableCell("label.folderNumber", 8, Element.ALIGN_CENTER),
				new TableCell("label.attendance", 8, Element.ALIGN_CENTER)
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
		table.setWidths(new int[] { 1, 4, 1, 1, 1 });

		for (TableCell header : sessionHeaders) {
			if (header.getLabel().equals(patientHeader)) {
				table.addCell(createPatientHeaderCell());
			} else {
				table.addCell(createCell(header));
			}
		}

		for (Session session : sessions) {
			table.addCell(createCell(new TableCell(session.getSessionId()::toString)));
			table.addCell(createPatientsTable(session, sessionDate));
			table.addCell(createCell(new TableCell(() -> session.getScheduledTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")))));
			table.addCell(createCell(new TableCell(() -> session.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")))));
			table.addCell(createCell(new TableCell(() -> session.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")))));
		}

		return table;
	}

	private PdfPCell createPatientHeaderCell() throws DocumentException {
		TableCell[] patientHeaders = getPatientHeaders();

		PdfPTable table = new PdfPTable(patientHeaders.length);
		PdfPCell headerCell = createCell(new TableCell("label.patients", 10, Element.ALIGN_CENTER));
		headerCell.setColspan(patientHeaders.length);
		table.addCell(headerCell);
		table.setWidths(new int[] { 3, 1, 1, 1, 1, 1 });

		for (TableCell patientHeader : patientHeaders) {
			table.addCell(createCell(patientHeader));
		}

		return new PdfPCell(table);
	}

	private PdfPCell createPatientsTable(Session session, LocalDate sessionDate) throws DocumentException {
		TableCell[] patientHeaders = getPatientHeaders();
		PdfPTable table = new PdfPTable(patientHeaders.length);
		table.setWidths(new int[] { 3, 1, 1, 1, 1, 1 });

		Map<Integer, PatientStats> patientStats = patientRepository.findPatienStats(session.getSessionId(), sessionDate.plusDays(1).atStartOfDay()).stream().collect(Collectors.toMap(PatientStats::getPatientId, Function.identity()));

		for (PatientSession patientSession : session.getPatientSessions()) {
			table.addCell(createCell(new TableCell(patientSession.getPatient()::getName, 8, Element.ALIGN_LEFT)));
			table.addCell(createCell(new TableCell(() -> patientSession.isAbsent() ? "" : "x", 8, Element.ALIGN_CENTER)));
			table.addCell(createCell(new TableCell(patientSession.getPatient()::getHealthInsurance, 8, Element.ALIGN_LEFT)));
			table.addCell(createCell(new TableCell(patientSession.getPatient()::getPatientRecord, 8, Element.ALIGN_CENTER)));
			table.addCell(createCell(new TableCell(patientSession.getPatient()::getFolderNumber, 8, Element.ALIGN_CENTER)));
			table.addCell(createCell(new TableCell(() -> String.valueOf(patientStats.get(patientSession.getPatient().getPatientId()).getCompletedSessions()), 8, Element.ALIGN_CENTER)));
		}

		PdfPCell cell = new PdfPCell(table);
		cell.setPadding(0);

		return cell;
	}

	private PdfPCell createCell(TableCell tableCell) {
		String text = tableCell.getValueSupplier() != null ? tableCell.getValue() : tableCell.getLabel();
		PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.HELVETICA, tableCell.getFontSize(), Font.NORMAL, tableCell.getColor())));
		cell.setBackgroundColor(tableCell.getBackgroundColor());
		cell.setHorizontalAlignment(tableCell.getTextAlign());
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPadding(4);
		return cell;
	}

	@Getter
	private static class TableCell {
		private String header;
		private Supplier<String> valueSupplier;
		private int fontSize = 10;
		private int textAlign = Element.ALIGN_CENTER;
		private BaseColor color = BaseColor.BLACK;
		private BaseColor backgroundColor = BaseColor.WHITE;

		public TableCell(String header) {
			this.header = header;
			this.backgroundColor = new BaseColor(230, 230, 230);
		}

		public TableCell(String header, int fontSize, int textAlign) {
			this(header);
			this.fontSize = fontSize;
			this.textAlign = textAlign;
		}

		public TableCell(Supplier<String> valueSupplier) {
			this.valueSupplier = valueSupplier;
		}

		public TableCell(Supplier<String> valueSupplier, int fontSize, int textAlign) {
			this.valueSupplier = valueSupplier;
			this.fontSize = fontSize;
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