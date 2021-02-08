package cloud.aibic_spiral.aibicmart.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 商品ごとの発注情報。JANコードと数量。
 * @author t-kanda
 *
 */
public class OrderForProcuct {

	@JsonProperty("jancode")
	@Schema(description = "JANコード")
	private String jan;
	
	@JsonProperty
	@Schema(description = "発注数")
	private int amount;
		
	public String jan(){
		return jan;
	}
	
	public int amount(){
		return amount;
	}
	
}
