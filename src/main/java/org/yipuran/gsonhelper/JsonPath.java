package org.yipuran.gsonhelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * ＠JsonPath("$.aaa.bbb")
 *
 * フィールドに付与する JSON-path
 * セパレータ文字 "." をエスケープする場合は、"\" でエスケープする。
 * AutoPathSerializer 使用時に効果がある。
 *
 * @since 4.17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface JsonPath {
	String value();
}

