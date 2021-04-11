package org.yipuran.gsonhelper.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * JSON読込み key-value 抽出.
 * <PRE>
 * JSONキーは、"."区切り、配列は添え字 [n] で表現、BiConsumer 実行または Streamを生成する
 * </PRE>
 * @since 4.23
 */
public class JsonView{
	/**
	 * コンストラクタ.
	 */
	public JsonView(){
	}
	/**
	 * JSON テキスト→ BiConsumer 実行
	 * @param jsontxt
	 * @param biconsumer JSONキーとJSONキーに位置する値の BiConsumer
	 */
	public void read(String jsontxt, BiConsumer<String, Object> biconsumer){
		parseElement(JsonParser.parseReader(new StringReader(jsontxt)), "", biconsumer);
	}
	/**
	 * JSON テキストを読込める java.io.Reader → BiConsumer 実行
	 * @param reader java.io.Reader
	 * @param biconsumer  JSONキーとJSONキーに位置する値の BiConsumer
	 */
	public void read(Reader reader, BiConsumer<String, Object> biconsumer){
		parseElement(JsonParser.parseReader(reader), "", biconsumer);
	}
	/**
	 * JSON テキストを読込 InputStream → BiConsumer 実行
	 * @param in
	 * @param biconsumer  JSONキーとJSONキーに位置する値の BiConsumer
	 */
	public void read(InputStream in, BiConsumer<String, Object> biconsumer){
		parseElement(JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)), "", biconsumer);
	}
	private void parseElement(JsonElement je, String parent, BiConsumer<String, Object> biconsumer){
		if (je.isJsonObject()) {
			JsonObject jo = (JsonObject)je;
			for(Entry<String, JsonElement> e:jo.entrySet()){
				parseElement(e.getValue(), parent + "." + e.getKey(), biconsumer);
			}
		}else if(je.isJsonArray()){
			JsonArray ary = je.getAsJsonArray();
			int i = 0;
			for(Iterator<JsonElement> it=ary.iterator();it.hasNext();i++){
				parseElement(it.next(), parent + "[" + i + "]", biconsumer);
			}
		}else if(je.isJsonNull()){
			biconsumer.accept(parent.substring(1), null);
		}else if(je.isJsonPrimitive()){
			String path = parent.substring(1);
			JsonPrimitive p = je.getAsJsonPrimitive();
			if (p.isNumber()){
				if (je.toString().indexOf(".") > 0) {
					biconsumer.accept(path, p.getAsDouble());
				}else{
					if (p.getAsLong() <= Integer.MAX_VALUE) {
						biconsumer.accept(path, p.getAsLong());
					}else{
						biconsumer.accept(path, p.getAsInt());
					}
				}
			}else if(p.isBoolean()){
				biconsumer.accept(path, p.getAsBoolean());
			}else if(p.isString()){
				biconsumer.accept(path, p.getAsString());
			}
		}
	}

	/**
	 * JSONキーPredicate検証→JSON テキスト→ BiConsumer 実行
	 * @param jsontxt
	 * @param predicate
	 * @param biconsumer JSONキーとJSONキーに位置する値の BiConsumer
	 */
	public void read(String jsontxt, Predicate<String> predicate, BiConsumer<String, Object> biconsumer){
		parseElement(JsonParser.parseReader(new StringReader(jsontxt)), "", predicate, biconsumer);
	}
	/**
	 * JSONキーPredicate検証→JSON テキストを読込める java.io.Reader → BiConsumer 実行
	 * @param reader java.io.Reader
	 * @param predicate
	 * @param biconsumer  JSONキーとJSONキーに位置する値の BiConsumer
	 */
	public void read(Reader reader, Predicate<String> predicate, BiConsumer<String, Object> biconsumer){
		parseElement(JsonParser.parseReader(reader), "", predicate, biconsumer);
	}
	/**
	 * JSONキーPredicate検証→JSON テキストを読込 InputStream → BiConsumer 実行
	 * @param in
	 * @param predicate
	 * @param biconsumer  JSONキーとJSONキーに位置する値の BiConsumer
	 */
	public void read(InputStream in, Predicate<String> predicate, BiConsumer<String, Object> biconsumer){
		parseElement(JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)), "", predicate, biconsumer);
	}
	private void parseElement(JsonElement je, String parent, Predicate<String> predicate, BiConsumer<String, Object> biconsumer){
		if (je.isJsonObject()) {
			JsonObject jo = (JsonObject)je;
			for(Entry<String, JsonElement> e:jo.entrySet()){
				parseElement(e.getValue(), parent + "." + e.getKey(), biconsumer);
			}
		}else if(je.isJsonArray()){
			JsonArray ary = je.getAsJsonArray();
			int i = 0;
			for(Iterator<JsonElement> it=ary.iterator();it.hasNext();i++){
				parseElement(it.next(), parent + "[" + i + "]", biconsumer);
			}
		}else if(je.isJsonNull()){
			String path = parent.substring(1);
			if (predicate.test(path)){
				biconsumer.accept(path, null);
			}
		}else if(je.isJsonPrimitive()){
			String path = parent.substring(1);
			if (predicate.test(path)){
				JsonPrimitive p = je.getAsJsonPrimitive();
				if (p.isNumber()){
					if (je.toString().indexOf(".") > 0) {
						biconsumer.accept(path, p.getAsDouble());
					}else{
						if (p.getAsLong() <= Integer.MAX_VALUE) {
							biconsumer.accept(path, p.getAsLong());
						}else{
							biconsumer.accept(path, p.getAsInt());
						}
					}
				}else if(p.isBoolean()){
					biconsumer.accept(path, p.getAsBoolean());
				}else if(p.isString()){
					biconsumer.accept(path, p.getAsString());
				}
			}
		}
	}

	/**
	 * JSON テキストを読込 InputStream→JSONキー＆値 EntryのStream生成
	 * @param in
	 * @return JSONキー＆値 EntryのStream生成
	 */
	public Stream<Entry<String, Object>> stream(InputStream in){
	   Stream.Builder<Entry<String, Object>> builder = Stream.builder();
		parseElement(JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)), "", builder);
	   return  builder.build();
	}
	/**
	 * JSON テキスト→JSONキー＆値 EntryのStream生成
	 * @param jsontxt
	 * @return JSONキー＆値 EntryのStream生成
	 */
	public Stream<Entry<String, Object>> stream(String jsontxt){
	   Stream.Builder<Entry<String, Object>> builder = Stream.builder();
		parseElement(JsonParser.parseReader(new StringReader(jsontxt)), "", builder);
	   return  builder.build();
	}
	/**
	 * JSON テキストを読込める java.io.Reader → JSONキー＆値 EntryのStream生成
	 * @param reader java.io.Reader
	 * @return JSONキー＆値 EntryのStream生成
	 */
	public Stream<Entry<String, Object>> stream(Reader reader){
	   Stream.Builder<Entry<String, Object>> builder = Stream.builder();
		parseElement(JsonParser.parseReader(reader), "", builder);
		return  builder.build();
	}

	private void parseElement(JsonElement je, String parent, Stream.Builder<Entry<String, Object>> builder){
		if (je.isJsonObject()) {
			JsonObject jo = (JsonObject)je;
			for(Entry<String, JsonElement> e:jo.entrySet()){
				parseElement(e.getValue(), parent + "." + e.getKey(), builder);
			}
		}else if(je.isJsonArray()){
			JsonArray ary = je.getAsJsonArray();
			int i = 0;
			for(Iterator<JsonElement> it=ary.iterator();it.hasNext();i++){
				parseElement(it.next(), parent + "[" + i + "]", builder);
			}
		}else if(je.isJsonNull()){
			builder.add(new SimpleEntry<String, Object>(parent.substring(1), null));
		}else if(je.isJsonPrimitive()){
			String path = parent.substring(1);
			JsonPrimitive p = je.getAsJsonPrimitive();
			if (p.isNumber()){
				if (je.toString().indexOf(".") > 0) {
					builder.add(new SimpleEntry<String, Object>(path, p.getAsDouble()));
				}else{
					if (p.getAsLong() <= Integer.MAX_VALUE) {
						builder.add(new SimpleEntry<String, Object>(path, p.getAsLong()));
					}else{
						builder.add(new SimpleEntry<String, Object>(path, p.getAsInt()));
					}
				}
			}else if(p.isBoolean()){
				builder.add(new SimpleEntry<String, Object>(path, p.getAsBoolean()));
			}else if(p.isString()){
				builder.add(new SimpleEntry<String, Object>(path, p.getAsString()));
			}
		}
	}
}
