package cloud.aibic_spiral.aibicmart.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author t-kanda
 *
 */
public class SummaryReport {

	@JsonProperty
	private String uid;

	@JsonProperty("jancode")
	private String jan;

	@JsonProperty
	private String since;

	@JsonProperty
	private String until;

	@JsonProperty("report_date")
	private String date;

	@JsonProperty("stock_last")
	private int finalStock;

	@JsonProperty("total_order")
	private int totalOrder;

	@JsonProperty("total_sold")
	private int totalSold;

	@JsonProperty("total_expired")
	private int totalExpired;

	@JsonProperty("total_chanceloss")
	private int totalChanceloss;

	@JsonProperty("total_sales")
	private int totalSales;

	@JsonProperty("total_cost")
	private int totalCosts;

	@JsonProperty("total_earnings")
	private int totalEarning;

	public SummaryReport(String uid, String jan, LocalDate since, LocalDate until, LocalDate date, int finalStock,
			int totalOrder, int totalSold, int totalExpired, int totalChanceloss, int totalSales, int totalCosts) {
		this.uid = uid;
		this.jan = jan;
		this.since = since.toString();
		this.until = until.toString();
		this.date = date.toString();
		this.finalStock = finalStock;
		this.totalOrder = totalOrder;
		this.totalSold = totalSold;
		this.totalExpired = totalExpired;
		this.totalChanceloss = totalChanceloss;
		this.totalSales = totalSales;
		this.totalCosts = totalCosts;
		totalEarning = totalSales - totalCosts;
	}

	public String reportContent() {
		return String.join("\t", uid, jan, since, until, date, Integer.toString(finalStock),
				Integer.toString(totalOrder), Integer.toString(totalSold), Integer.toString(totalExpired),
				Integer.toString(totalChanceloss), Integer.toString(totalSales), Integer.toString(totalCosts),
				Integer.toString(totalEarning));
	}

	public String reportHeader() {
		return String.join("\t", "uid", "jan", "since", "until", "report_date", "stock_last", "total_order",
				"total_sold", "total_expired", "total_chanceloss", "total_sales", "total_cost", "total_earnings");
	}

	public String report() {
		return String.join("\r\n", reportHeader(), reportContent());
	}

	public boolean hasContent() {
		return totalOrder != 0;
	}

}
