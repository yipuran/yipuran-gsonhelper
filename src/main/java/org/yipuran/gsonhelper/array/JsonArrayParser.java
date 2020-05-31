package org.yipuran.gsonhelper.array;

import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * JSON配列解析Consumer実行.
 * <PRE>
 * JsonReader を指定して配列要素を解析して任意 Ｔクラスに変換した配列読み取りを Consumer で実行する。
 * Gson の fromJson で配列全てを一度に、リストに格納するわけではなく１要素ずつの Consumer 処理で
 * ある為に、Big size になっている JSON配列を読込み処理するのに適している。
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
 * 　　JsonArrayReaderBuilder#build() を実行する
 * 解析の実行：
 * 　　jsonArrayParser.execute(jsonReader, t->{
 * 　　　　// t = 配列要素
 * 　　});
 * （ビルダ生成～解析実行の例）
 * GsonBuilder gsonbuilder = new GsonBuilder().serializeNulls()
 * .registerTypeAdapter(LocalDateTime.class, LocalDateTimeAdapter.of("yyyy/MM/dd HH:mm:ss"));
 *
 * JsonArrayParseBuilder.<Item>of(gsonbuilder, TypeToken.get(Item.class).getType())
 * .path("group", "itemlist").build()
 * .execute(reader, t->{
 * 	// t=配列要素
 * });
 *
 * 【注意】
 * execute 実行でJsonReader は、Readerとして読み進められる。
 *
 * Consumer実行中に発生する例外、JsonIOException, JsonSyntaxException, IOException は、
 * RuntimeException でラップされてスローされる。
 * </PRE>
 * @since 4.12
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
						if (request) request = true;
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
						reader.nextLong();
					}else if(token.equals(JsonToken.BOOLEAN)){
						reader.nextBoolean();
					}else if(token.equals(JsonToken.BOOLEAN)){
						reader.nextNull();
					}
			}
		}catch(Exception ex){
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
}