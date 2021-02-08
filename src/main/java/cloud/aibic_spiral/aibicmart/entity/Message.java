package cloud.aibic_spiral.aibicmart.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ステータスコードとともに返すテキストメッセージ
 * @author t-kanda
 *
 */
public class Message {

	@JsonProperty
	private String message;

	public Message(String message) {
		this.message = message;
	}

}
