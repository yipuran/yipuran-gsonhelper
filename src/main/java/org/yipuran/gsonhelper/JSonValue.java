package org.yipuran.gsonhelper;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * JSONキー指定 JsonElement取得.
 * <PRE>
 * JSONキー、ツリー構造のPATHからJsonElementを取得する。
 * ツリー構造のPATHは、"." 区切りでキーのPATHを指定する。配列要素は、[] で指定し n 番目は [n-1]である。
 * JsonElement は、JsonPrimitive、JsonObject、JsonArray の基底クラスであることから取得する JsonElement を
 * 各々の型で使用できる。
 * 使用例
 *    try(InputStream in = new FileInputStream("sample.json");
 *        Reader reader = new InputStreamReader(in, "UTF-8")){
 *        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
 *
 *        JSonValue jsv = new JSonValue(br);
 *        JsonElement e = jsv.get("store.book[2].price");
 * </PRE>
 */
public final class JSonValue{
	private Reader reader;
	private Pattern aryPattern = Pattern.compile("\\[\\d+\\]");
	private Pattern decPattern = Pattern.compile("\\d+");

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
	 * JsonElement取得.
	 * @param path JSON Key path  表現は "."区切り、配列は [Index]で表現
	 * @return JsonElement、 path にマッチしない場合は nullが返る
	 */
	public JsonElement get(String path){
		if (path==null) return null;
		if (path.replaceAll(" ", "").length()==0) return null;
		return get(path, JsonParser.parseReader(reader));
	}
	private JsonElement get(String path, JsonElement je){
		String[] pary = path.split("\\.");
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
}
