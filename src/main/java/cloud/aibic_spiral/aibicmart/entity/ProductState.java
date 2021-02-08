package cloud.aibic_spiral.aibicmart.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public class ProductState {

	@JsonProperty
	@Schema(description = "消費期限")
	private String expiration;

	@JsonProperty
	@Schema(description = "製造（入荷）年月日")
	private String made;

	@JsonProperty
	@Schema(description = "在庫数")
	private int amount;

	public ProductState(LocalDate made, LocalDate expire, int amount) {
		this.made = made.toString();
		this.expiration = expire.toString();
		this.amount = amount;
	}
	
	public int amount(){
		return amount;
	}

	public int sold(int willBeSold) {
		return amount -= willBeSold;
	}

	public LocalDate expire() {
		return LocalDate.parse(expiration);
	}
}
