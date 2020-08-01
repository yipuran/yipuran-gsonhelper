package org.yipuran.gsonhelper.serialize;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.yipuran.gsonhelper.NumberParse;

import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 * Map&lt;String, Object&gt;用、JsonDeserializer.
 * <PRE>
 * Mapのnest＝２階層以降のObject は、JSonObject である。
 * １階層目だけ、数値型は JsonPrimitive からの getAsInt(),getAsDouble(),getAsLong(),getAsBigDecimal()
 * ,getAsNumber(),getAsFloat(),getAsShort(),getAsByte() などを使用できる。
 * また、１階層目だけ、数値型は、インスタンスに追加するパターンキーで指定されたものと一致すれば
 * 該当の getAsメソッドで value が格納される
 * （Usage）
 *     Gson gson = new GsonBuilder()
 *     .registerTypeAdapter(new TypeToken&lt;Map&lt;String, Object&gt;&gt;(){}.getType(), new GenericMapDeserializer())
 *     .serializeNulls().create();
 *     Map<String, Object> map = gson.fromJson(string, new TypeToken&lt;Map&lt;String, Object&gt;&gt;(){}.getType());
 *
 *     または、append を使ってキーに特定した変換をいくつでも指定できる。
 *     GenericMapDeserializer gmds = new GenericMapDeserializer();
 *     gmds.append("value", NumberParse.LONG);
 *     gmds.append("length", NumberParse.DOUBLE);
 *
 *     Gson gson = new GsonBuilder()
 *     .registerTypeAdapter(new TypeToken&lt;Map&lt;String, Object>>(){}.getType(), gmds)
 *     .serializeNulls().create();
 *
 * </PRE>
 */
public final class GenericMapDeserializer implements JsonDeserializer<Map<String, Object>>{
	private Map<String, NumberParse> nmap;
	/**
	 * コンストラクタ.
	 */
	public GenericMapDeserializer(){
		nmap = new HashMap<>();
	}

	/**
	 * NumberParse に従った getAsXxxx() 登録.
	 * @param key JSON キー
	 * @param numberparse NumberParse
	 */
	public void append(String key, NumberParse numberparse){
		nmap.put(key, numberparse);
	}

	/* @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type, com.google.gson.JsonDeserializationContext) */
	@Override
	public Map<String, Object> deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
		if (!jsonElement.isJsonObject()){
			return null;
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		Set<Entry<String, JsonElement>> jsonEntrySet = jsonObject.entrySet();
		Map<String, Object> deserializedMap = new HashMap<String, Object>();

		for(Entry<String, JsonElement> entry : jsonEntrySet){
			try{
				if(entry.getValue().isJsonNull()){
					deserializedMap.put(entry.getKey(), null);
				}else if (entry.getValue().isJsonArray()){
					deserializedMap.put(entry.getKey(), entry.getValue());
				}else if(entry.getValue().isJsonObject()){
				   deserializedMap.put(entry.getKey(), entry.getValue());
				}else if(entry.getValue().isJsonPrimitive()){
					JsonPrimitive p = entry.getValue().getAsJsonPrimitive();
					if (p.isNumber()){
						String key = entry.getKey();
						if (nmap.containsKey(key)){
							if (nmap.get(key).equals(NumberParse.INTEGER)){
								deserializedMap.put(key, p.getAsInt());
							}else if(nmap.get(key).equals(NumberParse.LONG)){
								deserializedMap.put(key, p.getAsLong());
							}else if(nmap.get(key).equals(NumberParse.DOUBLE)){
								deserializedMap.put(key, p.getAsDouble());
							}else if(nmap.get(key).equals(NumberParse.BIGDECIMAL)){
								deserializedMap.put(key, p.getAsBigDecimal());
							}else if(nmap.get(key).equals(NumberParse.NUMBER)){
								deserializedMap.put(key, p.getAsNumber());
							}else if(nmap.get(key).equals(NumberParse.SHORT)){
								deserializedMap.put(key, p.getAsShort());
							}else if(nmap.get(key).equals(NumberParse.FLOAT)){
								deserializedMap.put(key, p.getAsFloat());
							}else if(nmap.get(key).equals(NumberParse.BYTE)){
								deserializedMap.put(key, p.getAsByte());
							}else if(nmap.get(key).equals(NumberParse.CHARACTER)){
								deserializedMap.put(key, p.getAsCharacter());
							}
						}else{
							deserializedMap.put(key, p);
						}
					}else if(p.isString()){
						deserializedMap.put(entry.getKey(), p.getAsString());
					}else if(p.isBoolean()){
						deserializedMap.put(entry.getKey(), p.getAsBoolean());
					}else if(p.isJsonNull()){
						deserializedMap.put(entry.getKey(), null);
					}else{
						deserializedMap.put(entry.getKey(), context.deserialize(entry.getValue(), String.class));
					}
				}
			}catch(Exception e){
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return deserializedMap;
	}
}
