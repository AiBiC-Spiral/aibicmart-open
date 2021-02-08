package cloud.aibic_spiral.aibicmart.entity;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 店舗のカレンダー。格納する項目は通常のカレンダーに加えて閉店日かどうかの情報。
 * @author t-kanda
 *
 */
public class Date {

	@JsonProperty
	@Schema(description = "年月日")
	private String date;

	@JsonProperty
	@Schema(description = "年")
	private int year;

	@JsonProperty
	@Schema(description = "月")
	private int month;

	@JsonProperty
	@Schema(description = "日")
	private int day;

	@JsonProperty
	@Schema(description = "曜日")
	private String dayOfWeek;

	@JsonProperty
	@Schema(description = "祝日かどうか")
	private int isHoliday;

	@JsonProperty
	@Schema(description = "祝日名")
	private String holidayName;

	@JsonProperty
	@Schema(description = "週番号")
	private int weekNumber;

	@JsonProperty
	@Schema(description = "店休日かどうか")
	private int closed;

	public Date(LocalDate d, String isHoliday, String holidayName, boolean closed) {
		this.date = d.toString();
		this.year = d.getYear();
		this.month = d.getMonthValue();
		this.day = d.getDayOfMonth();
		this.dayOfWeek = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
		this.isHoliday =  Integer.parseInt(isHoliday);
		this.holidayName = holidayName;
		this.weekNumber = d.get(WeekFields.SUNDAY_START.weekOfYear());
		this.closed = closed ? 1 : 0;
	}


	public static Date unknown(LocalDate d) {
		return new Date(d, "2", "不明", true);
	}
}
