package cloud.aibic_spiral.aibicmart.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 店舗インスタンスにアクセスするためのキー。
 * 
 * @author t-kanda
 *
 */
public class Token {

	@JsonProperty
	@Schema(description = "アクセス用トークン")
	private int token;

	@JsonProperty
	@Schema(description = "シミュレーション開始日")
	private String since;

	@JsonProperty
	@Schema(description = "シミュレーション終了日")
	private String until;

	/**
	 * 適当にトークン生成。ログ表示しやすいように切り詰めているけど受講人数に応じて真面目に桁数増やしたほうがよさそう。
	 * 
	 * @param uid
	 */
	public Token(String uid, LocalDate since, LocalDate until) {
		token = Math.abs((uid + LocalDateTime.now().toString()).hashCode() % 100000);
		this.since = since.toString();
		this.until = until.toString();

	}

	public int token() {
		return token;
	}

}
