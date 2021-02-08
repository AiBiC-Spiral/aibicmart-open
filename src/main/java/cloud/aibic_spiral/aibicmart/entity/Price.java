package cloud.aibic_spiral.aibicmart.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 販売価格データ
 * @author t-kanda
 *
 */
public class Price {
	
	@JsonProperty("jancode")
	@Schema(description = "JANコード")
	private String jan;

	@JsonProperty
	@Schema(description = "年月日")
	private String date;

	@JsonProperty
	@Schema(description = "販売価格")
	private int price;

	public Price(String jan, LocalDate date, int price) {
		this.jan = jan;
		this.date = date.toString();
		this.price = price;
	}

	public int price() {
		return price;
	}

}
