package org.yipuran.gsonhelper;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * ＠Exclude フィールド除外 Strategy.
 * <PRE>
 * ＠Exclude を付与したフィールドをJSONシリアライズ対象外にする。
 *
 *     new GsonBuilder().addSerializationExclusionStrategy( new ExcludeWithAnotateStrategy() ).build();
 *
 * ＠Expose の指定による excludeFieldsWithoutExposeAnnotation() と併用した場合、
 * ＠Exclude が優先されて＠Expose の効力はなくなる。
 * </PRE>
 * @since 4.5
 */
public class ExcludeWithAnotateStrategy implements ExclusionStrategy{
	@Override
	public boolean shouldSkipClass(Class<?> clazz){
		return false;
	}
	@Override
	public boolean shouldSkipField(FieldAttributes f){
		return f.getAnnotation(Exclude.class) != null;
	}
}
