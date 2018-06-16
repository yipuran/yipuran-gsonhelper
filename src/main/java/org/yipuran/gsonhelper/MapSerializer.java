package org.yipuran.gsonhelper;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
/**
 * Mapシリアライザ.
 * <PRE>
 * null → "" にする toJson は、Map でない型クラスでは、NullStringToEmptyAdapterFactory or NullStringIntegerToEmptyAdapterFactory
 * を使用すれば解決するが、Map に対してはこれらのアダプタが有効にならない。
 *
 *   Map を toJson 実行した時に、null → "" にする場合は、このシリアライザを使用し、
 *
 *   更に、、toJson は、java.lang.reflect.Type を、com.google.gson.reflect.TypeToken の getType で以下のように指定する。
 *
 *       toJson(map, new TypeToken&lt;Map&lt;String, Object&gt;&gt;(){}.getType()));
 *
 *  使用例：
 *      Map<String, Object> map = new HashMap<>();
 *
 *      Gson gson = new GsonBuilder()
 *        .registerTypeAdapter(new TypeToken&lt;Map&lt;String, Object&gt;&gt;(){}.getType(), new MapSerializer())
 *        .create();
 *
 *      String s = gson.toJson(map, new TypeToken&lt;Map&lt;String, Object&gt;&gt;(){}.getType());
 *
 * </PRE>
 */
public class MapSerializer implements JsonSerializer<Map<String, Object>>{

	/* @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext) */
	@Override
	public JsonElement serialize(Map<String, Object> map, Type typeOfSrc, JsonSerializationContext context){
		JsonObject jsonObject = new JsonObject();
		for(Map.Entry<String, Object> entry : map.entrySet()){
			if (entry.getValue()==null){
				jsonObject.add(entry.getKey(), context.serialize(""));
				continue;
			}else if(entry.getValue() instanceof Map){
				jsonObject.add(entry.getKey(), this.serialize((Map<String, Object>)entry.getValue(), typeOfSrc, context));
				continue;
			}
			jsonObject.add(entry.getKey(), context.serialize(entry.getValue()));
		}
		return jsonObject;
	}
}
