package org.yipuran.gsonhelper.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.GsonBuilder;

/**
 * HTTPS通信用のJsonHttpClientを生成する。
 */
public final class JsonHttpsClientBuilder{
	private URL url;
	private String proxy_server;
	private String proxy_user;
	private String proxy_passwd;
	private Integer proxy_port;
	private GsonBuilder gsonbuilder;
	private boolean forceUtf8 = false;
	private Map<String, String> headerOptions;
	/**
	 * コンストラクタ.
	 * @param path 送信先URLパス
	 * @param gsonbuilder GsonBuilder 送信するObject をJSONにする為のGsonBuilder
	 */
	private JsonHttpsClientBuilder(String path, GsonBuilder gsonbuilder){
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
	public static JsonHttpsClientBuilder of(String path, GsonBuilder gsonbuilder){
		return new JsonHttpsClientBuilder(path, gsonbuilder);
	}
	/**
	 * プロキシ設定.
	 * @param proxyServer プロキシサーバ
	 * @param proxyPort プロキシポート番号
	 * @return JsonHttpsClientBuilder
	 */
	public JsonHttpsClientBuilder setProxy(String proxyServer, int proxyPort){
		this.proxy_server = proxyServer;
		this.proxy_port = proxyPort;
		return this;
	}
	/**
	 * プロキシユーザ設定
	 * @param user プロキシユーザ
	 * @param passwd プロキシパスワード
	 * @return JsonHttpsClientBuilder
	 */
	public JsonHttpsClientBuilder setProxyUser(String user, String passwd){
		this.proxy_user = user;
		this.proxy_passwd = passwd;
		return this;
	}
	/**
	 * 生成されるJsonHttpClient の受信JSON出力を強制的にUTF-8変換して出力
	 */
	public JsonHttpsClientBuilder setForceUtf8(){
		this.forceUtf8 = true;
		return this;
	}
	/**
	 * Header property 追加.
	 * @param name key
	 * @param value value
	 * @return HttpClientBuilder
	 */
	public JsonHttpsClientBuilder addHeaderProperty(String name, String value) {
		headerOptions.put(name, value);
		return this;
	}
	/**
	 * JsonHttpClient生成 HTTPS用
	 * @return JsonHttpClient
	 */
	public JsonHttpClient build(){
		if (forceUtf8){
			if (proxy_server != null) {
				return new JsonHttpsClientUtf8Impl(url, proxy_server, proxy_user, proxy_passwd, proxy_port, gsonbuilder, headerOptions);
			}
			return new JsonHttpsClientUtf8Impl(url, gsonbuilder, headerOptions);
		}
		return proxy_server==null ? new JsonHttpsClientImpl(url, gsonbuilder, headerOptions) : new JsonHttpsClientImpl(url, proxy_server, proxy_user, proxy_passwd, proxy_port, gsonbuilder, headerOptions);
	}
}
