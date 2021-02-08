package cloud.aibic_spiral.aibicmart.rest;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cloud.aibic_spiral.aibicmart.controller.ShopManagerOneItem;
import cloud.aibic_spiral.aibicmart.controller.ShopOneItem;
import cloud.aibic_spiral.aibicmart.entity.Date;
import cloud.aibic_spiral.aibicmart.entity.Message;
import cloud.aibic_spiral.aibicmart.entity.Order;
import cloud.aibic_spiral.aibicmart.entity.OrderStatus;
import cloud.aibic_spiral.aibicmart.entity.Price;
import cloud.aibic_spiral.aibicmart.entity.ProductInfo;
import cloud.aibic_spiral.aibicmart.entity.ProductStateSet;
import cloud.aibic_spiral.aibicmart.entity.Result;
import cloud.aibic_spiral.aibicmart.entity.SummaryReport;
import cloud.aibic_spiral.aibicmart.entity.Token;
import cloud.aibic_spiral.aibicmart.entity.User;
import cloud.aibic_spiral.aibicmart.entity.Weather;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * REST APIを提供する。
 * 
 * @author t-kanda
 *
 */
@Path("/")
public class ShopRest {

	/**
	 * 練習 GET /gogo
	 * 
	 * @return 掛け声
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/gogo")
	@Operation(summary = "起動後のテスト用API", tags = {
			"test" }, description = "GETすると挨拶を返すだけのAPI。シミュレータの起動を確認できる。", responses = {
					@ApiResponse(responseCode = "200", description = "挨拶") })
	public Response getGogo() {
		return Response.status(200).entity("おはようございます！").build();
	}

	/**
	 * 店舗インスタンス始動
	 *
	 * @param from シミュレーション開始日
	 * @param to   シミュレーション終了日
	 * @param jan  商品
	 * 
	 * @return 店舗トークン
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/")
	@Operation(summary = "店舗インスタンス生成", tags = {
			"店舗インスタンス管理" }, description = "日付fromから日付toのシミュレーションを行う店舗インスタンスを立ち上げる。立ち上げに成功したら以降のアクセス用キー（と、修正後のシミュレート期間）を返す。"
					+ "日付を省略した場合はシミュレータの設定によりシミュレート期間を最大にとる。" + "ユーザごとに同時に起動できるのは1店舗までで同じidとkeyの組でアクセスが来ると前の店は破棄される。"
					+ "生成失敗時のメッセージはなるべく学生が原因を推測可能なようになっている。", responses = {
							@ApiResponse(responseCode = "201", description = "インスタンス生成成功", content = @Content(schema = @Schema(implementation = Token.class), examples = {
									@ExampleObject(value = "{\"token\": 99999, \"since\": \"2010-01-01\", \"until\": \"2010-06-30\"}") })),
							@ApiResponse(responseCode = "400", description = "パラメータ不正により店舗インスタンス生成失敗") })
	public Response init(@Parameter(description = "シミュレーション開始日") @QueryParam("from") String from,
			@Parameter(description = "シミュレーション終了日") @QueryParam("to") String to,
			@Parameter(description = "対象商品", required = true) @QueryParam("jan") String jan,
			@RequestBody(description = "ユーザ情報", content = @Content(schema = @Schema(implementation = User.class))) User u) {

		LocalDate since;
		LocalDate until;
		ShopManagerOneItem smo = ShopManagerOneItem.getInstance();

		try {
			since = (from == null) ? smo.simulateStartLimit() : LocalDate.parse(from);
			until = (to == null) ? smo.simulateEndLimit() : LocalDate.parse(to);
		} catch (DateTimeParseException e) {
			return MarketResponce.invalidDateFormat400();
		}
		if (since.isBefore(smo.simulateStartLimit()) || until.isAfter(smo.simulateEndLimit()) || since.isAfter(until)) {
			return MarketResponce.invalidPeriod400(since, until);
		}
		if (!smo.isValidProduct(jan)) {
			return MarketResponce.invalidProduct400();
		}
		Token t = smo.init(u, jan, since, until);
		if (t == null) {
			return MarketResponce.shopCreationFailed400();
		}

		URI location = URI.create("/api/shops" + t.token());
		return success201(t, location);
	}

	/**
	 * 店舗インスタンス破棄
	 * 
	 * @param token 店舗トークン
	 * @return 閉店の成否
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}")
	@Operation(summary = "店舗インスタンス破棄", tags = {
			"店舗インスタンス管理" }, description = "シミュレートを途中で打ち切り店舗インスタンスを破棄する。ログもとれなくなる。（破棄しなくても新しいシミュレーションは開始できるのであまり出番はない）", responses = {
					@ApiResponse(responseCode = "204", description = "インスタンス破棄成功"),
					@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし"),
					@ApiResponse(responseCode = "500", description = "内部エラーにより店舗インスタンス破棄失敗") })
	public Response forceDelete(@PathParam("token") int token) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFound404();
		}
		if (ShopManagerOneItem.getInstance().close(token)) {
			return Response.status(204).entity(new Message("Shop DELETED")).build();
		} else {
			return Response.status(500).entity(new Message("DELETE failed")).build();
		}
	}

	/**
	 * @param token
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/today")
	@Operation(summary = "日付照会（当日）", tags = { "カレンダー・天気" }, description = "店舗シミュレータ内のカレンダーの確認。本日の情報を返す。", responses = {
			@ApiResponse(responseCode = "200", description = "本日のカレンダー情報", content = @Content(schema = @Schema(implementation = Date.class), examples = {
					@ExampleObject(value = "{\"date\":\"2008-12-31\",\"year\":2008,\"month\":12,\"day\":31,\"dayOfWeek\":\"Wed\",\"isHoliday\":2,\"holidayName\":\"不明\",\"weekNumber\":53,\"closed\":1}") })),
			@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし") })
	public Response getToday(@PathParam("token") int token) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFound404();
		}
		return success(shop.getDateToday());
	}

	/**
	 * @param token
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/tomorrow")
	@Operation(summary = "日付照会（翌日）", tags = { "カレンダー・天気" }, description = "店舗シミュレータ内のカレンダーの確認。翌日の情報を返す。", responses = {
			@ApiResponse(responseCode = "200", description = "翌日のカレンダー情報", content = @Content(schema = @Schema(implementation = Date.class))),
			@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし") })

	public Response getTomorrow(@PathParam("token") int token) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFound404();
		}
		return success(shop.getDateTomorrow());
	}

	/**
	 * @param token
	 * @param jan
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/{jan}/data")
	@Operation(summary = "商品情報取得", tags = {
			"店舗操作" }, description = "商品情報の取得。POSデータのほか、シミュレータの情報として仕入れ値と消費期限を含む。", responses = {
					@ApiResponse(responseCode = "200", description = "取得に成功", content = @Content(schema = @Schema(implementation = ProductInfo.class), examples = {
							@ExampleObject(value = "{\r\n" + "  \"makerName\": \"PBL乳業\",\r\n"
									+ "  \"makerCode\": \"1234567\",\r\n" + "  \"JANCode\": \"01234567123456\",\r\n"
									+ "  \"productName\": \"ヨーグルト 100g\",\r\n" + "  \"JICFSName\": \"ヨーグルト\",\r\n"
									+ "  \"JICFSCode\": \"130205\",\r\n" + "  \"costPrice\": \"80\",\r\n"
									+ "  \"expire\": \"5\"\r\n" + "}") })),
					@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし"),
					@ApiResponse(responseCode = "404", description = "JANコードに対応する商品なし") })
	public Response getInfo(@PathParam("token") int token, @PathParam("jan") String jan) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFound404();
		}
		Map<String, String> result = shop.getProductInfo();
		if (result == null) {
			return MarketResponce.productNotFound404();
		}
		return success(result);
	}

	/**
	 * @param token
	 * @param jan
	 * @param date
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/{jan}/result/")
	@Operation(summary = "結果（当日）", tags = { "店舗操作" }, description = "その日のシミュレート結果の取得。", responses = {
			@ApiResponse(responseCode = "200", description = "シミュレート結果の取得に成功", content = @Content(schema = @Schema(implementation = Result.class), examples = {
					@ExampleObject(value = "{\r\n" + "  \"date\": \"2010-01-01\",\r\n" + "  \"visitor\": 80,\r\n"
							+ "  \"jancode\": \"01234567123456\",\r\n" + "  \"stock_start\": 11,\r\n"
							+ "  \"stock_end\": 0,\r\n" + "  \"sales\": 20,\r\n" + "  \"sold\": 11,\r\n"
							+ "  \"expired\": 0,\r\n" + "  \"chanceloss\": 9\r\n" + "}") })),
			@ApiResponse(responseCode = "400", description = "日付の不正"),
			@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし"),
			@ApiResponse(responseCode = "404", description = "JANコードに対応する商品なし") })
	public Response getTodayResult(@PathParam("token") int token, @PathParam("jan") String jan) {
		return getResult(token, jan, 0);
	}

	/**
	 * @param token
	 * @param jan
	 * @param date
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/{jan}/result/of/{date}")
	@Operation(summary = "結果（日指定）", tags = { "店舗操作" }, description = "その日のシミュレート結果の取得。", responses = {
			@ApiResponse(responseCode = "200", description = "シミュレート結果の取得に成功", content = @Content(schema = @Schema(implementation = Result.class))),
			@ApiResponse(responseCode = "400", description = "日付の不正"),
			@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし"),
			@ApiResponse(responseCode = "404", description = "JANコードに対応する商品なし") })
	public Response getResult(@PathParam("token") int token, @PathParam("jan") String jan,
			@PathParam("date") String date) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFound404();
		}
		LocalDate d;
		try {
			d = LocalDate.parse(date);
		} catch (DateTimeParseException e) {
			return MarketResponce.invalidDateFormat400();
		}
		if (!shop.isDatePastOrToday(d)) {
			return MarketResponce.invalidDate400();
		}
		Result result = shop.getResult(d);
		if (result == null) {
			return MarketResponce.productNotFound404();
		}
		return success(result);
	}

	/**
	 * @param token
	 * @param jan
	 * @param date
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/{jan}/result/relative/{days}")
	@Operation(summary = "結果（相対日指定）", tags = { "店舗操作" }, description = "その日のシミュレート結果の取得。", responses = {
			@ApiResponse(responseCode = "200", description = "シミュレート結果の取得に成功", content = @Content(schema = @Schema(implementation = Result.class))),
			@ApiResponse(responseCode = "400", description = "日付の不正"),
			@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし"),
			@ApiResponse(responseCode = "404", description = "JANコードに対応する商品なし") })
	public Response getResult(@PathParam("token") int token, @PathParam("jan") String jan,
			@PathParam("days") int days) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFound404();
		}
		// 今日まで
		if (days > 0) {
			return MarketResponce.invalidOffset400();
		}
		LocalDate d = shop.getDate(days);
		Result result = shop.getResult(d);
		if (result == null) {
			return MarketResponce.productNotFound404();
		}
		return success(result);
	}

	/**
	 * @param token
	 * @param jan
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/{jan}/stock")
	@Operation(summary = "在庫", tags = { "店舗操作" }, description = "現時点の在庫の状態を返す", responses = {
			@ApiResponse(responseCode = "200", description = "在庫の取得に成功", content = @Content(schema = @Schema(implementation = ProductStateSet.class), examples = {
					@ExampleObject(value = "{\r\n" + "  \"jancode\":  \"1234567890123\",\r\n" + "  \"status\": [\r\n"
							+ "    {\r\n" + "      \"expiration\": \"2017-05-11\",\r\n"
							+ "      \"made\": \"2017-05-11\",\r\n" + "      \"amount\": 2\r\n" + "    }\r\n"
							+ "  ],\r\n" + "  \"total\": 2\r\n" + "}") })),
			@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし"),
			@ApiResponse(responseCode = "404", description = "JANコードに対応する商品なし") })
	public Response getStock(@PathParam("token") int token, @PathParam("jan") String jan) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFound404();
		}
		ProductStateSet result = shop.getStock();
		if (result == null) {
			return MarketResponce.productNotFound404();
		}
		return success(result);
	}

	/**
	 * @param token
	 * @param jan
	 * @param date
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/{jan}/retailprice/of/{date}")
	@Operation(summary = "販売価格（日指定）", tags = {
			"店舗操作" }, description = "その日の販売価格の取得。販売数0で実際の販売価格がわからないときは定価（シミュレータの設定で記述）。", responses = {
					@ApiResponse(responseCode = "200", description = "販売価格の取得に成功", content = @Content(schema = @Schema(implementation = Price.class))),
					@ApiResponse(responseCode = "400", description = "日付の不正"),
					@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし"),
					@ApiResponse(responseCode = "404", description = "JANコードに対応する商品なし") })
	public Response getRetailPrice(@PathParam("token") int token, @PathParam("jan") String jan,
			@PathParam("date") String date) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFound404();
		}
		LocalDate d;
		try {
			d = LocalDate.parse(date);
		} catch (DateTimeParseException e) {
			return MarketResponce.invalidDateFormat400();
		}
		if (!shop.isDateTomorrowOrBefore(d)) {
			return MarketResponce.invalidDate400();
		}
		Price price = shop.getRetailPrice(d);
		if (price == null) {
			return MarketResponce.productNotFound404();
		}
		return success(price);
	}

	/**
	 * @param token
	 * @param jan
	 * @param date
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/{jan}/retailprice/relative/{days}")
	@Operation(summary = "販売価格（相対日指定）", tags = {
			"店舗操作" }, description = "その日の販売価格の取得。販売数0で実際の販売価格がわからないときは定価（シミュレータの設定で記述）。", responses = {
					@ApiResponse(responseCode = "200", description = "販売価格の取得に成功", content = @Content(schema = @Schema(implementation = Price.class))),
					@ApiResponse(responseCode = "400", description = "日付の不正"),
					@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし"),
					@ApiResponse(responseCode = "404", description = "JANコードに対応する商品なし") })
	public Response getRetailPrice(@PathParam("token") int token, @PathParam("jan") String jan,
			@PathParam("days") int days) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFound404();
		}
		// 1日先は許す
		if (days > 1) {
			return MarketResponce.invalidOffset400();
		}
		LocalDate d = shop.getDate(days);
		Price price = shop.getRetailPrice(d);
		if (price == null) {
			return MarketResponce.productNotFound404();
		}
		return success(price);
	}

	/**
	 * @param token
	 * @param jan
	 * @param date
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/{jan}/retailprice/")
	@Operation(summary = "販売価格（当日）", tags = {
			"店舗操作" }, description = "その日の販売価格の取得。販売数0で実際の販売価格がわからないときは定価（シミュレータの設定で記述）。", responses = {
					@ApiResponse(responseCode = "200", description = "販売価格の取得に成功", content = @Content(schema = @Schema(implementation = Price.class), examples = {
							@ExampleObject(value = "{\r\n" + "  \"jancode\": \"1234567890123\",\r\n"
									+ "  \"date\": \"2011-10-12\",\r\n" + "  \"price\": 130\r\n" + "}") })),
					@ApiResponse(responseCode = "400", description = "日付の不正"),
					@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし"),
					@ApiResponse(responseCode = "404", description = "JANコードに対応する商品なし") })
	public Response getRetailPrice(@PathParam("token") int token, @PathParam("jan") String jan) {
		return getRetailPrice(token, jan, 0);
	}

	@GET
	@Produces({ "text/tab-separated-values", MediaType.TEXT_PLAIN })
	@Path("/shops/{token}/{jan}/log")
	@Operation(summary = "全記録出力", tags = { "ログ出力" }, description = "日ごとの商品売り上げ記録を整形して返す。", responses = {
			@ApiResponse(responseCode = "200", description = "TSV形式の結果一覧", content = @Content(mediaType = "text/tab-separated-values", examples = {
					@ExampleObject(value = "date    visitor stockStart      stockEnd        sales   sold    expired chanceloss      orderAmount     proceeds        expenditure\r\n"
							+ "2017-02-29      0       0       0       0       0       0       0       35     0       0\r\n"
							+ "2017-03-01      845     35      0       45      35      0       10      35     3500    2800\r\n"
							+ "2017-03-02      469     35      25      10      10      0       0       35     4500   5600") })),
			@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし"),
			@ApiResponse(responseCode = "404", description = "JANコードに対応する商品なし") })
	public Response log(@PathParam("token") int token, @PathParam("jan") String jan) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFoundPlain404();
		}
		String result = shop.logCreation();
		if (result == null) {
			return MarketResponce.productNotFoundPlain404();
		}
		return success(result);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/{jan}/summary")
	@Operation(summary = "総計記録出力", tags = {
			"ログ出力" }, description = "現時点までの商品売り上げ記録の概要を返す。現時点での在庫と、それ以外の累計の情報。シミュレート終了時にこのデータを報告させる。", responses = {
					@ApiResponse(responseCode = "200", description = "現時点までの商品売り上げ記録の概要", content = @Content(schema = @Schema(implementation = SummaryReport.class), examples = {
							@ExampleObject(value = "{\r\n" + "  \"uid\": 1,\r\n"
									+ "  \"jancode\": \"1234567890123\",\r\n" + "  \"since\": \"2017-06-01\",\r\n"
									+ "  \"until\": \"2017-06-08\",\r\n" + "  \"report_date\": \"2017-06-08\",\r\n"
									+ "  \"stock_last\": 127,\r\n" + "  \"total_order\": 6200,\r\n"
									+ "  \"total_sold\": 4800,\r\n" + "  \"total_expired\": 0,\r\n"
									+ "  \"total_chanceloss\": 906,\r\n" + "  \"total_sales\": 48000,\r\n"
									+ "  \"total_cost\": 49600,\r\n" + "  \"total_earnings\": -1600\r\n" + "}") })),
					@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし"),
					@ApiResponse(responseCode = "404", description = "JANコードに対応する商品なし") })
	public Response summary(@PathParam("token") int token, @PathParam("jan") String jan) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFoundPlain404();
		}
		SummaryReport result = shop.report();
		if (result == null) {
			return MarketResponce.productNotFoundPlain404();
		}
		return success(result);
	}

	/**
	 * 
	 * @param token
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/weather/")
	@Operation(summary = "気象情報（当日）", tags = { "カレンダー・天気" }, description = "店舗シミュレータ内の気象情報の確認。本日の情報を返す。", responses = {
			@ApiResponse(responseCode = "200", description = "本日の気象情報：学生に配布するCSVの内容と同じ", content = @Content(schema = @Schema(implementation = Weather.class), examples = {
					@ExampleObject(value = "{\r\n" + "  \"date\": \"2017-06-01\",\r\n" + "  \"overview\": \"曇\",\r\n"
							+ "  \"rainfall\": \"0\",\r\n" + "  \"averageTemp\": \"5.7\",\r\n"
							+ "  \"maxTemp\": \"9.2\",\r\n" + "  \"minTemp\": \"2.9\",\r\n"
							+ "  \"solarRadiation\": \"8.02\"\r\n" + "}") })),
			@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし") })
	public Response getWeatherForecast(@PathParam("token") int token) {
		return getWeatherForecast(token, 0);
	}

	/**
	 * 
	 * @param token
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/weather/of/{date}")
	@Operation(summary = "気象情報（日指定）", tags = {
			"カレンダー・天気" }, description = "店舗シミュレータ内の気象情報の確認。指定日の情報を返す。未来については1日だけ許容する（この場合天気予報となり気温の小数点以下を四捨五入する）", responses = {
					@ApiResponse(responseCode = "200", description = "指定日の気象情報", content = @Content(schema = @Schema(implementation = Weather.class))),
					@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし") })
	public Response getWeatherForecast(@PathParam("token") int token, @PathParam("date") String date) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFound404();
		}
		LocalDate d;
		try {
			d = LocalDate.parse(date);
		} catch (DateTimeParseException e) {
			return MarketResponce.invalidDateFormat400();
		}
		if (!shop.isDateTomorrowOrBefore(d)) {
			return MarketResponce.invalidDate400();
		}
		return success(shop.getWeatherForecast(d));
	}

	/**
	 * 
	 * @param token
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/weather/relative/{days}")
	@Operation(summary = "気象情報（相対日指定）", tags = {
			"カレンダー・天気" }, description = "店舗シミュレータ内の気象情報の確認。本日の日付にdays日を加えた日付の情報を返す。未来については1日だけ許容する（この場合天気予報となり気温の小数点以下を四捨五入する）", responses = {
					@ApiResponse(responseCode = "200", description = "本日の日付にdays日を加えた日付の気象情報", content = @Content(schema = @Schema(implementation = Weather.class))),
					@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし") })
	public Response getWeatherForecast(@PathParam("token") int token, @PathParam("days") int days) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFound404();
		}
		// 1日先は許す
		if (days > 1) {
			return MarketResponce.invalidOffset400();
		}
		LocalDate d = shop.getDate(days);
		return success(shop.getWeatherForecast(d));
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/order")
	@Operation(summary = "発注履歴", tags = {
			"店舗操作" }, description = "日付fromからtoの間の発注履歴を返す。fromとtoを省略した場合はシミュレート済みの全期間の履歴を返す。", responses = {
					@ApiResponse(responseCode = "200", description = "発注履歴の取得に成功", content = @Content(schema = @Schema(implementation = Arrays.class), examples = {
							@ExampleObject(value = "[\r\n" + "  {\r\n" + "    \"date\": \"2013-09-12\",\r\n"
									+ "    \"orders\": [\r\n" + "      {\r\n"
									+ "        \"jancode\": \"1234567890123\",\r\n" + "        \"amount\": 3\r\n"
									+ "      }\r\n" + "    ]\r\n" + "  },\r\n" + "  {\r\n"
									+ "    \"date\": \"2013-09-13\",\r\n" + "    \"orders\": [\r\n" + "      {\r\n"
									+ "        \"jancode\":  \"1234567890123\",\r\n" + "        \"amount\": 3\r\n"
									+ "      }\r\n" + "    ]\r\n" + "  }\r\n" + "]") })),
					@ApiResponse(responseCode = "400", description = "日付の不正"),
					@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし"),
					@ApiResponse(responseCode = "404", description = "JANコードに対応する商品なし") })
	public Response getOrderHistory(@PathParam("token") int token, @QueryParam("from") String from,
			@QueryParam("to") String to) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFound404();
		}
		LocalDate since;
		LocalDate until;
		try {
			since = (from == null) ? ShopManagerOneItem.getInstance().simulateStartLimit() : LocalDate.parse(from);
			until = (to == null) ? ShopManagerOneItem.getInstance().simulateEndLimit() : LocalDate.parse(to);
		} catch (DateTimeParseException e) {
			return MarketResponce.invalidDateFormat400();
		}

		return success(shop.getOrderHistory(since, until));
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/shops/{token}/order")
	@Operation(summary = "発注指示", tags = {
			"店舗操作" }, description = "発注oを指示する。発注が成功した場合には、カレンダーが1日進んで再び業務サイクルが実行される。", responses = {
					@ApiResponse(responseCode = "201", description = "発注成功：実際に発注された内容が返ってくる", content = @Content(schema = @Schema(implementation = Order.class), examples = {
							@ExampleObject(value = "{  \"date\": \"2017-06-01\",\r\n" + "  \"status\": [\r\n"
									+ "    {\r\n" + "      \"jancode\": \"1234567890123\",\r\n"
									+ "      \"status\": \"success\",\r\n" + "      \"amount\": 50\r\n" + "    }\r\n"
									+ "  ]}") })),
					@ApiResponse(responseCode = "404", description = "トークンに対応する店舗なし"),
					@ApiResponse(responseCode = "500", description = "発注エラー：シミュレート期間が終了している店舗で発注した、マイナス発注をかけた、等") })
	public Response order(@PathParam("token") int token,
			@RequestBody(description = "発注内容", content = @Content(schema = @Schema(implementation = Order.class))) Order o) {
		ShopOneItem shop = ShopManagerOneItem.getInstance().shop(token);
		if (shop == null) {
			return MarketResponce.shopNotFound404();
		}
		OrderStatus result = shop.order(o);
		if (result != null) {
			URI location = URI.create("/api/shops/" + token + "/order");
			return success201(result, location);
		} else {
			return MarketResponce.orderFailed500();
		}
	}

//	@POST
//	@Produces(MediaType.APPLICATION_JSON)
//	@Path("/admin/reloadconfig")
//	public Response reloadconfig() {
//		ShopManagerOneItem.getInstance().loadConfig();
//		return success("reload config");
//	}

	private Response success(Object o) {
		return Response.status(200).entity(o).build();
	}

	private Response success201(Object o, URI location) {
		return Response.status(201).entity(o).location(location).build();
	}

}
