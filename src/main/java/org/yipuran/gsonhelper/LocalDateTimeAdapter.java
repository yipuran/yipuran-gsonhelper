package org.yipuran.gsonhelper;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson LocalDateTime Adapter.
 * <PRE>
 * Gson gson = new GsonBuilder()
 * .registerTypeAdapter(LocalDateTime.class, LocalDateTimeAdapter.create(()-&gt;DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS")))
 * .create();
 *
 * または、
 * Gson gson = new GsonBuilder()
 * .registerTypeAdapter(LocalDateTime.class, LocalDateTimeAdapter.of("yyyy/MM/dd HH:mm:ss.SSS"))
 * .create();
 *
 * </PRE>
 */
public interface LocalDateTimeAdapter extends JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
	@Override
	public default JsonElement serialize(LocalDateTime datetime, Type type, JsonSerializationContext context){
		return new JsonPrimitive(datetime.format(getFormatter()));
	}
	@Override
	public default LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException{
		return LocalDateTime.parse(json.getAsString(), getFormatter());
	}
	/**
	 * LocalDateTimeAdapter生成.
	 * @param adapter LocalDateTimeAdapterのgetFormatter()を関数型で指定する
	 * @return LocalDateTimeAdapter
	 */
	public static LocalDateTimeAdapter create(LocalDateTimeAdapter adapter){
		return adapter;
	}
	/**
	 * LocalDateTimeAdapter生成（Pattern文字列指定）.
	 * @param pattern DateTimeFormatter に渡す日付パターン
	 * @return LocalDateTimeAdapter
	 */
	public static LocalDateTimeAdapter of(String pattern){
		return LocalDateTimeAdapter.create(()->DateTimeFormatter.ofPattern(pattern));
	}
	/**
	 * JSONシリアライズ、デシリアライズにおける日付時刻書式を指定する。
	 * @return DateTimeFormatter
	 */
	public DateTimeFormatter getFormatter();
}
