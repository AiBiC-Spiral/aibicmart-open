package cloud.aibic_spiral.aibicmart.weather;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

import cloud.aibic_spiral.aibicmart.controller.ShopConfig;
import cloud.aibic_spiral.aibicmart.entity.Weather;

/**
 * 天気データ読み出し用クラス
 * 
 * @author t-kanda
 *
 */
public class WeatherForecast {

	public static Weather weather(LocalDate date, boolean forecast) {
		try {
			for (String line : Files.readAllLines(ShopConfig.getResource("weather.csv"))) {
				String[] val = line.split(",");
				if (val[0].contains("date")) {
					continue;
				}
				if (LocalDate.parse(val[0]).isEqual(date)) {
					if (forecast) {
						return new Weather(date, val[1], s2f2i2s(val[2]), s2f2i2s(val[3]), s2f2i2s(val[4]),
								s2f2i2s(val[5]), s2f2i2s(val[6]));
					} else {
						return new Weather(date, val[1], val[2], val[3], val[4], val[5], val[6]);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Weather.unknown(date);
	}

	/**
	 * 天候データを返す
	 * 
	 * @param date 日付
	 * @return 天候データ（実測値）
	 */
	public static Weather result(LocalDate date) {
		return weather(date, false);
	}

	/**
	 * 天候データを返す
	 * 
	 * @param date 日付
	 * @return 天候データ（予報値）
	 */
	public static Weather forecast(LocalDate date) {
		return weather(date, true);
	}

	/**
	 * 予報用：気温データを丸める
	 * 
	 * @param val
	 * @return
	 */
	private static String s2f2i2s(String val) {
		return Integer.toString((int) Float.parseFloat(val));
	}

}
