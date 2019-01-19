package org.yipuran.gsonhelper;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * JsonPattern：JSON書式判定.
 * <PRE>
 * 書式として扱う JSON素材に対して任意のJSONが、書式のJSONを満たす key を持っているか判定するクラス
 * 使い方：
 * // インスタンス生成は、書式JSON を String で渡すか、java.io.Reader で渡す。
 * JsonPattern pattern = new JsonPattern("{ a:1, b:'name', c:false, d:{ e:'E' }");
 *
 * // 書式チェックバリデーション
 * boolean b = pattern.validate("{ a:0, b:'beta', c:true, d:{ e:'', ee:4 }, f:2");
 *  →  b is false
 * boolean b = pattern.validate("{ a:0, c:true, d:{ ee:4 }, f:2");
 *  →  b is true
 *
 * // 不一致のキーと JsonType（enum定義）の Entryリストを参照
 * List&lt;Entry&lt;String, JsonType&gt;&gt; list = pattern.unmatches();
 *
 *    Entryのキーは、階層を ":" で連結した表現、"{ A:{ B:2 } }" の A の下の B は、 "A:B" と表現する。
 *    JsonType（enum定義）は、以下のとおり定義されている。
 *       public enum JsonType{
 *          STRING, NUMBER, BOOLEAN, ARRAY, OBJECT, NULL;
 *       }
 *
 * // 一致のキーと JsonType（enum定義）の Entryリストを参照
 * List&lt;Entry&lt;String, JsonType&gt;&gt; list = pattern.matches();
 *
 * // 指定JSON書式に存在しない、キーと JsonType（enum定義）の Entryリストを参照
 * List&lt;Entry&lt;String, JsonType&gt;&gt; list = pattern.ignores();
 *
 * // 指定JSON書式のキーと JsonType（enum定義）の Mapを参照
 * Map&lt;String, JsonType&gt; map = pattern.getPatternMap();
 *
 *	// 書式に従ったサンプルJSON の生成
 * String sample = pattern.format();
 *     生成されるJSON は、JsonType（enum定義）に沿って以下の default値（value）で生成される。
 *        JsonType.STRING   →  "" 空文字
 *        JsonType.NUMBER   →  0
 *        JsonType.BOOLEAN  →  false
 *        JsonType.ARRAY    →  []  空の 配列、但し中身がある場合は展開生成される
 *        JsonType.OBJET    →  {}  空の Object、但し中身がある場合は展開生成される
 *        JsonType.NULL     →  null
 * 生成されるJSON文字列は。Google gson の setPrettyPrinting() で整形した文字列である。
 *
 * 生成する対象のJsonType プリミティブ型については、上記の default値ではなく
 * JsonPattern インスタンスを返す setメソッドで変更することができる。
 * → setDefaultBoolean(boolean), setDefaultString(String), setDefaultNumber(Number) メソッド
 * をチェイン（連結）して使用する。
 *        （例）JsonType.BOOLEAN に対して、<b>true</b> 値、
 *              JsonType.NUMBER  に対して、2.75 （小数点）
 *              JsonType.STRING  に対して、"_"
 *          で、format() で JSON文字列を生成する場合、、
 *          JsonPattern pattern = new JsonPattern(reader)
 *                               .setDefaultBoolean(true)
 *                               .setsetDefaultNumber(2.75)
 *                               .setDefaultString("_");
 *          String sample = pattern.format();
 * </PRE>
 */
public final class JsonPattern{
	private Map<String, JsonType> vmap;
	private Map<String, Boolean> cmap;  // true:OK
	private Map<String, JsonType> ignoremap;
	private JsonElement readelement;

	/**
	 * コンストラクタ.
	 * @param jsonstr 書式パターンとして指定するJSON文字列
	 */
	public JsonPattern(String jsonstr){
		this(new StringReader(jsonstr));
	}
	/**
	 * コンストラクタ.
	 * @param reader 書式パターン読込みの java.io.Reader
	 */
	public JsonPattern(Reader reader){
		vmap = new HashMap<>();
		cmap = new HashMap<>();
		ignoremap = new HashMap<>();
		readelement = new JsonParser().parse(reader);
		scan_format(null, readelement);
	}

	private void scan_format(String k, JsonElement je){
		JsonObject jo = je.getAsJsonObject();
		if (jo.entrySet().size()==0){
			vmap.put(k, JsonType.OBJECT);
			cmap.put(k, false);
			return;
		}
		String pkey = Optional.ofNullable(k).map(e->e + ":").orElse("");
		for(Map.Entry<String, JsonElement> entry : jo.entrySet()){
			String jk = entry.getKey();
			String key = pkey + jk;
			JsonElement element = entry.getValue();
			if (element.isJsonNull()){
				vmap.put(key, JsonType.NULL);
				cmap.put(key, false);
				continue;
			}
			if (element.isJsonArray()){
				vmap.put(key, JsonType.ARRAY);
				cmap.put(key, false);
				element.getAsJsonArray().forEach(e->{
					if (e.isJsonObject()) scan_format(key, e);
				});
			}else{
				if (element.isJsonPrimitive()){
					vmap.put(key, primitiveType(element.getAsJsonPrimitive()));
					cmap.put(key, false);
				}else{
					scan_format(key, element);
				}
			}
		}
	}
	private JsonType primitiveType(JsonPrimitive jp){
		if (jp.isBoolean()) return JsonType.BOOLEAN;
		if (jp.isNumber()) return JsonType.NUMBER;
		return JsonType.STRING;
	}
	/**
	 * JSONタイプ enum.
	 */
	public enum JsonType{
		STRING, NUMBER, BOOLEAN, ARRAY, OBJECT, NULL;
	}

	/**
	 * バリデーションチェック（String）,
	 * @param jsonstr 検査対象のJSON文字列
	 * @return true = 不一致、書式JSONに存在するが、検査対象JSONに存在しないキー：value がある。
	 */
	public boolean validate(String jsonstr){
		return validate(new StringReader(jsonstr));
	}
	/**
	 * バリデーションチェック（Reader）,
	 * @param reader 検査対象のJSON文字列を読込む java.io.Reader
	 * @return true = 不一致、書式JSONに存在するが、検査対象JSONに存在しないキー：value がある。
	 */
	public boolean validate(Reader reader){
		ignoremap.clear();
		vmap.keySet().stream().forEachOrdered(key->cmap.put(key, false));
		try{
			JsonElement je = new JsonParser().parse(reader);
			scan_validate(null, je);
			return cmap.values().stream().anyMatch(e->e.booleanValue()==false);
		}catch(Exception ex){
			ex.printStackTrace();
			return true;
		}
	}
	private void scan_validate(String k, JsonElement je){
		JsonObject jo = je.getAsJsonObject();
		if (jo.entrySet().size()==0){
			cmap.put(k, false);
			if (vmap.containsKey(k)){
				cmap.put(k, true);
			}
			return;
		}
		String pkey = Optional.ofNullable(k).map(e->e + ":").orElse("");
		for(Map.Entry<String, JsonElement> entry : jo.entrySet()){
			String key = pkey + entry.getKey();
			JsonElement element = entry.getValue();
			JsonType type = vmap.get(key);
			if (element.isJsonNull()){
				if (type==null){
					ignoremap.put(key, JsonType.NULL);
					continue;
				}
				if (type.equals(JsonType.STRING) || type.equals(JsonType.NUMBER) || type.equals(JsonType.NULL)){
					cmap.put(key, true);
				}
				continue;
			}
			if (element.isJsonArray()){
				if (type==null){
					ignoremap.put(key, JsonType.ARRAY);
				}else if(type.equals(JsonType.ARRAY)){
					cmap.put(key, true);
				}
				element.getAsJsonArray().forEach(e->{
					if (e.isJsonObject()){
						scan_validate(key , e);
					}
				});
			}else{
				if (element.isJsonPrimitive()){
					JsonType etype = primitiveType(element.getAsJsonPrimitive());
					if (type==null){
						ignoremap.put(key, etype);
					}else	if(type.equals(etype)){
						cmap.put(key, true);
					}
				}else{
					cmap.put(key, true);
					scan_validate(key, element);
				}
			}
		}
	}
	/**
	 * 不一致のキーと JsonType（enum定義）の Entryリストを参照
	 * @return Entryのキーは、階層を ":" で連結した表現、
	 */
	public List<Entry<String, JsonType>> unmatches(){
		return vmap.entrySet().stream()
		.filter(e->cmap.get(e.getKey()).booleanValue()==false)
		.collect(Collectors.toList());
	}
	/**
	 * 一致のキーと JsonType（enum定義）の Entryリストを参照
	 * @return Entryのキーは、階層を ":" で連結した表現、
	 */
	public List<Entry<String, JsonType>> matches(){
		return vmap.entrySet().stream()
		.filter(e->cmap.get(e.getKey()).booleanValue()==true)
		.collect(Collectors.toList());
	}
	/**
	 * 無視されたキーと JsonType（enum定義）の Entryリストを参照
	 * @return Entryのキーは、階層を ":" で連結した表現、
	 */
	public List<Entry<String, JsonType>> ignores(){
		return ignoremap.entrySet().stream().collect(Collectors.toList());
	}
	/**
	 * 指定JSON書式のキーと JsonType（enum定義）の Mapを参照
	 * @return Map&lt;String, JsonType&gt;
	 */
	public Map<String, JsonType> getPatternMap(){
		return vmap;
	}
	/**
	 * 書式に従ったサンプルJSON の生成.
	 * <PRE>
	 * 生成されるJSON は、JsonType（enum定義）に沿って以下の default値（value）で生成される。
	 * この default値は、setDefaultBoolean(boolean), setDefaultString(String), setDefaultNumber(Number) メソッドで
	 * 変更して使用することができる。
	 *        JsonType.STRING   →  "" 空文字
	 *        JsonType.NUMBER   →  0
	 *        JsonType.BOOLEAN  →  false
	 *        JsonType.ARRAY    →  []  空の 配列、但し中身がある場合は展開生成される
	 *        JsonType.OBJET    →  {}  空の Object、但し中身がある場合は展開生成される
	 *        JsonType.NULL     →  null
	 * 生成されるJSON文字列は。Google gson の setPrettyPrinting() で整形した文字列である。
	 * </PRE>
	 * @return サンプルJSON
	 */
	public String format(){
		JsonObject jo = makejson(null, readelement, new JsonObject());
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		return gson.toJson(jo);
	}
	private boolean default_boolean = false;
	private String default_string = "";
	private Number default_number = 0;

	/**
	 * format JSON生成 Boolean 値のセット.
	 * @param default_boolean
	 * @return JsonPattern
	 */
	public JsonPattern setDefaultBoolean(boolean default_boolean){
		this.default_boolean = default_boolean;
		return this;
	}
	/**
	 * format JSON生成 String 値のセット.
	 * @param default_string
	 * @return JsonPattern
	 */
	public JsonPattern setDefaultString(String default_string){
		this.default_string = default_string;
		return this;
	}
	/**
	 * format JSON生成 数値のセット.
	 * @param default_number Number型、整数、小数点を設定できる
	 * @return JsonPattern
	 */
	public JsonPattern setDefaultNumber(Number default_number){
		this.default_number = default_number;
		return this;
	}

	private JsonObject makejson(String k, JsonElement je, JsonObject job){
		JsonObject jo = je.getAsJsonObject();
		if (jo.entrySet().size()==0){
			job.add(k, new JsonObject());
			return job;
		}
		String pkey = Optional.ofNullable(k).map(e->e + ":").orElse("");
		for(Map.Entry<String, JsonElement> entry : jo.entrySet()){
			String jk = entry.getKey();
			String key = pkey + jk;
			JsonElement element = entry.getValue();
			if (element.isJsonNull()){
				job.add(jk, null);
				continue;
			}
			if (element.isJsonArray()){
				JsonArray ary = new JsonArray();
				element.getAsJsonArray().forEach(e->{
					if (e.isJsonObject()){
						if (ary.size()==0){
							ary.add(makejson(key, e, new JsonObject()));
						}else{
							makejson(key, e, new JsonObject());
						}
					}
				});
				job.add(jk, ary);
			}else{
				if (element.isJsonPrimitive()){
					JsonType type = primitiveType(element.getAsJsonPrimitive());
					if (type.equals(JsonType.BOOLEAN)){
						job.addProperty(jk, default_boolean);
					}else if(type.equals(JsonType.NUMBER)){
						job.addProperty(jk, default_number);
					}else if(type.equals(JsonType.STRING)){
						job.addProperty(jk, default_string);
					}
				}else{
					if (element.deepCopy().toString().equals("{}")){
						makejson(jk, element, job);
					}else{
						job.add(jk, makejson(key, element, new JsonObject()));
					}
				}
			}
		}
		return job;
	}
}
