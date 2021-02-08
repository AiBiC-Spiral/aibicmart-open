package cloud.aibic_spiral.aibicmart.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 来客数。千客万来。（現段階では単体で返すAPIは未実装）
 * @author t-kanda
 *
 */
public class Visitor {

	@JsonProperty
	@Schema(description = "年月日")
	private String date;
	
	@JsonProperty
	@Schema(description = "来客数")
	private int visitor;

	public Visitor(LocalDate date, int visitor) {
		this.date = date.toString();
		this.visitor = visitor;
	}

	public int visitor() {
		return visitor;
	}

}
