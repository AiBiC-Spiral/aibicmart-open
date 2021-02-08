package cloud.aibic_spiral.aibicmart.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Stream;

import cloud.aibic_spiral.aibicmart.entity.User;
import gnu.trove.map.hash.THashMap;

/**
 * ユーザ情報の検証。student.csvの1列目をユーザ名、2列目をキーとして検証。
 * @author t-kanda
 *
 */
public class UserAuth {

	private Map<String, String> db;

	public UserAuth() {
		loadDB();
	}

	private void loadDB() {
		db = new THashMap<>();
		try (Stream<String> st = Files.lines(ShopConfig.getResource("student.csv"))) {
			st.map(s -> s.split(",")).forEach(s -> db.put(s[0], s[1]));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ユーザ情報の検証。平文なのでキーはパスワードにしないほうがいい。<br>
	 * （他人のIDで店舗インスタンスを作成してしまうこと防ぐのが主な目的なので、セキュアなキーは使わない。2文字程度でも十分。）
	 * @param u ユーザ情報
	 * @return 検証の成否
	 */
	public boolean validateUser(User u) {
		// return true if you don't need a validation.
		if (!db.containsKey(u.id())) {
			return false;
		}
		return db.get(u.id()).equals(u.key());
	}

}
