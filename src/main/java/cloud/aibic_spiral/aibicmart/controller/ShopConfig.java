package cloud.aibic_spiral.aibicmart.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import cloud.aibic_spiral.aibicmart.entity.ProductInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopConfig {

	/**
	 * シミュレータのデータが入っているディレクトリを指す。<br>
	 * 学生から見えないようにchmodしておく。
	 */
	public static final String MART_RESOURCE = "/opt/martresource";

	public static Path getResource(String filename) {
		return Paths.get(MART_RESOURCE, filename);
	}

	private LocalDate simulateStart, simulateEnd;

	@JsonProperty("product")
	private List<ProductInfo> products;

	@JsonProperty("stock_limit")
	private int limit;

	public ShopConfig(@JsonProperty("simulate_start") String simulateStart,
			@JsonProperty("simulate_end") String simulateEnd) {
		this.simulateStart = LocalDate.parse(simulateStart);
		this.simulateEnd = LocalDate.parse(simulateEnd);
	}

	public static ShopConfig loadConfig() {
		ObjectMapper mapper = new ObjectMapper();
		try (InputStream is = Files.newInputStream(getResource("config.json"))) {
			return mapper.readValue(is, ShopConfig.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * シミュレータの動作設定
	 */

	/**
	 * シミュレーション開始下限
	 * 
	 * @return
	 */
	public LocalDate simulateStart() {
		return simulateStart;
	}

	/**
	 * シミュレーション終了上限
	 * 
	 * @return
	 */
	public LocalDate simulateEnd() {
		return simulateEnd;
	}

	/**
	 * 在庫数上限
	 * 
	 * @return
	 */
	public int limit() {
		return limit;
	}

	/**
	 * 取り扱い商品情報一覧
	 * 
	 * @return
	 */
	public List<ProductInfo> products() {
		return Collections.unmodifiableList(products);
	}

	public boolean isValidProduct(String jan) {
		return products.stream().map(ProductInfo::jan).anyMatch(jan::equals);
	}

	/*
	 * 商品毎の情報
	 */

	/**
	 * 消費期限（日数）
	 * 
	 * @param jan
	 * @return
	 */
	public int expire(String jan) {
		for (ProductInfo pi : products) {
			if (pi.jan().equals(jan)) {
				return pi.expire();
			}
		}
		return 0;
	}

	/**
	 * 仕入れ値
	 * 
	 * @param jan
	 * @return
	 */
	public int cost(String jan) {
		for (ProductInfo pi : products) {
			if (pi.jan().equals(jan)) {
				return pi.cost();
			}
		}
		return 0;
	}

	/**
	 * 定価
	 * 
	 * @param jan
	 * @return
	 */
	public int defaultPrice(String jan) {
		for (ProductInfo pi : products) {
			if (pi.jan().equals(jan)) {
				return pi.defaultPrice();
			}
		}
		return 0;
	}

	/**
	 * for debugging
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		ShopConfig j = loadConfig();
		System.out.println(j.simulateStart);
		System.out.println(j.simulateEnd);

	}

}
