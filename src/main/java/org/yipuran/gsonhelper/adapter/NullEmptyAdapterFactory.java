package org.yipuran.gsonhelper.adapter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * NULL → "" JSON 作成用 AdapterFactory.
 * <PRE>
 * GsonBuilder の serializeNulls() は、JSON生成時、空は、"key": null を出力するのに対して、
 * "key": "" を生成するための TypeAdapterFactory
 *
 * この NullEmptyAdapterFactory を使用した場合、serializeNulls() は効力を持たなくなる。
 *
 * （使用例）
 * 　　　Gson gson = new GsonBuilder().registerTypeAdapterFactory(new NullEmptyAdapterFactory()).create();
 *
 * この AdapterFactory が、デフォルトで備えている TypeToken は、以下のクラス
 * 　　String
 * 　　Integer
 * 　　Long
 * 　　Double
 * 　　LocalDate
 * 　　LocalDateTime
 * この他に追加したい場合は、addTypeAdapterメソッドで追加して GsonBuilter に渡す。
 *
 *
 * （使用例）
 * 　　　// Foo が null の時に、 "" とする FooAdapter を用意して追加
 * 　　　Gson gson = new GsonBuilder().registerTypeAdapterFactory(
 * 　　　   new NullEmptyAdapterFactory()
 * 　　　   .addTypeAdapter(TypeToken.get(Foo.class), new FooAdapter())
 * 　　　).create();
 * </PRE>
 * 
 */
public class NullEmptyAdapterFactory implements TypeAdapterFactory{
	private Map<TypeToken, TypeAdapter> map;

	/**
	 * default コンストラクタ.
	 */
	public NullEmptyAdapterFactory() {
		map = new HashMap<>();
	}

	/**
	 * TypeAdapter の追加.
	 * @param typetoken com.google.gson.reflect.TypeToken
	 * @param a TypeAdapter
	 * @return コンストラクタ生成後のTypeAdapter追加したインスタンス
	 */
	public NullEmptyAdapterFactory addTypeAdapter(TypeToken typetoken, TypeAdapter a) {
		map.put(typetoken, a);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TypeAdapter create(Gson gson, TypeToken type){
		Class<?> rawType =  type.getRawType();
		if (rawType==String.class){
			return new TypeAdapter<String>(){
				@Override
				public String read(JsonReader reader) throws IOException {
					if (reader.peek() == JsonToken.NULL) {
						reader.nextNull();
						return "";
					}
					return reader.nextString();
				}
				@Override
				public void write(JsonWriter writer, String value) throws IOException {
					if (value == null) {
						writer.value("");
						return;
					}
					writer.value(value);
				}
			};
		}else if(rawType==Integer.class) {
			return new TypeAdapter<Integer>(){
				@Override
				public void write(JsonWriter writer, Integer value) throws IOException{
					if (value == null) {
						writer.value("");
						return;
					}
					writer.value(value);
				}
				@Override
				public Integer read(JsonReader reader) throws IOException{
					if (reader.peek() == JsonToken.NULL) {
						reader.nextNull();
						return null;
					}
					return reader.nextInt();
				}
			};
		}else if(rawType==Long.class) {
			return new TypeAdapter<Long>(){
				@Override
				public void write(JsonWriter writer, Long value) throws IOException{
					if (value == null) {
						writer.value("");
						return;
					}
					writer.value(value);
				}
				@Override
				public Long read(JsonReader reader) throws IOException{
					if (reader.peek() == JsonToken.NULL) {
						reader.nextNull();
						return null;
					}
					return reader.nextLong();
				}
			};
		}else if(rawType==Double.class) {
			return new TypeAdapter<Double>(){
				@Override
				public void write(JsonWriter writer, Double value) throws IOException{
					if (value == null) {
						writer.value("");
						return;
					}
					writer.value(value);
				}
				@Override
				public Double read(JsonReader reader) throws IOException{
					if (reader.peek() == JsonToken.NULL) {
						reader.nextNull();
						return null;
					}
					return reader.nextDouble();
				}
			};
		}else if(rawType==Boolean.class) {
			return new TypeAdapter<Boolean>(){
				@Override
				public void write(JsonWriter writer, Boolean value) throws IOException{
					if (value == null) {
						writer.value("");
						return;
					}
					writer.value(value);
				}
				@Override
				public Boolean read(JsonReader reader) throws IOException{
					if (reader.peek() == JsonToken.NULL) {
						reader.nextNull();
						return null;
					}
					return reader.nextBoolean();
				}
			};
		}else if(rawType==LocalDate.class) {
			return new TypeAdapter<LocalDate>(){
				@Override
				public void write(JsonWriter writer, LocalDate value) throws IOException{
					if (value == null) {
						writer.value("");
						return;
					}
					writer.value(value.toString());
				}
				@Override
				public LocalDate read(JsonReader reader) throws IOException{
					if (reader.peek() == JsonToken.NULL) {
						reader.nextNull();
						return null;
					}
					return LocalDate.parse(reader.nextString());
				}
			};
		}else if(rawType==LocalDateTime.class) {
			return new TypeAdapter<LocalDateTime>(){
				@Override
				public void write(JsonWriter writer, LocalDateTime value) throws IOException{
					if (value == null) {
						writer.value("");
						return;
					}
					writer.value(value.toString());
				}
				@Override
				public LocalDateTime read(JsonReader reader) throws IOException{
					if (reader.peek() == JsonToken.NULL) {
						reader.nextNull();
						return null;
					}
					return LocalDateTime.parse(reader.nextString());
				}
			};
		}
		return map.entrySet().stream().filter(e->e.getKey().getRawType()==rawType).map(e->e.getValue()).findFirst().orElse(null);
	}
}
