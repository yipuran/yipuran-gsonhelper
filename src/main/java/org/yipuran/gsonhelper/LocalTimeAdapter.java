package org.yipuran.gsonhelper;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson LocalTime Adapter.
 * <PRE>
 * Gson gson = new GsonBuilder()
 * .registerTypeAdapter(LocalTime.class, LocalTimeAdapter.create(()->DateTimeFormatter.ofPattern("hh:mm:ss a")))
 * .create();
 * </PRE>
 */
public interface LocalTimeAdapter extends JsonSerializer<LocalTime>, JsonDeserializer<LocalTime>{
	@Override
	public default JsonElement serialize(LocalTime datetime, Type type, JsonSerializationContext context){
		return new JsonPrimitive(datetime.format(getFormatter()));
	}
	@Override
	public default LocalTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException{
		return LocalTime.parse(json.getAsString(), getFormatter());
	}
	/**
	 * LocalDateTimeAdapter生成.
	 * @param adapter LocalDateTimeAdapterのgetFormatter()を関数型で指定する
	 * @return LocalDateTimeAdapter
	 */
	public static LocalTimeAdapter create(LocalTimeAdapter adapter){
		return adapter;
	}
	/**
	 * JSONシリアライズ、デシリアライズにおける日付時刻書式を指定する。.
	 * @return DateTimeFormatter
	 */
	public DateTimeFormatter getFormatter();
}
