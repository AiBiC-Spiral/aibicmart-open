package cloud.aibic_spiral.aibicmart.entity;

import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import gnu.trove.set.hash.THashSet;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 発注情報。日付と商品ごとの発注。
 * @author t-kanda
 *
 */
public class Order {

	@JsonProperty
	@Schema(description = "発注日")
	private String date;

	@JsonProperty
	@Schema(description = "発注内容")
	private Set<OrderForProcuct> orders;

	public Order(LocalDate date) {
		orders = new THashSet<>();
		this.date = date.toString();
	}

	public Order() {

	}

	public void setDate(LocalDate date) {
		this.date = date.toString();
	}

	public boolean addOrder(OrderForProcuct o) {
		return orders.add(o);
	}

	public OrderForProcuct orderForProduct(String p) {
		for(OrderForProcuct o : orders){
			if(o.jan().equals(p)){
				return o;
			}
		}
		return null;
	}
}
