package cloud.aibic_spiral.aibicmart.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public class Weather {

	@JsonProperty
	@Schema(description = "年月日")
	private String date;

	@JsonProperty
	@Schema(description = "天気概要（雨、晴…など）")
	private String overview;

	@JsonProperty
	@Schema(description = "降雨量")
	private String rainfall;

	@JsonProperty
	@Schema(description = "平均気温")
	private String averageTemp;

	@JsonProperty
	@Schema(description = "最高気温")
	private String maxTemp;

	@JsonProperty
	@Schema(description = "最低気温")
	private String minTemp;

	@JsonProperty
	@Schema(description = "日照時間")
	private String solarRadiation;

	public Weather(LocalDate date, String overview, String raifall, String averageTemp, String maxTemp, String minTemp,
			String solarRadiation) {
		this.date = date.toString();
		this.overview = overview;
		this.rainfall = raifall;
		this.averageTemp = averageTemp;
		this.maxTemp = maxTemp;
		this.minTemp = minTemp;
		this.solarRadiation = solarRadiation;
	}

	public static Weather unknown(LocalDate date) {
		return new Weather(date, "不明","不明" ,"不明" ,"不明", "不明", "不明");
	}

}
