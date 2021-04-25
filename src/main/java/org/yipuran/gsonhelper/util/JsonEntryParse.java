package org.yipuran.gsonhelper.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
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
 * addDeserilaize で、デシリアライザ登録が可能
 * </PRE>
 * @since 4.24
 */
public class JsonEntryParse{
	private Map<String, Function<JsonElement, Object>> dMap;
	/**
	 * コンストラクタ.
	 */
	public JsonEntryParse(){
		dMap = new HashMap<>();
	}
	/**
	 * 指定JSONキーPattern → JsonElement デシリアライザ登録
	 * @param ptn JSONキーPattern
	 * @param deserial JsonElement デシリアライズ Function
	 * @return JsonEntryParse
	 */
	public JsonEntryParse addDeserilaize(Pattern ptn, Function<JsonElement, Object> deserial) {
		dMap.put(ptn.pattern(), deserial);
		return this;
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
		String p = parent.length() > 0 ? parent.substring(1) : parent;
		Function<JsonElement, Object> nodeparser;
		if (dMap.size() > 0 && (nodeparser = dMap.entrySet().stream()
				.filter(e->Pattern.compile(e.getKey()).matcher(p).find())
				.findAny().map(e->e.getValue()).orElse(null)) != null) {
			biconsumer.accept(p, nodeparser.apply(je));
		}else{
			if (je.isJsonObject()) {
				JsonObject jo = (JsonObject)je;
				for(Entry<String, JsonElement> e:jo.entrySet()){
					parseElement(e.getValue(), parent + "." + e.getKey(), biconsumer);
				}
			}else if(je.isJsonArray()){
				JsonArray ary = je.getAsJsonArray();
				if (ary.size() > 0) {
					int i = 0;
					for(Iterator<JsonElement> it=ary.iterator();it.hasNext();i++){
						parseElement(it.next(), parent + "[" + i + "]", biconsumer);
					}
				}else{
					biconsumer.accept(parent.substring(1), new ArrayList<Object>());
				}
			}else if(je.isJsonNull()){
				biconsumer.accept(parent.substring(1), null);
			}else if(je.isJsonPrimitive()){
				String path = parent.substring(1);
				JsonPrimitive ptv = je.getAsJsonPrimitive();
				if (ptv.isNumber()){
					if (je.toString().indexOf(".") > 0) {
						biconsumer.accept(path, ptv.getAsDouble());
					}else{
						if (ptv.getAsLong() <= Integer.MAX_VALUE) {
							biconsumer.accept(path, ptv.getAsLong());
						}else{
							biconsumer.accept(path, ptv.getAsInt());
						}
					}
				}else if(ptv.isBoolean()){
					biconsumer.accept(path, ptv.getAsBoolean());
				}else if(ptv.isString()){
					biconsumer.accept(path, ptv.getAsString());
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
		String p = parent.length() > 0 ? parent.substring(1) : parent;
		Function<JsonElement, Object> nodeparser;
		if (dMap.size() > 0 && (nodeparser = dMap.entrySet().stream()
				.filter(e->Pattern.compile(e.getKey()).matcher(p).find())
				.findAny().map(e->e.getValue()).orElse(null)) != null) {
			builder.add(new SimpleEntry<String, Object>(p, nodeparser.apply(je)));
		}else{
			if (je.isJsonObject()) {
				JsonObject jo = (JsonObject)je;
				for(Entry<String, JsonElement> e:jo.entrySet()){
					parseElement(e.getValue(), parent + "." + e.getKey(), builder);
				}
			}else if(je.isJsonArray()){
				JsonArray ary = je.getAsJsonArray();
				if (ary.size() > 0) {
					int i = 0;
					for(Iterator<JsonElement> it=ary.iterator();it.hasNext();i++){
						parseElement(it.next(), parent + "[" + i + "]", builder);
					}
				}else{
					builder.add(new SimpleEntry<String, Object>(parent.substring(1), new ArrayList<Object>()));
				}
			}else if(je.isJsonNull()){
				builder.add(new SimpleEntry<String, Object>(parent.substring(1), null));
			}else if(je.isJsonPrimitive()){
				String path = parent.substring(1);
				JsonPrimitive ptv = je.getAsJsonPrimitive();
				if (ptv.isNumber()){
					if (je.toString().indexOf(".") > 0) {
						builder.add(new SimpleEntry<String, Object>(path, ptv.getAsDouble()));
					}else{
						if (ptv.getAsLong() <= Integer.MAX_VALUE) {
							builder.add(new SimpleEntry<String, Object>(path, ptv.getAsLong()));
						}else{
							builder.add(new SimpleEntry<String, Object>(path, ptv.getAsInt()));
						}
					}
				}else if(ptv.isBoolean()){
					builder.add(new SimpleEntry<String, Object>(path, ptv.getAsBoolean()));
				}else if(ptv.isString()){
					builder.add(new SimpleEntry<String, Object>(path, ptv.getAsString()));
				}
			}
		}
	}
}
