package org.yipuran.gsonhelper.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.GsonBuilder;

/**
 * JsonHttpClientを生成する。
 * <PRE>
 * （使用法）
 * String path;  // 送信先URL
 * GsonBuilder gsonbuilder = new Gsonbuilder().serializeNulls();
 * JsonHttpClient client = JsonHttpClientBuilder.of(path, gsonbuilder).build();
 * または、
 * JsonHttpClient client = JsonHttpClientBuilder.of(path, gsonbuilder).setForceUtf8().build();
 * </PRE>
 */
public final class JsonHttpClientBuilder{
	private URL url;
	private GsonBuilder gsonbuilder;
	private boolean forceUtf8 = false;
	private Map<String, String> headerOptions;

	/**
	 * コンストラクタ.
	 * @param path 送信先URLパス
	 * @param gsonbuilder GsonBuilder 送信するObject をJSONにする為のGsonBuilder
	 */
	private JsonHttpClientBuilder(String path, GsonBuilder gsonbuilder){
		this.gsonbuilder = gsonbuilder;
		headerOptions = new HashMap<>();
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
	 * 生成されるJsonHttpClient の受信JSON出力を強制的にUTF-8変換して出力
	 */
	public JsonHttpClientBuilder setForceUtf8(){
		this.forceUtf8 = true;
		return this;
	}
	/**
	 * Header property 追加.
	 * @param name key
	 * @param value value
	 * @return HttpClientBuilder
	 */
	public JsonHttpClientBuilder addHeaderProperty(String name, String value) {
		headerOptions.put(name, value);
		return this;
	}
	/**
	 * JsonHttpClient生成
	 * @return JsonHttpClient
	 */
	public JsonHttpClient build(){
		return forceUtf8 ? new JsonHttpClientUtf8Impl(url, gsonbuilder, headerOptions) : new JsonHttpClientImpl(url, gsonbuilder, headerOptions);
	}
}
