package cloud.aibic_spiral.aibicmart.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import cloud.aibic_spiral.aibicmart.calendar.AiBiCCalendar;
import cloud.aibic_spiral.aibicmart.entity.*;
import cloud.aibic_spiral.aibicmart.logutils.AiBiCLogger;
import cloud.aibic_spiral.aibicmart.pos.LocalPOSOneItem;
import cloud.aibic_spiral.aibicmart.pos.RealPOS;
import cloud.aibic_spiral.aibicmart.weather.WeatherForecast;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * 
 * それぞれのお店インスタンス。過剰在庫が眠る場所。<br>
 * この店舗は単一商品版。
 * 
 * @author t-kanda
 *
 */
public class ShopOneItem {

	private String uid;
	private int token;
	private String jan;

	/**
	 * 店舗シミュレーター内カレンダー
	 */
	private LocalDate date;
	private LocalDate since, until;

	/**
	 * 陳列棚。おそらく冷蔵。
	 */
	public LocalPOSOneItem stock;

	/*
	 * 商売の履歴。
	 */

	private Map<LocalDate, Order> orderHistory;
	public TObjectIntMap<LocalDate> salesHistory;
	public TObjectIntMap<LocalDate> costHistory;

	public ShopOneItem(String uid, int token, LocalDate since, LocalDate until, String jan) {
		this.since = since;
		this.until = until;
		// シミュレート前日の晩からスタート
		date = since.minusDays(1);
		this.uid = uid;
		this.token = token;
		this.jan = jan;
		stock = new LocalPOSOneItem(jan);
		orderHistory = new THashMap<>();
		salesHistory = new TObjectIntHashMap<>();
		costHistory = new TObjectIntHashMap<>();

		AiBiCLogger.info("Created : from {} to {}", uid, token, since.toString(), until.toString());

	}

	/**
	 * シミュレータ内の日付を取得
	 * 
	 * @return
	 */
	public Date getDateToday() {
		return AiBiCCalendar.get(date);
	}

	public Date getDateTomorrow() {
		return AiBiCCalendar.get(date.plusDays(1));
	}

	public String uid() {
		return uid;
	}

	public int token() {
		return token;
	}

	/**
	 * Result getResult(Date d): 日付d (<= 本日)の商品pの販売実績情報を返す
	 * 
	 * @param d
	 * @return
	 */
	public Result getResult(LocalDate d) {
		// 未来の日付
		if (!isDatePastOrToday(d)) {
			return null;
		}
		// シミュレーション開始前
		if (d.isBefore(since)) {
			return new Result(date, RealPOS.getVisitor(date), jan, 0, 0, RealPOS.demandQuantity(jan, date), 0, 0, 0);
		}
		return stock.result(d);
	}

	public Result getResult() {
		return getResult(date);
	}

	/**
	 * 商品pの現時点の在庫数品の状態を返す。 商品状態 (bb(p), made(p))の配列
	 * 
	 * @return
	 */
	public ProductStateSet getStock() {
		return stock.getProductState();
	}

	/**
	 * 日付dにおける来店者数を返す。
	 * 
	 * @param d
	 * @return
	 *         <li>今日までなら実測値</li>
	 *         <li>明後日以降は-1</li>
	 */
	public Visitor getVisitors(LocalDate d) {
		if (isDatePastOrToday(d)) {
			// 今日までなら実測値
			return new Visitor(d, RealPOS.getVisitor(d));
		} else {
			// 明後日以降は-1
			return new Visitor(d, -1);
		}
	}

	/**
	 * 商品pのdにおける仕入れ値を返す． 通常は商品ごとに固定？<br>
	 * （メモ：実データにないのでDBつくらないと？今回の課題では仕入れ値を考慮してなにかするシーンは少ないかもしれない）
	 * 
	 * @return
	 */
	public int getCostPrice() {
		return ShopManagerOneItem.getInstance().cost(jan);

	}

	/**
	 * 商品pのdにおける販売価格を返す． dが未来の場合は，予定販売価格を返す．<br>
	 * 決まってるのは明日まで
	 * 
	 * @param d
	 * @return
	 */
	public Price getRetailPrice(LocalDate d) {
		// 明後日以降は不明
		if (!isDateTomorrowOrBefore(d)) {
			return null;
		}
		int price = RealPOS.getRetailPrice(jan, d);
		if (price < 0) {
			return null;
		}
		return new Price(jan, d, price);
	}

	/**
	 * 商品pの当日の販売価格を返す．<br>
	 * 決まってるのは明日まで
	 * 
	 * @return
	 */
	public Price getRetailPrice() {
		return getRetailPrice(date);
	}

	/**
	 * 明日までの天気予報を返す
	 * 
	 * @param d
	 * @return
	 */
	public Weather getWeatherForecast(LocalDate d) {
		if (!isDateTomorrowOrBefore(d)) {
			return null;
		} else if (isDatePastOrToday(d)) {
			return WeatherForecast.result(d);
		} else {
			return WeatherForecast.forecast(d);
		}
	}

	public Weather getWeatherForecast() {
		return WeatherForecast.forecast(date);
	}

	/**
	 * 日付sinceからuntilの間の発注履歴を返す．
	 * 
	 * @param since
	 * @param until
	 * @return
	 */
	public List<Order> getOrderHistory(LocalDate since, LocalDate until) {
		if (until.isAfter(date)) {
			until = date;
		}
		List<Order> o = new ArrayList<>();
		for (LocalDate l = since; l.isBefore(until); l = l.plusDays(1)) {
			o.add(orderHistory.get(l));
		}

		return o;
	}

	/**
	 * 発注！
	 * 
	 * @param o
	 * @return
	 */
	public OrderStatus order(Order o) {

		// シミュレート期間を過ぎているので発注はエラー。
		// 情報取得APIを叩けるようにインスタンスは残っている状態
		// 最終日は売り上げ処理だけ。 date = untilは拒否。
		if (!date.isBefore(until)) {
			return null;
		}

		// 発注のときは日付はこちらで指定する
		o.setDate(date);
		OrderStatus os = new OrderStatus(date);

		// 発注に基づいて在庫を増やす

		OrderForProcuct op = o.orderForProduct(jan);
		if (op == null) {
			// この商品は発注指示に含まれていない
			os.noOrder(jan);
		} else if (op.amount() > 0) {
			// 発注指示あり
			int realAmount = stock.order(op.amount(), date);
			os.success(jan, realAmount);
		} else if (op.amount() == 0) {
			// 発注しない指示あり
			os.success(jan, 0);
		} else {
			return null;
		}


		orderHistory.put(date, o);

		// 発注完了。翌日の売り上げ処理へ
		date = date.plusDays(1);

		// 発注翌日の売り上げ処理
		if (!date.isBefore(since)) {
			int stockOpen = stock.stock();

			// 今日の売れる量を取得
			int demand = RealPOS.demandQuantity(jan, date);
			int price = getRetailPrice(date).price();

			// 在庫へらす
			int chanceloss = stock.saleAndGetChanceloss(jan, demand, price);
			int sold = demand - chanceloss;

			// 廃棄
			int expired = stock.expire(jan, date);
			int stockClose = stock.stock();

			int visitor = getVisitors(date).visitor();

			// 履歴書き出し
			Result result = new Result(date, visitor, jan, stockOpen, stockClose, demand, sold, expired, chanceloss);
			stock.record(result);

		}

		salesHistory.put(date, stock.sales());
		costHistory.put(date, stock.cost());

		// 最終日なら閉店処理
		if (date.equals(until)) {
			close();
		}

		return os;
	}

	/**
	 * 商品情報を取得
	 * 
	 * @return
	 */
	public Map<String, String> getProductInfo() {
		Map<String, String> posData = RealPOS.getProductInfo(jan);
		posData.put("costPrice", Integer.toString(getCostPrice()));
		posData.put("expire", Integer.toString(getExpire()));
		return posData;
	}

	private int getExpire() {
		return ShopManagerOneItem.getInstance().expire(jan);
	}

	/**
	 * 報告用サマリ
	 * 
	 * @param p
	 * @return
	 */
	public SummaryReport report() {
		int finalStock = stock.stock();
		int totalOrder = totalOrder();
		int totalSold = stock.totalSold();
		int totalExpired = stock.totalExpired();
		int totalChanceloss = stock.totalChanceloss();
		int totalSales = salesHistory.get(date);
		int totalCosts = costHistory.get(date);
		return new SummaryReport(uid, jan, since, until, date, finalStock, totalOrder, totalSold, totalExpired,
				totalChanceloss, totalSales, totalCosts);
	}

	public Map<LocalDate, OrderForProcuct> getTotalOrderHistoryByJAN() {
		return orderHistory.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().orderForProduct(jan)));
	}

	private int totalOrder() {
		return orderHistory.values().stream().map(o -> o.orderForProduct(jan)).mapToInt(OrderForProcuct::amount).sum();
	}

	/**
	 * ログTSV生成
	 * 
	 * @param p
	 * @return
	 */
	public String logCreation() {
		if (stock.getProductState() == null) {
			return null;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println(headerLine());
		// シミュレート開始前日晩、発注のみのログ
		LocalDate b = since.minusDays(1);
		Order o = orderHistory.get(since.minusDays(1));
		int amountb = (o == null) ? 0 : o.orderForProduct(jan).amount();
		int salesb = (salesHistory.containsKey(b)) ? salesHistory.get(b) : 0;
		int costb = (costHistory.containsKey(b)) ? costHistory.get(b) : 0;
		pw.println(String.join("\t", b.toString(), "0", "0", "0", "0", "0", "0", "0", Integer.toString(amountb),
				Integer.toString(salesb), Integer.toString(costb)));

		// 発注済みの日のログ
		for (LocalDate l = since; l.isBefore(date); l = l.plusDays(1)) {
			Result r = stock.result(l);
			OrderForProcuct op = orderHistory.get(l).orderForProduct(jan);
			int amount = (op == null) ? 0 : op.amount();
			int sales = salesHistory.get(l);
			int cost = costHistory.get(l);
			pw.println(logLine(r, amount, sales, cost));
		}

		// 売り上げ処理がおわって発注をしていない今日のログ
		if (!date.isBefore(since)) {
			Result r = stock.result(date);
			int sales = salesHistory.get(date);
			int cost = costHistory.get(date);
			pw.println(logLine(r, 0, sales, cost));
		}
		pw.close();
		return sw.toString();
	}

	/**
	 * ログTSVヘッダ
	 * 
	 * @return
	 */
	private String headerLine() {
		return String.join("\t", "date", "visitor", "stockOpen", "stockClose", "demand", "sold", "expired",
				"chanceloss", "orderAmount", "sales", "cost");
	}

	/**
	 * ログTSV１行
	 * 
	 * @return
	 */
	private String logLine(Result r, int amount, int sales, int cost) {
		return String.join("\t", r.date(), Integer.toString(r.visitor()), Integer.toString(r.stockOpen()),
				Integer.toString(r.stockClose()), Integer.toString(r.demand()), Integer.toString(r.sold()),
				Integer.toString(r.expired()), Integer.toString(r.chanceloss()), Integer.toString(amount),
				Integer.toString(sales), Integer.toString(cost));
	}

	/*
	 * utils
	 */

	/**
	 * シミュレート期間満了。レポート生成？
	 */
	public void close() {
		SummaryReport sr = report();

		if (sr.hasContent()) {
			AiBiCLogger.info("Report: " + sr.reportContent(), uid, token);
		}

		AiBiCLogger.info("Simulation Completed!", uid, token);
	}

	/**
	 * その日付は昨日までか
	 * 
	 * @param d
	 * @return
	 */
	public boolean isDatePast(LocalDate d) {
		return d.isBefore(date);
	}

	/**
	 * その日付は今日までか
	 * 
	 * @param d
	 * @return
	 */
	public boolean isDatePastOrToday(LocalDate d) {
		return !d.isAfter(date);
	}

	/**
	 * その日付は明日までか
	 * 
	 * @param d
	 * @return
	 */
	public boolean isDateTomorrowOrBefore(LocalDate d) {
		return d.equals(date.plusDays(1)) || isDatePastOrToday(d);
	}

	public List<Result> allResult() {
		return stock.allResult();
	}

	public LocalDate getDate(int days) {
		return date.plusDays(days);
	}
}
