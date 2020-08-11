package org.yipuran.gsonhelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

/**
 * JSONキー指定 JsonElement取得.
 * <PRE>
 * JSONキー、ツリー構造のPATHからJsonElementを取得する。
 * ツリー構造のPATHは、"." 区切りでキーのPATHを指定する。配列要素は、[] で指定し n 番目は [n-1]である。
 * JsonElement は、JsonPrimitive、JsonObject、JsonArray の基底クラスであることから取得する JsonElement を
 * 各々の型で使用できる。
 * JSON PATHセパレータは、"."以外に変えることもできる。
 * setJsonPathSeparator(String separator)で変更する。
 *
 * （注意）抽出の処理、get メソッド、print メソッドは、１回実行したら、２度目はそのまま使用できず、
 * インスタンスを再生しなおさないとならない。
 * これは、JSON を読込んで実行するからである。
 *
 * 使用例
 *    try(InputStream in = new FileInputStream("sample.json");
 *        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)){
 *        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
 *
 *        JSonValue jsv = new JSonValue(br);
 *        JsonElement e = jsv.get("store.book[2].price");
 *
 *        // 注意、get . print は１回実行したら、２度は使えない。
 *        String s = print("store.book[2].price2);
 *
 * </PRE>
 */
public final class JSonValue{
	private Reader reader;
	private Pattern aryPattern = Pattern.compile("\\[\\d+\\]");
	private Pattern decPattern = Pattern.compile("\\d+");
	private String separator = "\\.";

	/**
	 * コンストラクタ.
	 * @param reader Reader
	 */
	public JSonValue(Reader reader){
		this.reader = reader;
	}
	/**
	 * コンストラクタ.
	 * @param json JSON strring
	 */
	public JSonValue(String json){
		this.reader = new StringReader(json);
	}
	/**
	 * JSON path セパレータ の変更.
	 * デフォルトのセパレータ "." を他の文字に変更する
	 * @param separator セパレータ
	 */
	public void setJsonPathSeparator(String separator){
		this.separator = separator;
	}
	/**
	 * JsonElement取得.
	 * @param path JSON Key path  表現は "."区切り、配列は [Index]で表現
	 * @return JsonElement、 path にマッチしない場合は nullが返る
	 */
	public JsonElement get(String path){
		if (path==null) return null;
		if (path.replaceAll(" ", "").length()==0) return null;
		String _path = path.replaceFirst("\\$" + separator, "");
		_path = _path.replaceFirst("^" + separator, "");
		return get(_path, JsonParser.parseReader(reader));
	}
	private JsonElement get(String path, JsonElement je){
		String[] pary = path.split(separator);
		String key = pary[0].replaceAll("\\[\\d+\\]", "");
		if (je.isJsonNull()){
			return null;
		}else if(je.isJsonObject()){
			for(Entry<String, JsonElement> entry:je.getAsJsonObject().entrySet()){
				if (entry.getKey().equals(key)){
					if (entry.getValue().isJsonArray() && aryPattern.matcher(pary[0]).find()){
						int i = getIndex(pary[0]);
						JsonArray ja = entry.getValue().getAsJsonArray();
						if (i < ja.size()){
							if (pary.length > 1) {
								return get(Arrays.stream(pary).skip(1).collect(Collectors.joining(".")), ja.get(i));
							}
							return ja.get(i);
						}
						return null;
					}
					if (pary.length > 1) {
						return get(Arrays.stream(pary).skip(1).collect(Collectors.joining(".")), entry.getValue());
					}
					return entry.getValue();
				}
			}
		}else if(je.isJsonPrimitive()){
			return je;
		}
		return null;
	}
	private int getIndex(String s){
		Matcher m = aryPattern.matcher(s);
		if (m.find()){
			Matcher md = decPattern.matcher(m.group());
			if (md.find()){
				return Integer.parseInt(md.group());
			}
		}
		return -1;
	}
	/**
	 * JSON-Path→抽出String
	 * @param path JSON-PATH
	 * @return
	 * @since 4.20
	 */
	public String print(String path) {
		JsonElement je = get(path);
		String rtn = null;
		if (je==null) return null;
		try(ByteArrayOutputStream bo = new ByteArrayOutputStream();
				JsonWriter writer = new JsonWriter(new PrintWriter(bo))){
			Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
			gson.toJson(je, writer);
			writer.flush();
			rtn = bo.toString();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		return rtn;
	}
	/**
	 * JSON-Path→抽出String インデント文字指定
	 * @param path JSON-PATH
	 * @param indent
	 * @return
	 * @since 4.20
	 */
	public String print(String path, String indent) {
		JsonElement je = get(path);
		String rtn = null;
		if (je==null) return null;
		try(ByteArrayOutputStream bo = new ByteArrayOutputStream();
			JsonWriter writer = new JsonWriter(new PrintWriter(bo))){
			if (indent != null) writer.setIndent(indent);
			Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
			gson.toJson(je, writer);
			writer.flush();
			rtn = bo.toString();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		return rtn;
	}
	/**
	 * JSON-Path→抽出OutputStreamへ書き出す
	 * @param path
	 * @param out OutputStream
	 * @since 4.20
	 */
	public void print(String path, OutputStream out) {
		try(JsonWriter writer = new JsonWriter(new PrintWriter(out))){
			Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
			JsonElement je = get(path);
			gson.toJson(je, writer);
			writer.flush();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	/**
	 * JSON-Path→抽出OutputStreamへ書き出す（インデント文字指定）
	 * @param path JSON-PATH
	 * @param out OutputStream
	 * @param indent
	 * @since 4.20
	 */
	public void print(String path, OutputStream out, String indent) {
		try(JsonWriter writer = new JsonWriter(new PrintWriter(out));){
			if (indent != null) writer.setIndent(indent);
			Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
			JsonElement je = get(path);
			gson.toJson(je, writer);
			writer.flush();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
}
