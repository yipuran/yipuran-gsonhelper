package org.yipuran.gsonhelper.array;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * JsonArrayReader生成ビルダ.
 * <PRE>
 * （使用例）
 *
 * GsonBuilder gsonbuilder = new GsonBuilder().serializeNulls()
 * .registerTypeAdapter(LocalDateTime.class, LocalDateTimeAdapter.of("yyyy/MM/dd HH:mm:ss"));
 *
 * JsonArrayParseBuilder.<Item>of(gsonbuilder, TypeToken.get(Item.class).getType())
 * .path("group", "itemlist").build()
 * .execute(reader, t->{
 * 	// t=配列要素
 * });
 * </PRE>
 * @since 4.12
 */
public final class JsonArrayParseBuilder<T>{
	private GsonBuilder gsonbuilder;
	private Stream<String> pathstream;
	private Type type;
	private JsonArrayParseBuilder(GsonBuilder gsonbuilder, Type type) {
		this.gsonbuilder = gsonbuilder;
		this.type = type;
	}
	/**
	 * Type指定インスタンス生成.
	 * @param gsonbuilder Tクラス解析のGson生成する為のGsonBuilder
	 * @param type 配列要素 T クラスの java.lang.reflect.Type
	 * @return
	 */
	public static <T> JsonArrayParseBuilder<T> of(GsonBuilder gsonbuilder, Type type) {
		return new JsonArrayParseBuilder<T>(gsonbuilder, type);
	}
	/**
	 * クラス指定インスタンス生成
	 * @param gsonbuilder Tクラス解析のGson生成する為のGsonBuilder
	 * @param cls 配列要素 T クラス
	 * @return
	 */
	public static <T> JsonArrayParseBuilder<T> of(GsonBuilder gsonbuilder, Class<T> cls) {
		return new JsonArrayParseBuilder<T>(gsonbuilder, TypeToken.get(cls).getType());
	}
	/**
	 * 解析対象配列までの JSONキーをPATH としてリストで指定
	 * @param pathlist List
	 * @return
	 */
	public JsonArrayParseBuilder<T> path(List<String> pathlist) {
		if (pathlist.size() < 1) throw new IllegalArgumentException("path is required!");
		pathstream = pathlist.stream();
		return this;
	}
	/**
	 *  解析対象配列までの JSONキーをPATH として並べる。
	 * @param path String[]
	 * @return
	 */
	public JsonArrayParseBuilder<T> path(String...path) {
		if (path.length < 1) throw new IllegalArgumentException("path is required!");
		pathstream = Arrays.asList(path).stream();
		return this;
	}
	/**
	 * JsonArrayReader生成
	 * @return JsonArrayParser
	 */
	public JsonArrayParser<T> build() {
		String ptn = "^\\$\\." + pathstream
		.map(e->e.replaceAll("\\.", "\\."))
		.map(e->e.replaceAll("\\[", "\\\\["))
		.map(e->e.replaceAll("\\]", "\\\\]"))
		.map(e->e.replaceAll("\\-", "\\\\-"))
		.map(e->e.replaceAll("\\+", "\\\\+"))
		.map(e->e.replaceAll("\\*", "\\\\*"))
		.map(e->e.replaceAll("\\$", "\\$"))
		.map(e->e.replaceAll("\\^", "\\^"))
		.map(e->e.replaceAll("\\?", "\\?"))
		.map(e->e.replaceAll("\\{", "\\\\{"))
		.map(e->e.replaceAll("\\}", "\\\\}"))
		.collect(Collectors.joining("\\."))
		+ "\\[\\d+\\]$";
		return new JsonArrayParser<>(gsonbuilder, type, ptn);
	}
}
