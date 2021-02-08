package cloud.aibic_spiral.aibicmart.calendar;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Arrays;
// import java.util.Collections;
import java.util.List;

import cloud.aibic_spiral.aibicmart.controller.ShopConfig;
import cloud.aibic_spiral.aibicmart.entity.Date;

/**
 * カレンダーデータ読み出し用クラス<br>
 * フォーマットはdate,year,month,day,dayOfWeek,isHoliday,holidayName,weekNumberで学生に配布するものと同じ項目。<br>
 * 閉店日については開示しない＆将来の閉店日は学生に直接は伝えないので別リソース。
 * 
 * @author t-kanda
 *
 */
public class AiBiCCalendar {

	private static List<String> closed = Arrays.asList("2000-12-31", "2001-01-01");

//	private static List<String> closed() {
//		try {
//			return Files.lines(ShopConfig.getResource("closed.txt")).collect(Collectors.toList());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return Collections.emptyList();
//	}

	public static Date get(LocalDate date) {

		try {
			for (String line : Files.readAllLines(ShopConfig.getResource("calendar.csv"))) {
				String[] val = line.split(",");
				if (val[0].contains("date")) {
					continue;
				}
				if (LocalDate.parse(val[0]).isEqual(date)) {
					return new Date(date, val[5], val[6], closed.contains(val[0]));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Date.unknown(date);
	}

}
