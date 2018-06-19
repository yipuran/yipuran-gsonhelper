package org.yipuran.gsonhelper;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * JSONキーPATHによるJsonElement抽出.
 * <PRE>
 * "." ドットで区切った JSON キーのパスを指定して Optional&lt;JsonElement&gt; を求める関数型インターフェース
 * JSON キーは、半角英数字と "_" アンダースコア文字だけの約束とする。
 * キーにマッチしない場合、Optional.empty() を取得することになる。
 * （使用例）
 *  JSON ファイル test.json の中身
 *      { "a":{ "b":{ "c": 2345 , "num":[0,1,2] } } }
 *
 *  try(InputStream in = new FileInputStream("test.json");
 *       Reader reader = new InputStreamReader(in, "UTF-8")){
 *     JsonElement je = new JsonParser().parse(reader);
 *     JsonPathSearch js = JsonPathSearch.of(je->{
 *       je->je.ifPresent(e->{
 *           System.out.println( e.getAsInt() );
 *       });
 *     });
 *     js.compute("a.b.c", je);
 *  }catch(IOException e){
 *    e.printStackTrace();
 *  }
 *  または、
 *     JsonPathSearch.of(je->{
 *        je.ifPresent(e->{
 *           e.getAsJsonArray().forEach(t->{
 *              System.out.println( t.getAsInt() );
 *           });
 *        });
 *     }).compute("a.b.num", reader);
 * </PRE>
 */
@FunctionalInterface
public interface JsonPathSearch extends Serializable{
	void accept(Optional<JsonElement> je);

	/**
	 * Optional&lt;JsonElement&gt; コンシューマを設定してインスタンス生成.
	 * @param consumer Optional&lt;JsonElement&gt; コンシューマ
	 * @return JsonPathSearch
	 */
	public static JsonPathSearch of(Consumer<Optional<JsonElement>> consumer){
		return e->consumer.accept(e);
	}
	/**
	 * JSON解析実行（JsonElement指定）.
	 * @param key "." ドットで区切った JSON キーのパス
	 * @param je JsonElement
	 */
	default void compute(String key, JsonElement je){
		accept(elementParse(je, key));
	}
	/**
	 * JSON解析実行（java.io.Reader指定）.
	 * @param key "." ドットで区切った JSON キーのパス
	 * @param reader java.io.Reader
	 */
	default void compute(String key, Reader reader){
		accept(elementParse(new JsonParser().parse(reader), key));
	}
	/**
	 * JSON解析実行（JSON文字列指定）.
	 * @param key "." ドットで区切った JSON キーのパス
	 * @param jsonstring JSON文字列
	 */
	default void compute(String key, String jsonstring){
		accept(elementParse(new JsonParser().parse(new StringReader(jsonstring==null ? "" : jsonstring)), key));
	}
	static Optional<JsonElement> elementParse(JsonElement je, String key){
		if (!je.isJsonObject()) return Optional.empty();
		if (key.contains(".")){
			String[] sp = key.split("\\.");
			if (sp.length > 1){
				return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
						je.getAsJsonObject().entrySet().iterator(), Spliterator.ORDERED ), false)
					.filter(e->e.getKey().equals(sp[0])).findAny()
					.flatMap(e->elementParse(e.getValue(), key.replaceFirst("^[\\w_]+\\.", "")));
			}else{
				return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
						je.getAsJsonObject().entrySet().iterator(), Spliterator.ORDERED ), false)
					.filter(e->e.getKey().equals(sp[0])).findAny().map(e->e.getValue());
			}
		}
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
				je.getAsJsonObject().entrySet().iterator(), Spliterator.ORDERED ), false)
			.filter(e->e.getKey().equals(key)).findAny().map(e->e.getValue());
	}
}
