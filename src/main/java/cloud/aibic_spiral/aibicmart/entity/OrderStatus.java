package cloud.aibic_spiral.aibicmart.entity;

import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import gnu.trove.set.hash.THashSet;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 発注結果。日付と商品ごとの発注結果。
 * @author t-kanda
 *
 */
public class OrderStatus {
	
	@JsonProperty
	@Schema(description = "発注日")
	private String date;

	@JsonProperty
	@Schema(description = "発注結果")
	private Set<OrderStatusForProduct> status;
	
	public OrderStatus(LocalDate date) {
		this.date = date.toString();
		status = new THashSet<>();
	}

	public void success(String jan, int amount) {
		status.add(new OrderStatusForProduct(jan, "success", amount));
	}

	public void noOrder(String jan) {
		status.add(new OrderStatusForProduct(jan, "no-order", 0));
	
	}
}
