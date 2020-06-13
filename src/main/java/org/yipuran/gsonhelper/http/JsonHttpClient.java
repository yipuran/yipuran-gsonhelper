package org.yipuran.gsonhelper.http;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.gson.stream.JsonReader;

/**
 * HTTP JSON クライアント.
 *  <PRE>
 * （使用例）
 * String path;  // 送信先URL
 * GsonBuilder gsonbuilder = new Gsonbuilder().serializeNulls();
 * JsonHttpClient client = JsonHttpClientBuilder.of(path, gsonbuilder).build();
 *
 * // (1) 受信→整形していないJSON
 * int status = client.execute(object, (type, hmap)->{
 * 	// type = Content-type
 *    // hmap = HTTPヘッダマップ
 * }, body->{
 *    // body = HTTP受信 contents 文字列
 * });
 *
 * // (2) 受信→整形したJSON
 * int status = client.executePretty(object, (type, hmap)->{
 * 	// type = Content-type
 *    // hmap = HTTPヘッダマップ
 * }, s->{
 *    // s = 整形したJSON
 * });
 *
 * // (3) 受信→未整形JSONと整形JSON を別々のConsumerで処理
 * int status = client.execute(object, (type, hmap)->{
 * 	// type = Content-type
 *    // hmap = HTTPヘッダマップ
 * }, body->{
 *    // body = HTTP受信 contents 文字列
 * }, s->{
 *    // s = 整形したJSON
 * });
 *
 * // (4) 受信→未整形JSONと整形JSON を BiConsumer＜未整形JSON, 整形JSON＞で処理
 * int status = client.execute(object, (type, hmap)->{
 * 	// type = Content-type
 *    // hmap = HTTPヘッダマップ
 * }, (b, s)->{
 *    // b = 未整形JSON
 *    // s = 整形したJSON
 * });
 *
 * // (5) JsonReader で受信
 * int status = client.execute(object, (type, hmap)->{
 * 	// type = Content-type
 *    // hmap = HTTPヘッダマップ
 * }, jr->{
 *    // jr = JsonReaderN
 * });
 *
 * （注意）(2)～(4) は、送受信前に Gsonbuilderは、registerTypeAdapter で GenericMapDeserializer が登録されて、setPrettyPrinting() が実行される。
 *  JsonHttpClientBuilder は、.build() 実行前に、setForceUtf8()を実行することで、
 *  生成されるJsonHttpClient の受信JSON出力を強制的にUTF-8変換して出力する  JsonHttpClient for UTF8 にすることができる
 *  </PRE>
 */
public interface JsonHttpClient{
	/**
	 * 受信→整形していないJSON
	 * @param object JSONにして送信する対象
	 * @param headconsumer Content-typeとHTTPヘッダ受信マップのBiConsumer
	 * @param bodyconsumer 受信したJSON文字列
	 */
	public void execute(Object object,BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<String> bodyconsumer);

	/**
	 * 受信→整形したJSON
	 * <PRE>
	 * （注意）送受信前に、Gsonbuilderは、registerTypeAdapter で GenericMapDeserializer が登録されて、setPrettyPrinting() が実行される。
	 * </PRE>
	 * @param object JSONにして送信する対象
	 * @param headconsumer Content-typeとHTTPヘッダ受信マップのBiConsumer
	 * @param jsonconsumer 受信→整形したJSONの Consumer
	 */
	public void executePretty(Object object,BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<String> jsonconsumer);

	/**
	 * 受信→未整形JSONと整形JSON
	 * <PRE>
	 * （注意）送受信前に、Gsonbuilderは、registerTypeAdapter で GenericMapDeserializer が登録されて、setPrettyPrinting() が実行される。
	 * </PRE>
	 * @param object JSONにして送信する対象
	 * @param headconsumer Content-typeとHTTPヘッダ受信マップのBiConsumer
	 * @param bodyconsumer 受信したJSON文字列
	 * @param jsonconsumer 受信→整形したJSONの Consumer
	 */
	public void execute(Object object,BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<String> bodyconsumer, Consumer<String> jsonconsumer);

	/**
	 * 受信→未整形JSONと整形JSON を BiConsumer＜未整形JSON, 整形JSON＞で処理.
	 * <PRE>
	 * （注意）送受信前に、Gsonbuilderは、registerTypeAdapter で GenericMapDeserializer が登録されて、setPrettyPrinting() が実行される。
	 * </PRE>
	 * @param object JSONにして送信する対象
	 * @param headconsumer Content-typeとHTTPヘッダ受信マップのBiConsumer
	 * @param consumer BiConsumer＜未整形JSON, 整形JSON＞
	 */
	public void execute(Object object,BiConsumer<String, Map<String, List<String>>> headconsumer, BiConsumer<String, String> consumer);

	/**
	 * JsonReader で受信
	 * @param object JSONにして送信する対象
	 * @param headconsumer Content-typeとHTTPヘッダ受信マップのBiConsumer
	 * @param jsonreadconsumer JsonReader
	 * @return HTTPステータス
	 */
	public int executeJsonread(Object object,BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<JsonReader> jsonreadconsumer);

	/**
	 * HTTPレスポンスコード取得.
	 * @return HTTPレスポンスコード
	 */
	public int getHttpresponsecode();
}
