package br.com.litecode.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@NamedNativeQueries({
	@NamedNativeQuery(
		name = "monthlyBilling",
		query = "select date_format(issue_date, '%Y-%m-01') as date, 0 as groupColumn, sum(gross_amount) as aggregateColumn " +
				"from invoice " +
				"where invoice_type = 'I' and status <> 3 and date_format(issue_date, '%Y-%m-01') >= date_format(:startDate, '%Y-%m-01') " +
				"group by date_format(issue_date, '%Y-%m-01') order by date",
		resultSetMapping = "billingReportMapping"),
	@NamedNativeQuery(
		name = "monthlyBillingByService",
		query = "select date_format(issue_date, '%Y-%m-01') as date, concat(s.code, '. ', s.name) as groupColumn, sum(si.amount) as aggregateColumn " +
				"from invoice i " +
				"join invoice_service si on si.invoice_id = i.invoice_id " +
				"join service s on s.service_id = si.service_id " +
				"where invoice_type = 'I' and  status <> 3 and date_format(issue_date, '%Y-%m-01') >= date_format(:startDate, '%Y-%m-01') " +
				"group by s.name, date_format(issue_date, '%Y-%m-01') order by date, s.code",
		resultSetMapping = "billingReportMapping"),
})

@SqlResultSetMapping( 
	name="billingReportMapping",
	entities = {
	    @EntityResult(
	        entityClass = ReportRecord.class,
	        fields = {
	        	@FieldResult(name = "reportId.date", column = "date"),
	            @FieldResult(name = "reportId.groupColumn", column = "groupColumn"),
	            @FieldResult(name = "aggregateColumn", column = "aggregateColumn")
	        }
	    )
	}
)

/*
- sessions by month: bar
- sessions by chamber: pie
-
 */


@Entity
public class ReportRecord {

	@Id
	@EmbeddedId
	private ReportId reportId;
	private BigDecimal aggregateColumn;
	
	public Date getDate() {
		return reportId.getDate();
	}
	
	public String getGroupColumn() {
		return reportId.getGroupColumn();
	}

	public BigDecimal getAggregateColumn() {
		return aggregateColumn;
	}
}

@Embeddable
class ReportId implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String groupColumn;
	private Date date;

	public String getGroupColumn() {
		return groupColumn;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((groupColumn == null) ? 0 : groupColumn.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (!(obj instanceof ReportId)) {
			return false;
		}
		ReportId other = (ReportId) obj;
		return groupColumn.equals(other.groupColumn) && date.equals(other.date);
	}
}