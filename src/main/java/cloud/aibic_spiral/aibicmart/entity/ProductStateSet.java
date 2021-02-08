package cloud.aibic_spiral.aibicmart.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author t-kanda
 *
 */
public class ProductStateSet {

	@JsonProperty("jancode")
	@Schema(description = "JANコード")
	private String jan;

	@JsonProperty
	@Schema(description = "消費期限別在庫情報")
	private List<ProductState> productStates;

	@JsonProperty
	@Schema(description = "合計")
	private int total;

	public ProductStateSet(String jan) {
		this.jan = jan;
		productStates = new ArrayList<>();
	}

	/**
	 * 入荷
	 * 
	 * @param made
	 *            製造年月日
	 * @param expire
	 *            消費期限
	 * @param amount
	 *            入荷量
	 * @return true
	 */
	public boolean set(LocalDate made, LocalDate expire, int amount) {
		boolean status = productStates.add(new ProductState(made, expire, amount));
		total += amount;
		return status;
	}


	/**
	 * 外部から値を書き換える用。操作後に update() が必要。
	 * @return 消費期限別在庫のイテレータ
	 */
	public Iterator<ProductState> iterator() {
		return productStates.iterator();
	}
	
	/**
	 * 外部から値を書き換える用。操作後に update() が必要。
	 * @return 消費期限別在庫のイテレータ。リストの最期をさす
	 */
	public ListIterator<ProductState> iteratorOnLast() {
		return productStates.listIterator(productStates.size());
	}
	
	public int total() {
		return total;
	}

	/**
	 * 棚卸し。在庫数を数えなおす。
	 */
	public void update() {
		total = productStates.stream().mapToInt(ProductState::amount).sum();
	}


}
