package cloud.aibic_spiral.aibicmart.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ユーザ情報（ユーザ名とシミュレータアクセスのためのキー）
 * @author t-kanda
 *
 */
public class User {

	@JsonProperty
	@Schema(description = "ユーザ名")
	private String id;

	@JsonProperty
	@Schema(description = "シミュレータ動作用のキー（他人のIDで店舗インスタンスを作成してしまうこと防ぐのが主な目的なので、セキュアなキーは使わない。2文字程度でも十分。）")
	private String key;

	public String id() {
		return id;
	}

	public String key() {
		return key;
	}
}
