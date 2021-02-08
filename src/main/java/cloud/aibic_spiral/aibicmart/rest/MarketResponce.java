package cloud.aibic_spiral.aibicmart.rest;

import java.time.LocalDate;

import javax.ws.rs.core.Response;

import cloud.aibic_spiral.aibicmart.controller.ShopManagerOneItem;
import cloud.aibic_spiral.aibicmart.entity.Message;

/**
 * ステータスコードとメッセージをセットで返す。学生が原因特定できるようにメッセージは親切寄り。
 * @author t-kanda
 *
 */
public class MarketResponce {

	private static String PRODUCT_NOT_FOUND = "商品が見つかりませんでした。 Product not found. （JANCODE指定ミスかも））";
	private static String SHOP_NOT_FOUND = "店舗インスタンスが見つかりませんでした。 Shop not found. （同じidで別店舗起動して消えたかも？））";

	public static Response productNotFound404() {
		return error404(PRODUCT_NOT_FOUND);
	}

	public static Response invalidProduct400() {
		return error400(PRODUCT_NOT_FOUND);
	}

	public static Response shopNotFound404() {
		return error404(SHOP_NOT_FOUND);
	}

	public static Response productNotFoundPlain404() {
		return errorPlain404(PRODUCT_NOT_FOUND);
	}

	public static Response shopNotFoundPlain404() {
		return errorPlain404(SHOP_NOT_FOUND);
	}

	public static Response invalidOffset400() {
		return error400("日付指定のオフセットが不正です。 Invalid offset. （APIによってどこまで先のデータを取得できるかが制限されています））");
	}

	public static Response invalidDate400() {
		return error400("日付が不正です。 Invalid date. （APIによってどこまで先のデータを取得できるかが制限されています））");
	}

	public static Response invalidDateFormat400() {
		return error400(
				"日付指定の形式が不正です。 Invalid date format. （日付が存在しないか書き方が間違っています。YYYY-MM-DDの形式を用いてください。 例：1600-01-04））");
	}

	public static Response invalidPeriod400(LocalDate since, LocalDate until) {
		String from = ShopManagerOneItem.getInstance().simulateStartLimit().toString();
		String to = ShopManagerOneItem.getInstance().simulateEndLimit().toString();
		return error400(String.format("期間指定 %s～%s は不正です。 Invalid Period %s to %s. （シミュレート可能な期間は%s～%sです）", since, until,
				since, until, from, to));
	}

	public static Response shopCreationFailed400() {
		return error400("店舗インスタンスの生成に失敗しました。 Shop Creation Failed. （idとkeyのペアが間違っているかも）");
	}

	public static Response orderFailed500() {
		return Response.status(500).entity(new Message("発注に失敗しました。 Order Failed. （シミュレート期間が終了しているか、マイナス発注をかけているかも）"))
				.build();
	}

	private static Response error400(String message) {
		return Response.status(400).entity(new Message(message)).build();
	}

	private static Response error404(String message) {
		return Response.status(404).entity(new Message(message)).build();
	}

	private static Response errorPlain404(String message) {
		return Response.status(404).entity(message).build();
	}

}
