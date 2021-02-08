package cloud.aibic_spiral.aibicmart.pos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import cloud.aibic_spiral.aibicmart.controller.ShopManagerOneItem;
import cloud.aibic_spiral.aibicmart.entity.ProductState;
import cloud.aibic_spiral.aibicmart.entity.ProductStateSet;
import cloud.aibic_spiral.aibicmart.entity.Result;

/**
 * シミュレート中の在庫と売り上げの管理。在庫は消費期限ごとに格納されている。
 * 
 * @author t-kanda
 *
 */
public class LocalPOSOneItem {

	private String jan;
	private ProductStateSet product;
	private List<Result> result;

	// もうかりまっか
	// 収入
	private int sales = 0;
	// 支出
	private int cost = 0;

	public LocalPOSOneItem(String jan) {
		this.jan = jan;
		product = new ProductStateSet(jan);
		result = new ArrayList<>();
	}

	public int cost() {
		return cost;
	}

	public int sales() {
		return sales;
	}

	public ProductStateSet getProductState() {
		return product;
	}

	/**
	 * 在庫を取得。
	 * 
	 * @return
	 */
	public int stock() {
		// 消費期限ごとに保存されているので合計する
		return product.total();
	}

	/**
	 * 発注に応じて在庫補充
	 * 
	 * @param amount
	 * @param ordarDate
	 * @return 実際に発注できた数
	 */
	public int order(int amount, LocalDate ordarDate) {

		int expire = ShopManagerOneItem.getInstance().expire(jan);

		// 在庫上限を超えるような発注を防ぐ
		amount = Math.min(amount, ShopManagerOneItem.getInstance().stockLimit() - stock());

		boolean result = product.set(ordarDate, ordarDate.plusDays(expire), amount);
		if (!result) {
			return -1;
		} else {
			cost += ShopManagerOneItem.getInstance().cost(jan) * amount;
			return amount;
		}

	}

	/**
	 * 販売シミュレートを実行し、チャンスロスを返す。販売数ではないので注意。 古い順にはけていく
	 * 
	 * @param demand 販売予定数
	 * @param price
	 * @return チャンスロス
	 */
	public int saleAndGetChancelossFIFO(int demand, int price) {

		int willBeSold = demand;

		for (Iterator<ProductState> it = product.iterator(); it.hasNext();) {
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
		product.update(); // イテレータで直接内部を操作したので整合性をとる

		int chanceLoss = willBeSold;
		sales += price * (demand - chanceLoss);
		return chanceLoss;
	}

	/**
	 * 販売シミュレートを実行し、チャンスロスを返す。販売数ではないので注意。 新しい順にはけていく
	 * 
	 * @param demand 販売予定数
	 * @param price
	 * @return チャンスロス
	 */
	public int saleAndGetChanceloss(String jan, int demand, int price) {

		int willBeSold = demand;

		for (ListIterator<ProductState> it = product.iteratorOnLast(); it.hasPrevious();) {
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
		product.update(); // イテレータで直接内部を操作したので整合性をとる

		int chanceLoss = willBeSold;
		sales += price * (demand - chanceLoss);
		return chanceLoss;
	}

	/**
	 * 消費期限が切れる商品を廃棄する
	 * 
	 * @param date
	 * @return 廃棄数
	 */
	public int expire(String jan, LocalDate date) {
		int expired = 0;
		for (Iterator<ProductState> it = product.iterator(); it.hasNext();) {
			// 消費期限が古いほうから同じ日の在庫セットをまとめて持ってくる
			ProductState p = it.next();
			if (!date.isBefore(p.expire())) {
				expired += p.amount();
				it.remove();
			} else {
				// 入荷から消費期限までが一定なので全部チェックしなくてもここでbreakしていい気がする。高々消費期限ぶん（1桁くらいを想定）ループしかしないので放置。
			}
		}
		product.update();

		return expired;
	}

	/**
	 * 販売実績を記録
	 * 
	 * @param l
	 * @return
	 */
	public boolean record(Result l) {
		return result.add(l);
	}

	/**
	 * 販売実績の取得
	 * 
	 * @param date
	 * @return
	 */
	public Result result(LocalDate date) {
		for (Result r : result) {
			if (LocalDate.parse(r.date()).equals(date)) {
				return r;
			}
		}
		return null;
	}

	public List<Result> allResult() {
		return result;
	}

	public int totalSold() {
		return result.stream().mapToInt(Result::sold).sum();
	}

	public int totalExpired() {
		return result.stream().mapToInt(Result::expired).sum();
	}

	public int totalChanceloss() {
		return result.stream().mapToInt(Result::chanceloss).sum();
	}

}
