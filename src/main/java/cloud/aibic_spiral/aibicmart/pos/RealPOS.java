package cloud.aibic_spiral.aibicmart.pos;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.IntStream;

import cloud.aibic_spiral.aibicmart.controller.ShopConfig;
import cloud.aibic_spiral.aibicmart.controller.ShopManagerOneItem;
import gnu.trove.map.hash.THashMap;

/**
 * 店舗の実データを読みに行くためのクラス。 シミュレータの内部状態によらないデータの担当。
 * 
 * @author t-kanda
 *
 */
public class RealPOS {

	/**
	 * 来客数取得
	 * 
	 * @param date
	 * @return
	 */
	public static int getVisitor(LocalDate date) {
		try {
			for (String line : Files.readAllLines(ShopConfig.getResource("customer.csv"))) {
				String[] val = line.split(",");
				if (val[0].contains("date")) {
					continue;
				}

				if (LocalDate.parse(val[0]).isEqual(date)) {
					return Integer.parseInt(val[2]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 販売価格（その日の平均）取得
	 * 
	 * @param jan
	 * @param date
	 * @return
	 */
	public static int getRetailPrice(String jan, LocalDate date) {
		if (!Files.exists(getPathByJAN(jan))) {
			return -11;
		}
		try {
			for (String line : Files.readAllLines(getPathByJAN(jan))) {
				String[] val = line.split(",");
				if (val[0].contains("date")) {
					continue;
				}

				// 消費期限による値引きなど一日に複数通りの単価が発生する場合は考慮していない
				if (LocalDate.parse(val[0]).isEqual(date)) {
					if ((val[3]).equals("0")) {
						return ShopManagerOneItem.getInstance().defaultPrice(jan);
					} else {
						return Integer.parseInt(val[4]) / Integer.parseInt(val[3]);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 指定日の販売数取得
	 * 
	 * @param jan
	 * @param date
	 * @return
	 */
	public static int demandQuantity(String jan, LocalDate date) {
		try {
			for (String line : Files.readAllLines(getPathByJAN(jan))) {
				String[] val = line.split(",");
				if (val[0].contains("date")) {
					// 1行目スキップ
					continue;
				}
				if (LocalDate.parse(val[0]).isEqual(date)) {
					return Integer.parseInt(val[3]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * csvの1,2行目から製品情報を取得して返す。
	 * 
	 * @param jan
	 * @return
	 */
	public static Map<String, String> getProductInfo(String jan) {
		try (BufferedReader br = Files.newBufferedReader(getPathByJAN(jan))) {
			// date,shopCode,JANCode,quantity,sales,productName,makerCode,makerName,JICFSCode,JICFSName
			String[] header = br.readLine().split(",");
			String[] data = br.readLine().split(",");
			Map<String, String> info = new THashMap<>();
			IntStream.of(2, 5, 6, 7, 8, 9).forEach(i -> info.put(header[i], data[i]));
			return info;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * JANコードからcsvを読み込み
	 * 
	 * @param jan
	 * @return
	 */
	public static Path getPathByJAN(String jan) {
		return ShopConfig.getResource(jan + ".csv");
	}

}
