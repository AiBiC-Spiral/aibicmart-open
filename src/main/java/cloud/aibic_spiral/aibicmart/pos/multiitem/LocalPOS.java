package cloud.aibic_spiral.aibicmart.pos.multiitem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import cloud.aibic_spiral.aibicmart.controller.multiitem.ShopManager;
import cloud.aibic_spiral.aibicmart.entity.ProductState;
import cloud.aibic_spiral.aibicmart.entity.ProductStateSet;
import cloud.aibic_spiral.aibicmart.entity.Result;
import gnu.trove.map.hash.THashMap;

/**
 * シミュレート中の在庫売り上げ管理
 * 
 * @author t-kanda
 *
 */
public class LocalPOS {

	private Map<String, ProductStateSet> stock;
	private Map<String, List<Result>> result;

	// もうかりまっか
	// 収入
	private int sales = 0;
	// 支出
	private int cost = 0;

	/**
	 * 取扱商品設定
	 */
	private void initStock() {
		stock = new THashMap<>();
		ShopManager.getInstance().products().stream().forEach(p -> stock.put(p, new ProductStateSet(p)));
	}

	/**
	 * 取扱商品設定
	 */
	private void initResult() {
		result = new THashMap<>();
		ShopManager.getInstance().products().stream().forEach(p -> result.put(p, new ArrayList<>()));
	}

	public LocalPOS() {
		initStock();
		initResult();
	}

	public int cost() {
		return cost;
	}

	public int sales() {
		return sales;
	}

	public ProductStateSet getProductState(String p) {
		return stock.get(p);
	}

	/**
	 * 在庫を取得。
	 * 
	 * @param jan
	 * @return
	 */
	public int stock(String jan) {
		if (!stock.containsKey(jan)) {
			return 0;
		}
		ProductStateSet ps = stock.get(jan);
		// 消費期限ごとに保存されているので合計する
		return ps.total();
	}

	/**
	 * 発注に応じて在庫補充
	 * 
	 * @param jan
	 * @param amount
	 * @param ordarDate
	 * @return 実際に発注できた数
	 */
	public int order(String jan, int amount, LocalDate ordarDate) {
		if (!stock.containsKey(jan)) {
			return -1;
		}
		ProductStateSet ps = stock.get(jan);
		int expire = ShopManager.getInstance().expire(jan);

		// 在庫いっぱいになるまで調整
		// ここでやるの？
		amount = Math.min(amount, ShopManager.getInstance().stockLimit() - stock(jan));

		boolean result = ps.set(ordarDate, ordarDate.plusDays(expire), amount);
		if (!result) {
			return -1;
		} else {
			cost += ShopManager.getInstance().cost(jan) * amount;
			return amount;
		}

	}

	/**
	 * 販売シミュレートを実行し、チャンスロスを返す。販売数ではないので注意。 古い順にはけていく
	 * 
	 * @param jan
	 * @param demand 販売予定数
	 * @param price
	 * @return チャンスロス
	 */
	public int saleAndGetChancelossFIFO(String jan, int demand, int price) {
		if (!stock.containsKey(jan)) {
			return 0;
		}
		ProductStateSet ps = stock.get(jan);

		int willBeSold = demand;

		for (Iterator<ProductState> it = ps.iterator(); it.hasNext();) {
			// 消費期限が古いほうから同じ日の在庫セットをまとめて持ってくる
			ProductState p = it.next();
			int stock = p.amount();
			if (stock <= willBeSold) {
				// この消費期限の分はぜんぶ売れる
				willBeSold -= stock;
				it.remove();
				if (willBeSold == 0) {
					// ぴったり！
					break;
				}
			} else {
				// この在庫セットですべて賄える
				p.sold(willBeSold);
				willBeSold = 0;
			}
		}
		ps.update();

		int chanceLoss = willBeSold;
		sales += price * (demand - chanceLoss);
		return chanceLoss;
	}

	/**
	 * 販売シミュレートを実行し、チャンスロスを返す。販売数ではないので注意。 新しい順にはけていく
	 * 
	 * @param jan
	 * @param demand 販売予定数
	 * @param price
	 * @return チャンスロス
	 */
	public int saleAndGetChanceloss(String jan, int demand, int price) {
		if (!stock.containsKey(jan)) {
			return 0;
		}
		ProductStateSet ps = stock.get(jan);

		int willBeSold = demand;

		for (ListIterator<ProductState> it = ps.iteratorOnLast(); it.hasPrevious();) {
			// 消費期限が新しいほうから同じ日の在庫セットをまとめて持ってくる
			ProductState p = it.previous();
			int stock = p.amount();
			if (stock <= willBeSold) {
				// この消費期限の分はぜんぶ売れる
				willBeSold -= stock;
				it.remove();
				if (willBeSold == 0) {
					// ぴったり！
					break;
				}
			} else {
				// この在庫セットですべて賄える
				p.sold(willBeSold);
				willBeSold = 0;
			}
		}
		ps.update();

		int chanceLoss = willBeSold;
		sales += price * (demand - chanceLoss);
		return chanceLoss;
	}

	/**
	 * 消費期限が切れる商品を廃棄する
	 * 
	 * @param jan
	 * @param date
	 * @return 廃棄数
	 */
	public int expire(String jan, LocalDate date) {
		if (!stock.containsKey(jan)) {
			return 0;
		}
		ProductStateSet ps = stock.get(jan);
		int expired = 0;
		for (Iterator<ProductState> it = ps.iterator(); it.hasNext();) {
			// 消費期限が古いほうから同じ日の在庫セットをまとめて持ってくる
			ProductState p = it.next();
			if (!date.isBefore(p.expire())) {
				expired += p.amount();
				it.remove();
			} else {
				// 入荷から消費期限までが一定なので全部チェックしなくてもここでbreakしていい気がする
			}
		}
		ps.update();

		return expired;
	}

	/**
	 * 販売実績を記録
	 * 
	 * @param jan
	 * @param l
	 * @return
	 */
	public boolean record(String jan, Result l) {
		if (!result.containsKey(jan)) {
			return false;
		}
		return result.get(jan).add(l);
	}

	/**
	 * 販売実績の取得
	 * 
	 * @param jan
	 * @param date
	 * @return
	 */
	public Result result(String jan, LocalDate date) {
		if (!result.containsKey(jan)) {
			return null;
		}
		for (Result r : result.get(jan)) {
			if (LocalDate.parse(r.date()).equals(date)) {
				return r;
			}
		}
		return null;
	}

	public List<Result> allResult(String jan) {
		return result.getOrDefault(jan, null);
	}

	public int totalSold(String jan) {
		return result.get(jan).stream().mapToInt(Result::sold).sum();
	}

	public int totalExpired(String jan) {
		return result.get(jan).stream().mapToInt(Result::expired).sum();
	}

	public int totalChanceloss(String jan) {
		return result.get(jan).stream().mapToInt(Result::chanceloss).sum();
	}

}
