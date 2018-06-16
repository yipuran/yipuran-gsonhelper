package org.yipuran.gsonhelper;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * JSON Generic解析.
 * <PRE>
 * JSONキー、ツリー構造のPATHで Mapへの変換または検索を実行する為のクラス
 * ツリー構造のPATHは、"." 区切りでキーのPATHを指定する。配列要素は、[] で指定し n 番目は [n-1]である。
 * 統一した数値解釈はコンストラクタでは指定できない。統一で解釈させる場合は provide() でインスタンス生成時に
 * 指定する。
 *      JsonGenericParser parser = JsonGenericParser.provide(NumberParse.DOUBLE);
 * provide() を使用しない通常のコンストラクタで指定した場合は、数値型は、JsonPrimitive型として取得されるので、
 * Gson の getAsInt(),getAsDouble(),getAsLong(),getAsBigDecimal(),getAsNumber(),getAsFloat(),getAsShort(),getAsByte() から
 * 選択して使用する。
 *
 * Map への変換は toMap() 、検索実行は search() で、検索ハンドラの指定は addHandler() でチェーンで指定する。
 *
 * （検索の例）
 *    {  "store" : { "book": [
 *           {
 *             "category": "fiction",
 *              "author": "Hermane",
 *              "title": "MobyDick"
 *           },
 *           {
 *              "category": "fiction",
 *              "author": "Tolkien",
 *              "title": "Lambda"
 *            }
 *        ],
 *        "locate": "Tokyo"
 *    }
 *  に対して、先頭の book の title を検索実行する例
 *
 *    new JsonGenericParser().addHandler("store.book[0].title", (k, v)->{
 *         // k=キー  v=値
 *    }).search(reader);
 *
 * （Mapへ変換）
 *      Map<String, Object> map = genParser.toMap(reader);
 *      map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e->{
 *
 *         System.out.println("key = " + e.getKey() + "   :  value = " + e.getValue() );
 *
 *         if (e.getValue() instanceof JsonArray){
 *             System.out.println("This is JsonArray ");
 *         }
 *         if (e.getValue() instanceof JsonPrimitive){
 *             long l = ((JsonPrimitive)o).getAsLong());
 *         }
 *      });
 *   検索、Mapへ変換ともに、値が配列の場合、instanceof JsonArray や、JsonPrimitive で検査することができる
 *
 * </PRE>
 */
public final class JsonGenericParser{
	private NumberParse numberparse;
	/** コンストラクタ.	 */
	public JsonGenericParser(){
	}
	private JsonGenericParser(NumberParse numberParse){
		numberparse = numberParse;
	}
	/**
	 * 数値解釈指定のインスタンス生成.
	 * @param numberParse NumberParse
	 * @return JsonGenericParser
	 */
	public static JsonGenericParser provide(NumberParse numberParse){
		return new JsonGenericParser(numberParse);
	}
	/**
	 * JSONキーマップ取得. キーは"." 区切りのJSONキー、配列は[n]で要素指定
	 * @param json 解析対象のJSON文字列
	 * @return Map<String, Object>
	 */
	public Map<String, Object> toMap(String json){
		return toMap(new StringReader(json));
	}
	/**
	 * JSONキーマップ取得. キーは"." 区切りのJSONキー、配列は[n]で要素指定
	 * @param reader 解析対象のJSON読込み Reader
	 * @return Map<String, Object>
	 */
	public Map<String, Object> toMap(Reader reader){
		Map<String, Object> map = new HashMap<String, Object>();
		JsonElement je = new JsonParser().parse(reader);
		if (je.isJsonObject()){
			je.getAsJsonObject().entrySet().forEach(entry->	map.putAll(maped(entry.getKey(), entry.getValue(), map)));
		}
		return map;
	}
	private Map<String, Object> maped(String key, JsonElement je, Map<String, Object> map){
		if (je.isJsonNull()){
			map.put(key, null);
		}else if(je.isJsonObject()){
			je.getAsJsonObject().entrySet().forEach(entry->	map.putAll(maped(key + "." + entry.getKey(), entry.getValue(), map)));
		}else if(je.isJsonArray()){
			JsonArray jary = je.getAsJsonArray();
			map.put(key, jary);
			int i = 0;
			for(JsonElement e:jary){
				map.putAll(maped(key + "[" + i + "]", e, map));
				i++;
			}
		}else if(je.isJsonPrimitive()){
			JsonPrimitive p = je.getAsJsonPrimitive();
			if (p.isNumber()){
				if (numberparse==null){
					map.put(key, p);
				}else {
					if (numberparse.equals(NumberParse.INTEGER)){
						map.put(key, p.getAsInt());
					}else if(numberparse.equals(NumberParse.LONG)){
						map.put(key, p.getAsLong());
					}else if(numberparse.equals(NumberParse.DOUBLE)){
						map.put(key, p.getAsDouble());
					}else if(numberparse.equals(NumberParse.BIGDECIMAL)){
						map.put(key, p.getAsBigDecimal());
					}else if(numberparse.equals(NumberParse.NUMBER)){
						map.put(key, p.getAsNumber());
					}else if(numberparse.equals(NumberParse.SHORT)){
						map.put(key, p.getAsShort());
					}else if(numberparse.equals(NumberParse.FLOAT)){
						map.put(key, p.getAsFloat());
					}else if(numberparse.equals(NumberParse.BYTE)){
						map.put(key, p.getAsByte());
					}else if(numberparse.equals(NumberParse.CHARACTER)){
						map.put(key, p.getAsCharacter());
					}
				}
			}else if(p.isString()){
				map.put(key, p.getAsString());
			}else if(p.isBoolean()){
				map.put(key, p.getAsBoolean());
			}else if(p.isJsonNull()){
				map.put(key, null);
			}
		}
		return map;
	}

	private Map<String, BiConsumer<String, Object>> hmap = new HashMap<>();

	/**
	 * 検索ハンドラ登録.
	 * @param key "." 区切りのJSONキー、配列は[n]で要素指定、
	 * @param biconsumer BiConsumer<String, Object> String=key, Object=JSONキーが指すObject
	 * @return JsonGenericParser
	 */
	public JsonGenericParser addHandler(String key, BiConsumer<String, Object> biconsumer){
		hmap.put(key, biconsumer);
		return this;
	}
	/**
	 * 検索ハンドラクリア.
	 * 登録してある検索ハンドラを全てクリア
	 */
	public JsonGenericParser clear(){
		hmap.clear();
		return this;
	}
	/**
	 * BiConsumer で指定する JSONキーで限定する検索読出しハンドラによる読込実行.
	 * @param json 解析対象のJSON文字列
	 */
	public void search(String json){
		search(new StringReader(json));
	}
	/**
	 * BiConsumer で指定する JSONキーで限定する検索読出しハンドラによる読込実行.
	 * （注意）Reader 指定は１回実行したら次回は異なる Reader でなければならない。
	 * @param reader reader 解析対象のJSON読込み Reader
	 */
	public void search(Reader reader){
		JsonElement je = new JsonParser().parse(reader);
		System.out.println(je.isJsonObject());
		JsonObject jo = je.getAsJsonObject();
		for(Map.Entry<String, JsonElement> entry : jo.entrySet()){
			search(entry.getKey(), entry.getValue());
		}
	}
	private void search(String key, JsonElement je){
		if (je.isJsonNull()){
			hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
			.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), null));
		}else if(je.isJsonObject()){
			je.getAsJsonObject().entrySet().forEach(entry->search(key+"."+entry.getKey(), entry.getValue()));
		}else if(je.isJsonArray()){
			JsonArray jary = je.getAsJsonArray();
			hmap.entrySet().stream().filter(he->he.getKey().equals(key))
			.findFirst().ifPresent(he->he.getValue().accept(he.getKey(), jary));

			AtomicInteger i = new AtomicInteger(0);
			for(JsonElement e:jary){
				String k = key + "[" + i.getAndIncrement() + "]";
				hmap.entrySet().stream().filter(he->he.getKey().equals(k) && !e.isJsonPrimitive()).findFirst()
				.ifPresent(he->he.getValue().accept(he.getKey(), e));
				search(k, e);
			}
		}else if(je.isJsonPrimitive()){
			JsonPrimitive p = je.getAsJsonPrimitive();
			if (p.isNumber()){
				if (numberparse==null){
					hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
					.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), p));
				}else{
					if (numberparse.equals(NumberParse.INTEGER)){
						hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
						.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), p.getAsInt()));
					}else if(numberparse.equals(NumberParse.LONG)){
						hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
						.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), p.getAsLong()));
					}else if(numberparse.equals(NumberParse.DOUBLE)){
						hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
						.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), p.getAsDouble()));
					}else if(numberparse.equals(NumberParse.BIGDECIMAL)){
						hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
						.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), p.getAsBigDecimal()));
					}else if(numberparse.equals(NumberParse.NUMBER)){
						hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
						.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), p.getAsNumber()));
					}else if(numberparse.equals(NumberParse.SHORT)){
						hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
						.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), p.getAsShort()));
					}else if(numberparse.equals(NumberParse.FLOAT)){
						hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
						.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), p.getAsFloat()));
					}else if(numberparse.equals(NumberParse.BYTE)){
						hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
						.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), p.getAsByte()));
					}else if(numberparse.equals(NumberParse.CHARACTER)){
						hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
						.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), p.getAsCharacter()));
					}
				}
			}else if(p.isString()){
				hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
				.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), p.getAsString()));
			}else if(p.isBoolean()){
				hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
				.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), p.getAsBoolean()));
			}else if(p.isJsonNull()){
				hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
				.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), null));
			}
		}else if(je.isJsonObject()){
			je.getAsJsonObject().entrySet().forEach(entry->search(key+"."+entry.getKey(), entry.getValue()));
		}else if(je.isJsonNull()){
			hmap.entrySet().stream().filter(e->key.equals(e.getKey()))
			.findFirst().ifPresent(e->e.getValue().accept(e.getKey(), null));
		}
	}
}
