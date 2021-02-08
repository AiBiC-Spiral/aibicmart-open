package cloud.aibic_spiral.aibicmart.controller.multiitem;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import cloud.aibic_spiral.aibicmart.controller.ShopConfig;
import cloud.aibic_spiral.aibicmart.controller.UserAuth;
import cloud.aibic_spiral.aibicmart.entity.ProductInfo;
import cloud.aibic_spiral.aibicmart.entity.Token;
import cloud.aibic_spiral.aibicmart.entity.User;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * 
 * 個々の店舗を統括するクラス。 要求に応じて新装開店とトークン払い出し、店舗閉鎖。
 * 
 * @author t-kanda
 *
 */
public class ShopManager {

	/**
	 * インスタンスはひとつです
	 */
	private static ShopManager shopManager;

	private ShopConfig config;
	private UserAuth users;

	/**
	 * Map<Token, Shop>
	 */
	private TIntObjectMap<Shop> shops;

	public void loadConfig() {
		shops = new TIntObjectHashMap<>();
		config = ShopConfig.loadConfig();
		users = new UserAuth();
	}	
	
	public ShopManager() {
		loadConfig();
	}

	/**
	 * 単豚
	 */
	public static ShopManager getInstance() {
		if (shopManager == null) {
			shopManager = new ShopManager();
		}
		return shopManager;
	}
	
	/**
	 * 店舗インスタンスを初期化する（班番号が必要）
	 * 
	 * @param uid
	 * @return
	 */
	public Token init(User u, LocalDate since, LocalDate until) {
		if (!users.validateUser(u)) {
			return null;
		}
		String uid = u.id();
		// その学籍番号の店は閉じる。
		shopSet(uid).stream().forEach(s -> shops.remove(s.token()));
		if (since.isBefore(simulateStartLimit())) {
			since = simulateStartLimit();
		}
		if (until.isAfter(simulateEndLimit())) {
			until = simulateEndLimit();
		}
		if(since.isAfter(until)){
			return null;
		}
		Token t = new Token(uid, since, until);
		if (shops.containsKey(t.token())) {
			return null;
		}
		shops.put(t.token(), new Shop(uid, t.token(), since, until));
		return t;
	}

	public LocalDate simulateStartLimit() {
		return config.simulateStart();
	}

	public LocalDate simulateEndLimit() {
		return config.simulateEnd();
	}

	public int stockLimit() {
		return config.limit();
	}

	/**
	 * 閉店
	 * 
	 * @param token
	 * @return 削除の成否
	 */
	public boolean close(int token) {
		Shop s = shops.get(token);
		s.close();
		return (shops.remove(token) != null);
	}

	public Shop shop(int token) {
		return shops.get(token);
	}

	public Set<Shop> shopSet(String uid) {
		return shops.valueCollection().stream().filter(s -> (s.uid().equals(uid))).collect(Collectors.toSet());
	}

	public Set<String> products() {
		return config.products().stream().map(ProductInfo::jan).collect(Collectors.toSet());
	}

	public int expire(String jan) {
		return config.expire(jan);
	}

	public int cost(String jan) {
		return config.cost(jan);
	}

	public Collection<Shop> shops() {
		return shops.valueCollection();
	}

	public int defaultPrice(String jan) {
		return config.defaultPrice(jan);
	}

	public boolean isValidProduct(String jan) {
		return config.isValidProduct(jan);
	}

}
