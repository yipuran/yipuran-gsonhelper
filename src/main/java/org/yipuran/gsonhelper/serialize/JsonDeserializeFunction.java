package org.yipuran.gsonhelper.serialize;

/**
 * JsonDeserializeFunction.
 * <PRE>
 * GenericDeserializer生成の第２引数として指定する。
 *
 * S = Stream&lt;Entry&lt;String, JsonElement&gt;&gt;
 * T = JSON→変換対象の型
 * C = JsonDeserializationContext
 * return = T と同じ型
 * </PRE>
 */
@FunctionalInterface
public interface JsonDeserializeFunction<S, T, C>{
	T apply(S s, T t, C c);
}
