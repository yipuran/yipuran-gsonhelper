package org.yipuran.gsonhelper;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * JSON NULL→"" 変換用 String アダプタファクトリ(for Google gson).
 * <PRE>
 * Gson gson = new GsonBuilder()
 *              .registerTypeAdapterFactory(new NullStringToEmptyAdapterFactory())
 *              .create();
 * GsonBuilder#serializeNulls() の意味がなくなる。
 * </PRE>
 */
public class NullStringToEmptyAdapterFactory implements TypeAdapterFactory{
	/*
	 * @see com.google.gson.TypeAdapterFactory#create(com.google.gson.Gson, com.google.gson.reflect.TypeToken)
	 */
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type){
		 Class<T> rawType = (Class<T>)type.getRawType();
		 if (rawType != String.class){
			 return null;
		 }
		 return (TypeAdapter<T>) new TypeAdapter<String>(){
			@Override
			public String read(JsonReader reader) throws IOException{
				if (reader.peek()==JsonToken.NULL){
					reader.nextNull();
					return "";
				}
				return reader.nextString();
			}
			@Override
			public void write(JsonWriter writer, String value) throws IOException{
				if (value==null){
					writer.value("");
					return;
				}
				writer.value(value);
			}
		};
	}
}

