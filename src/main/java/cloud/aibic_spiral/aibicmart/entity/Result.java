package cloud.aibic_spiral.aibicmart.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 日ごとのシミュレーション結果
 * 
 * @author t-kanda
 *
 */
public class Result {

	@JsonProperty
	@Schema(description = "年月日")
	protected String date;

	@JsonProperty
	@Schema(description = "来客数")
	protected int visitor;

	@JsonProperty("jancode")
	@Schema(description = "JANコード")
	protected String jan;

	@JsonProperty("stock_open")
	@Schema(description = "在庫数（その日の初期値）")
	protected int stockOpen;

	@JsonProperty("stock_close")
	@Schema(description = "在庫数（その日の終了時） = stock_start - sold - expired")
	protected int stockClose;

	@JsonProperty
	@Schema(description = "需要数（売れるはずだった数）")
	protected int demand;

	@JsonProperty
	@Schema(description = "販売数（実際に売れた数）")
	protected int sold;

	@JsonProperty
	@Schema(description = "廃棄数")
	protected int expired;

	@JsonProperty
	@Schema(description = "チャンスロス")
	protected int chanceloss;

	public Result(LocalDate date, int visitor, String jan, int stockOpen, int stockClose, int demand, int sold,
			int expired, int chanceloss) {
		this.date = date.toString();
		this.visitor = visitor;
		this.jan = jan;
		this.stockOpen = stockOpen;
		this.demand = demand;
		this.sold = sold;
		this.expired = expired;
		this.stockClose = stockClose;
		this.chanceloss = chanceloss;
	}

	public static Result none(String jan, LocalDate date) {
		return new Result(date, 0, jan, 0, 0, 0, 0, 0, 0);
	}

	public String date() {
		return date;
	}

	public int stockOpen() {
		return stockOpen;
	}

	public int stockClose() {
		return stockClose;
	}

	public int demand() {
		return demand;
	}

	public int sold() {
		return sold;
	}

	public int expired() {
		return expired;
	}

	public int chanceloss() {
		return chanceloss;
	}

	public int visitor() {
		return visitor;
	}
}
