package org.yipuran.gsonhelper;

import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * 汎用 JsonDeserializer.
 * <PRE>
 * Supplier と、JsonDeserializeFunction を指定したインスタンスをGsonBuilder の registerTypeAdapter で指定する。
 *
 * （使用例）
 * Gson gson = new GsonBuilder().serializeNulls()
 * .registerTypeAdapter(new TypeToken&lt;LocalDate&gt;(){}.getType(), LocalDateAdapter.create(()-&gt;DateTimeFormatter.ofPattern("yyyy/MM/dd")))
 * .registerTypeAdapter(new TypeToken&lt;Foo&gt;(){}.getType(), new GenericDeserializer&lt;&gt;(()-&gt;new Foo()
 *    , (s, t, c)-&gt;s.collect(()-&gt;t, (r, u)-&gt;{
 *      if (u.getKey().equals("name")) r.setName(u.getValue().getAsString());
 *      if (u.getKey().equals("date")) r.setDate(c.deserialize(u.getValue(), new TypeToken&lt;LocalDate&gt;(){}.getType()));
 *    }, (r, u)-&gt;{})))
 * .create();
 * </PRE>
 */
public class GenericDeserializer<T> implements JsonDeserializer<T>{
	private JsonDeserializeFunction<Stream<Entry<String, JsonElement>>, T, JsonDeserializationContext> function;
	private Supplier<T> supplier;
	/**
	 * コンストラクタ.
	 * @param supplier 解析対象TのSupplier
	 * @param function JsonDeserializeFunction
	 */
	public GenericDeserializer(Supplier<T> supplier, JsonDeserializeFunction<Stream<Entry<String, JsonElement>>, T, JsonDeserializationContext> function){
		this.supplier = supplier;
		this.function = function;
	}
	@Override
	public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
		T t = supplier.get();
		if (!json.isJsonNull()){
			t = function.apply(json.getAsJsonObject().entrySet().stream().filter(e->!e.getValue().isJsonNull()), t, context);
		}
		return t;
	}
}
