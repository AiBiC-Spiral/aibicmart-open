package cloud.aibic_spiral.aibicmart.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 商品ごとの発注結果
 * 
 * @author t-kanda
 *
 */
public class OrderStatusForProduct {

	@JsonProperty("jancode")
	@Schema(description = "JANコード")
	private String jan;

	@JsonProperty
	@Schema(description = "発注結果")
	private String status;

	@JsonProperty
	@Schema(description = "発注数")
	private int amount;

	public OrderStatusForProduct(String jan, String status, int amount) {
		this.jan = jan;
		this.status = status;
		this.amount = amount;
	}

}
