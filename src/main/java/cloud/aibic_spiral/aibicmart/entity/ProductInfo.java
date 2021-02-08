package cloud.aibic_spiral.aibicmart.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductInfo {

	@JsonProperty("JANCode")
	private String jan;

	@JsonProperty
	private int cost;

	@JsonProperty("default_price")
	private int defaultPrice;

	@JsonProperty
	private int expire;

	public ProductInfo(String jan) {
		this.jan = jan;
	}

	public ProductInfo() {

	}

	public String jan() {
		return jan;
	}

	public int expire() {
		return expire;
	}

	public int cost() {
		return cost;
	}

	public int defaultPrice() {
		return defaultPrice;
	}

}
