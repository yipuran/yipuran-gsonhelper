package org.yipuran.gsonhelper;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson LocalDate Adapter.
 * <PRE>
 * Gson gson = new GsonBuilder()
 * .registerTypeAdapter(LocalDate.class, LocalDateAdapter.create(()-&gt;DateTimeFormatter.ofPattern("yyyy/MM/dd")))
 * .create();
 *
 * または、
 *
 * Gson gson = new GsonBuilder()
 * .registerTypeAdapter(LocalDate.class, LocalDateAdapter.of("yyyy/MM/dd"))
 * .create();
 * </PRE>
 */
@FunctionalInterface
public interface LocalDateAdapter extends JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
	@Override
	public default JsonElement serialize(LocalDate date, Type type, JsonSerializationContext context){
		return new JsonPrimitive(date.format(getFormatter()));
	}
	@Override
	public default LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException{
		return LocalDate.parse(json.getAsString(), getFormatter());
	}
	/**
	 * LocalDateAdapter生成.
	 * @param adapter LocalDateAdapterのgetFormatter()を関数型で指定する
	 * @return LocalDateAdapter
	 */
	public static LocalDateAdapter create(LocalDateAdapter adapter){
		return adapter;
	}
	/**
	 * LocalDateAdapter生成（Pattern文字列指定）.
	 * @param pattern DateTimeFormatter に渡す日付パターン
	 * @return LocalDateAdapter
	 */
	public static LocalDateAdapter of(String pattern){
		return LocalDateAdapter.create(()->DateTimeFormatter.ofPattern(pattern));
	}
	/**
	 * JSONシリアライズ、デシリアライズにおける日付書式を指定する。
	 * @return DateTimeFormatter
	 */
	public DateTimeFormatter getFormatter();

}

