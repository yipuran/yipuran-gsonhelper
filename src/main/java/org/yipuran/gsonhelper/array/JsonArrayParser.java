package org.yipuran.gsonhelper.array;

import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * JSON配列解析.
 * <PRE>
 * Consumer または、Stream を取得する。
 *
 * Consumerの取得
 *    JsonReader を指定して配列要素を解析して任意 Ｔクラスに変換した配列読み取りを Consumer で実行する。
 *    Gson の fromJson で配列全てを一度に、リストに格納するわけではなく１要素ずつの Consumer 処理で
 *    ある為に、Big size になっている JSON配列を読込み処理するのに適している。
 *
 * （利用方法）
 * JsonArrayParser インスタンス生成は、JsonArrayReaderBuilder で生成する。
 *
 * JsonArrayReaderBuilder インスタンス生成：
 * 　　JsonArrayReaderBuilder の ofメソッドで配列要素を任意クラスに変換する為の Gson を生成する
 * 　　GsonBuilder と 任意クラス java.lang.reflect.Type か、Class＜T＞を指定する。
 * 　　java.lang.reflect.Type は、com.google.common.reflect.TypeToken の他、
 * 　　com.google.inject.TypeLiteral など、取得する方法はあるが、
 * 　　gson を実行する配下では、com.google.gson.reflect.TypeToken を使用する方法が同じJAR依存が保たれる。
 * 　　　　TypeToken.get(cls).getType()
 * JsonArrayReaderBuilder に、JSON配列のPATH を指定：
 * 　　キーをルートから順に並べたリスト、もしくは、文字列の可変配列 String... で指定する
 * 　　メソッド：JsonArrayReaderBuilder#path(List＜String＞)
 * 　　　　　　　JsonArrayReaderBuilder#path(String...)
 * JsonArrayParser インスタンス生成：
 * 　　JsonArrayReaderBuilder#create() を実行する
 *
 * 解析の実行（Consumerの実行）：
 * 　　jsonArrayParser.execute(jsonReader, t->{
 * 　　　　// t = 配列要素
 * 　　});
 * （ビルダ生成～解析実行の例）
 * GsonBuilder gsonbuilder = new GsonBuilder().serializeNulls()
 * .registerTypeAdapter(LocalDateTime.class, LocalDateTimeAdapter.of("yyyy/MM/dd HH:mm:ss"));
 *
 * JsonArrayParseBuilder.&lt;Item>of(gsonbuilder, TypeToken.get(Item.class).getType())
 * .path("group", "itemlist").create()
 * .execute(reader, t->{
 * 	// t=配列要素
 * });
 *
 * 解析の実行（Stream の取得）：
 * 　　Stream&lt;item> stream = jsonArrayParser.execute(jsonReader);
 *
 *  （ビルダ生成～解析実行の例）
 * GsonBuilder gsonbuilder = new GsonBuilder().serializeNulls()
 * .registerTypeAdapter(LocalDateTime.class, LocalDateTimeAdapter.of("yyyy/MM/dd HH:mm:ss"));
 *
 * JsonArrayParseBuilder.&lt;Item>of(gsonbuilder, TypeToken.get(Item.class).getType())
 * .path("group", "itemlist").create().forEach(System.out::println);
 *
 * 【注意】
 * JsonReader は、Readerとして読み進められる。
 *
 * Consumer実行中に発生する例外、JsonIOException, JsonSyntaxException, IOException は、
 * RuntimeException でラップされてスローされる。
 * </PRE>
 * @since 4.14
 */
public final class JsonArrayParser<T>{
	private Gson gson;
	private Type type;
	private Pattern targetptn;

	protected JsonArrayParser(GsonBuilder gsonbuilder, Type type, String pathptn) {
		gson = gsonbuilder.create();
		this.type = type;
		targetptn = Pattern.compile(pathptn);
	}
	/**
	 * 配列解析Consumer実行.
	 * @param reader JsonReader
	 * @param consumer Tクラスの Consumer
	 */
	public void execute(JsonReader reader, Consumer<T> consumer) {
		boolean request = false;
		try{
			while((reader.hasNext() || reader.peek().equals(JsonToken.END_ARRAY)
				|| reader.peek().equals(JsonToken.END_OBJECT)) && !reader.peek().equals(JsonToken.END_DOCUMENT)
			){
					JsonToken token = reader.peek();
					if (token.equals(JsonToken.BEGIN_ARRAY)){
						reader.beginArray();
						if (targetptn.matcher(reader.getPath()).matches()){
							request = true;
						}
					}else if(token.equals(JsonToken.END_ARRAY)){
						reader.endArray();
						if (request) request = false;
					}else if(token.equals(JsonToken.BEGIN_OBJECT)){
						if (request) {
							consumer.accept(gson.fromJson(reader, type));
						}else{
							reader.beginObject();
						}
					}else if(token.equals(JsonToken.END_OBJECT)){
						reader.endObject();
					}else if(token.equals(JsonToken.NAME)){
						reader.nextName();
					}else if(token.equals(JsonToken.STRING)){
						reader.nextString();
					}else if(token.equals(JsonToken.NUMBER)){
						reader.nextString();
					}else if(token.equals(JsonToken.BOOLEAN)){
						reader.nextBoolean();
					}else if(token.equals(JsonToken.NULL)){
						reader.nextNull();
					}
			}
		}catch(Exception ex){
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	/**
	 * Stream 取得.
	 * @param reader JsonReader
	 * @return 配列 JsonArray ＴのStream
	 * @since 4.15
	 */
	public Stream<T> stream(JsonReader reader){
		Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED){
			boolean request = false;
			@Override
			public boolean tryAdvance(Consumer<? super T> action){
				try{
					JsonToken token = reader.peek();
					if (!reader.hasNext() && !token.equals(JsonToken.END_ARRAY) && token.equals(JsonToken.END_OBJECT) && !token.equals(JsonToken.END_DOCUMENT)) {
						return false;
					}
					switch(token){
						case BEGIN_ARRAY:
								reader.beginArray();
								if (targetptn.matcher(reader.getPath()).matches()){
									request = true;
								}
								break;
						case END_ARRAY:
								reader.endArray();
								if (request) request = false;
								break;
						case BEGIN_OBJECT:
								if (request) {
									action.accept(gson.fromJson(reader, type));
								}else{
									reader.beginObject();
								}
								break;
						case END_OBJECT:
								reader.endObject();
								break;
						case NAME:
								reader.nextName();
								break;
						case STRING:
								reader.nextString();
								break;
						case NUMBER:
								reader.nextString();
								break;
						case BOOLEAN:
								reader.nextBoolean();
								break;
						case NULL:
								reader.nextNull();
								break;
						case END_DOCUMENT:
								return false;
					}
					return true;
				}catch(Exception ex){
					throw new RuntimeException(ex.getMessage(), ex);
				}
			}
		};
		return StreamSupport.stream(spliterator, false);
	}

	/**
	 * Json-Path,JsonValue の MapEntry Stream 取得.
	 * @param reader JsonReader
	 * @return Map.Entryy&lt;String, Object&gt; の Stream
	 * @since 4.15
	 */
	public Stream<Map.Entry<String, Object>> mapstream(JsonReader reader){
		Spliterator<Map.Entry<String, Object>> spliterator = new Spliterators.AbstractSpliterator<Map.Entry<String, Object>>(Long.MAX_VALUE, Spliterator.ORDERED){
			@Override
			public boolean tryAdvance(Consumer<? super Map.Entry<String, Object>> action){
				try{
					JsonToken token = reader.peek();
					if (!reader.hasNext() && !token.equals(JsonToken.END_ARRAY) && token.equals(JsonToken.END_OBJECT) && !token.equals(JsonToken.END_DOCUMENT)) {
						return false;
					}
					switch(token){
						case BEGIN_ARRAY:
								reader.beginArray();
								break;
						case END_ARRAY:
								reader.endArray();
								break;
						case BEGIN_OBJECT:
								reader.beginObject();
								break;
						case END_OBJECT:
								reader.endObject();
								break;
						case NAME:
								reader.nextName();
								break;
						case STRING:
								action.accept(new AbstractMap.SimpleEntry<String, Object>(reader.getPath(), reader.nextString()));
								break;
						case NUMBER:
								double d = Double.valueOf(reader.nextString());
								action.accept(new AbstractMap.SimpleEntry<String, Object>(reader.getPath(), d==(long)d
										? Integer.MIN_VALUE <= (long)d && (long)d <= Integer.MAX_VALUE ? (int)d : (long)d : d));
								break;
						case BOOLEAN:
								action.accept(new AbstractMap.SimpleEntry<String, Object>(reader.getPath(), reader.nextBoolean()));
								break;
						case NULL:
								action.accept(new AbstractMap.SimpleEntry<String, Object>(reader.getPath(), null));
								break;
						case END_DOCUMENT:
								return false;
					}
					return true;
				}catch(Exception ex){
					throw new RuntimeException(ex.getMessage(), ex);
				}
			}
		};
		return StreamSupport.stream(spliterator, false);
	}

}