package org.yipuran.gsonhelper;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Map<String, Object>用、JsonDeserializer.
 * <PRE>
 * （Usage）
 *     Gson gson = new GsonBuilder()
 *     .registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(), new GenericMapDeserializer())
 *     .serializeNulls().create();
 *     Map<String, Object> map = gson.fromJson(string, new TypeToken<Map<String, Object>>(){}.getType());
 * </PRE>
 */
public final class GenericMapDeserializer implements JsonDeserializer<Map<String, Object>>{

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
					deserializedMap.put(entry.getKey(), context.deserialize(entry.getValue(), String.class));
				}
			}catch(Exception e){
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return deserializedMap;
	}
}
