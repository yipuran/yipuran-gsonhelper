package org.yipuran.gsonhelper;

import java.util.Arrays;

/**
 * JSONエラータイプ.
 * @since 4.8
 */
public enum JsonErrorType {
	/**
	 * Unterminated object : value表現エラー  例） "date" : 2019/12/03
	 *  "date" : 201-12-03 はこのエラーにならない。
	 */
	UnterminatedObject("Unterminated object"),
	/**
	 * Unterminated String : value表現エラー(文字列を期待されてるのに文字列認識できない）  例） "a": '
	 */
	UnterminatedString("Unterminated String"),

	/**
	 * Unterminated array : value表現エラー(配列を期待されてるのに、配列認識できない）  例） "a": ["1":12]
	 */
	UnterminatedArray("Unterminated array"),

	/**
	 * Expected name: キーの表現が解釈できない（想定できない）
	 * <PRE>
	 * 例）{} : 2
	 *        : 2
	 * </PRE>
	 */
	ExpectedName("Expected name"),

	/**
	 * JSON key-value 区切り文字が ':' でない
	 */
	SeparatorError("Expected ':'"),

	/**
	 * Expected value : value 表現解釈できない（想定できない）
	 * <PRE>
	 * 　例）　"data": /03
	 * </PRE>
	 */
	ExpectedValue("Expected value"),

	/**
	 * Unexpected value : 想定外の value
	 * 　例） "s2":  ,
	 */
	UnexpectedValue("Unexpected value");

	private String detail;
	private JsonErrorType(String detail){
		this.detail = detail;
	}
	/**
	 * MalformedJsonException エラーメッセージ.
	 * @return  MalformedJsonException エラーメッセージ
	 */
	public String getDetail(){
		return detail;
	}

	/**
	 * Exception エラーメッセージ → JsonErrorType特定.
	 * @param message Exception エラーメッセージ
	 * @return JsonErrorType
	 */
	public static JsonErrorType parse(String message){
		return Arrays.stream(values()).filter(e->message.startsWith(e.detail)).findAny().orElse(null);
	}
}
