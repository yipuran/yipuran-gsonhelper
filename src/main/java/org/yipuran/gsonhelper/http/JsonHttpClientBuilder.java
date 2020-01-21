package org.yipuran.gsonhelper.http;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.GsonBuilder;

/**
 * JsonHttpClientを生成する。
 */
public final class JsonHttpClientBuilder{
	private URL url;
	private GsonBuilder gsonbuilder;

	/**
	 * コンストラクタ.
	 * @param path 送信先URLパス
	 * @param gsonbuilder GsonBuilder 送信するObject をJSONにする為のGsonBuilder
	 */
	private JsonHttpClientBuilder(String path, GsonBuilder gsonbuilder){
		this.gsonbuilder = gsonbuilder;
		try{
			url = new URL(path);
		}catch(MalformedURLException e){
			throw new RuntimeException(e);
		}
	}
	/**
	 * JsonHttpClientBuilderインスタンス生成.
	 * @param path 送信先URLパス
	 * @param gsonbuilder GsonBuilder 送信するObject をJSONにする為のGsonBuilder
	 * @return JsonHttpClientBuilder
	 */
	public static JsonHttpClientBuilder of(String path, GsonBuilder gsonbuilder){
		return new JsonHttpClientBuilder(path, gsonbuilder);
	}
	/**
	 * JsonHttpClient生成
	 * @return JsonHttpClient
	 */
	public JsonHttpClient build(){
		return new JsonHttpClient(url, gsonbuilder);
	}
}
