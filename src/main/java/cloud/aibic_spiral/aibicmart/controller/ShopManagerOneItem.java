package cloud.aibic_spiral.aibicmart.controller;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import cloud.aibic_spiral.aibicmart.entity.ProductInfo;
import cloud.aibic_spiral.aibicmart.entity.Token;
import cloud.aibic_spiral.aibicmart.entity.User;
import cloud.aibic_spiral.aibicmart.logutils.AiBiCLogger;

/**
 * 
 * 店舗インスタンスを統括するクラス。 要求に応じて新装開店とトークン払い出し、店舗閉鎖。<br>
 * 各店舗は1商品のみ取り扱う。
 * 
 * @author t-kanda
 *
 */
public class ShopManagerOneItem {

	/**
	 * サーバ起動中単一のインスタンスで動作する。
	 */
	private static ShopManagerOneItem shopManager;

	private ShopConfig config;
	private UserAuth users;

	private Map<Integer, ShopOneItem> shops;

	public void loadConfig() {
		shops = new ConcurrentHashMap<>();
		config = ShopConfig.loadConfig();
		users = new UserAuth();
		AiBiCLogger.info("Config RELOADED", "None", 0);
	}

	public ShopManagerOneItem() {
		loadConfig();
	}

	public static ShopManagerOneItem getInstance() {
		if (shopManager == null) {
			shopManager = new ShopManagerOneItem();
		}
		return shopManager;
	}

	/**
	 * 店舗インスタンスの生成
	 * @param u ユーザ情報（ユーザ名、キー）
	 * @param jan 取り扱う商品
	 * @param since シミュレーション開始日
	 * @param until シミュレーション終了日
	 * @return アクセス用のトークン
	 */
	public Token init(User u, String jan, LocalDate since, LocalDate until) {
		if (!users.validateUser(u)) {
			return null;
		}
		if (!isValidProduct(jan)) {
			return null;
		}

		String uid = u.id();
		
		synchronized (shops) {

			// その学籍番号の店は閉じる。
			shopSet(uid).stream().forEach(s -> shops.remove(s.token()));
			
			// シミュレーション期間の強制修正
			if (since.isBefore(simulateStartLimit())) {
				since = simulateStartLimit();
			}
			if (until.isAfter(simulateEndLimit())) {
				until = simulateEndLimit();
			}
			
			// シミュレーション期間の検証
			if (since.isAfter(until)) {
				return null;
			}
			
			// 店舗トークンとインスタンス生成
			Token t = new Token(uid, since, until);
			if (shops.containsKey(t.token())) {
				return null;
			}
			shops.put(t.token(), new ShopOneItem(uid, t.token(), since, until, jan));
			return t;
		}
	}

	/*
	 * シミュレータの動作設定
	 */

	/** シミュレーション開始下限
	 * @return
	 */
	public LocalDate simulateStartLimit() {
		return config.simulateStart();
	}

	/**
	 * シミュレーション終了上限
	 * @return
	 */
	public LocalDate simulateEndLimit() {
		return config.simulateEnd();
	}

	/**
	 * 在庫数上限
	 * @return
	 */
	public int stockLimit() {
		return config.limit();
	}

	/**
	 * 取り扱い商品一覧（JANコード）
	 * @return
	 */
	public Set<String> products() {
		return config.products().stream().map(ProductInfo::jan).collect(Collectors.toSet());
	}

	public boolean isValidProduct(String jan) {
		return config.isValidProduct(jan);
	}

	/*
	 * 商品毎の情報
	 */

	/**
	 * 消費期限（日数）
	 * @param jan
	 * @return
	 */
	public int expire(String jan) {
		return config.expire(jan);
	}

	/**
	 * 仕入れ値
	 * @param jan
	 * @return
	 */
	public int cost(String jan) {
		return config.cost(jan);
	}

	/**
	 * 定価（シミュレータの動作には関係がない。参考値だが学生は学習用データを見るので重要ではないかも）
	 * @param jan
	 * @return
	 */
	public int defaultPrice(String jan) {
		return config.defaultPrice(jan);
	}


	/**
	 * 閉店
	 * 
	 * @param token
	 * @return 削除の成否
	 */
	public boolean close(int token) {
		ShopOneItem s = shop(token);
		s.close();
		return (shops.remove(token) != null);
	}

	public ShopOneItem shop(int token) {
		return shops.get(token);
	}

	public Set<ShopOneItem> shopSet(String uid) {
		return shops.values().stream().filter(s -> (s.uid().equals(uid))).collect(Collectors.toSet());
	}


//	public Collection<ShopOneItem> shops() {
//		return shops.values();
//	}
//

}
