package org.yipuran.gsonhelper.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.yipuran.gsonhelper.GenericMapDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * JsonHttpClient 実装 HTTPS 版.
 */
class JsonHttpsClientImpl implements JsonHttpClient{
	private URL url;
	private String proxy_server;
	private String proxy_user;
	private String proxy_passwd;
	private Integer proxy_port;
	private GsonBuilder gsonbuilder;
	private int httpresponsecode;
	/**
	 * コンストラクタ HTTPS 版.
	 * @param url HTTP先URL
	 * @param method HTTPメソッド
	 */
	protected JsonHttpsClientImpl(URL url, GsonBuilder gsonbuilder){
		this.url = url;
		this.gsonbuilder = gsonbuilder;
	}
	/**
	 * コンストラクタ（Proxy指定） HTTPS 版.
	 * @param url HTTP先URL
	 * @param method HTTPメソッド
	 * @param proxy_server Proxyサーバ名
	 * @param proxy_user Proxyユーザ名
	 * @param proxy_passwd Proxyパスワード
	 * @param proxy_port Proxyポート番号
	 */
	protected JsonHttpsClientImpl(URL url, String proxy_server, String proxy_user, String proxy_passwd, Integer proxy_port, GsonBuilder gsonbuilder){
		this.url = url;
		this.proxy_server = proxy_server;
		this.proxy_user = proxy_user;
		this.proxy_passwd = proxy_passwd;
		this.proxy_port = proxy_port;
		this.gsonbuilder = gsonbuilder;
	}
	/**
	 * 受信→整形していないJSON
	 * @param object JSONにして送信する対象
	 * @param headconsumer Content-typeとHTTPヘッダ受信マップのBiConsumer
	 * @param bodyconsumer 受信したJSON文字列
	 */
	@Override
	public void execute(Object object, BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<String> bodyconsumer){
		Gson gson = gsonbuilder.create();
		String jsonstr = gson.toJson(object);
		try{
			SSLContext ctx = SSLContext.getInstance("SSL");
			ctx.init(null, new X509TrustManager[]{ new NonAuthentication() }, null);
			SSLSocketFactory factory = ctx.getSocketFactory();

			HttpsURLConnection uc;
			if (proxy_server != null){
				// Proxy利用
				if (proxy_user != null && proxy_passwd != null){
					Authenticator.setDefault(new Authenticator(){
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(proxy_user, proxy_passwd.toCharArray());
						}
					});
				}
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_server, Optional.ofNullable(proxy_port).orElse(80)));
				uc = (HttpsURLConnection)url.openConnection(proxy);
			}else{
				uc = (HttpsURLConnection)url.openConnection();
			}
			uc.setSSLSocketFactory(factory);
			uc.setDoOutput(true);
			uc.setReadTimeout(0);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Content-Type", "application/json");
			uc.setRequestProperty("Content-Length", Integer.toString(jsonstr.getBytes("utf8").length));
			uc.connect();
			OutputStreamWriter osw = new OutputStreamWriter(uc.getOutputStream(), "utf8");
			osw.write(jsonstr);
			osw.flush();
			httpresponsecode = uc.getResponseCode();
			if (httpresponsecode != 200){
				throw new RuntimeException("HTTP response " + httpresponsecode);
			}
			headconsumer.accept(uc.getContentType(), uc.getHeaderFields());

			try(InputStream in = uc.getInputStream();ByteArrayOutputStream bo = new ByteArrayOutputStream()){
				byte[] buf = new byte[1024];
				int n;
				while((n = in.read(buf)) >= 0){
					bo.write(buf, 0, n);
				}
				bo.flush();
				bodyconsumer.accept(bo.toString());
			}
		}catch(Exception e){
		   throw new RuntimeException(e);
		}
	}

	/**
	 * 受信→整形したJSON
	 * <PRE>
	 * （注意）送受信前に、Gsonbuilderは、registerTypeAdapter で GenericMapDeserializer が登録されて、setPrettyPrinting() が実行される。
	 * </PRE>
	 * @param object JSONにして送信する対象
	 * @param headconsumer Content-typeとHTTPヘッダ受信マップのBiConsumer
	 * @param jsonconsumer 受信→整形したJSONの Consumer
	 */@Override
	public void executePretty(Object object, BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<String> jsonconsumer){
		 Gson gson = gsonbuilder
					.registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(), new GenericMapDeserializer())
					.setPrettyPrinting().create();
		String jsonstr = gson.toJson(object);
		try{
			SSLContext ctx = SSLContext.getInstance("SSL");
			ctx.init(null, new X509TrustManager[]{ new NonAuthentication() }, null);
			SSLSocketFactory factory = ctx.getSocketFactory();

			HttpsURLConnection uc;
			if (proxy_server != null){
				// Proxy利用
				if (proxy_user != null && proxy_passwd != null){
					Authenticator.setDefault(new Authenticator(){
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(proxy_user, proxy_passwd.toCharArray());
						}
					});
				}
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_server, Optional.ofNullable(proxy_port).orElse(80)));
				uc = (HttpsURLConnection)url.openConnection(proxy);
			}else{
				uc = (HttpsURLConnection)url.openConnection();
			}
			uc.setSSLSocketFactory(factory);
			uc.setDoOutput(true);
			uc.setReadTimeout(0);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Content-Type", "application/json");
			uc.setRequestProperty("Content-Length", Integer.toString(jsonstr.getBytes("utf8").length));
			uc.connect();
			OutputStreamWriter osw = new OutputStreamWriter(uc.getOutputStream(), "utf8");
			osw.write(jsonstr);
			osw.flush();
			httpresponsecode = uc.getResponseCode();
			if (httpresponsecode != 200){
				throw new RuntimeException("HTTP response " + httpresponsecode);
			}
			headconsumer.accept(uc.getContentType(), uc.getHeaderFields());

			try(InputStream in = uc.getInputStream();ByteArrayOutputStream bo = new ByteArrayOutputStream()){
				byte[] buf = new byte[1024];
				int n;
				while((n = in.read(buf)) >= 0){
					bo.write(buf, 0, n);
				}
				bo.flush();
				Map<String, Object> map = gson.fromJson(bo.toString(), new TypeToken<Map<String, Object>>(){}.getType());
				jsonconsumer.accept(gson.toJson(map));
			}
		}catch(Exception e){
		   throw new RuntimeException(e);
		}
	}

	/**
	 * 受信→未整形JSONと整形JSON
	 * <PRE>
	 * （注意）送受信前に、Gsonbuilderは、registerTypeAdapter で GenericMapDeserializer が登録されて、setPrettyPrinting() が実行される。
	 * </PRE>
	 * @param object JSONにして送信する対象
	 * @param headconsumer Content-typeとHTTPヘッダ受信マップのBiConsumer
	 * @param bodyconsumer 受信したJSON文字列
	 * @param jsonconsumer 受信→整形したJSONの Consumer
	 */
	@Override
	public void execute(Object object, BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<String> bodyconsumer, Consumer<String> jsonconsumer){
		Gson gson = gsonbuilder
				.registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(), new GenericMapDeserializer())
				.setPrettyPrinting().create();
		String jsonstr = gson.toJson(object);
		try{
			SSLContext ctx = SSLContext.getInstance("SSL");
			ctx.init(null, new X509TrustManager[]{ new NonAuthentication() }, null);
			SSLSocketFactory factory = ctx.getSocketFactory();

			HttpsURLConnection uc;
			if (proxy_server != null){
				// Proxy利用
				if (proxy_user != null && proxy_passwd != null){
					Authenticator.setDefault(new Authenticator(){
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(proxy_user, proxy_passwd.toCharArray());
						}
					});
				}
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_server, Optional.ofNullable(proxy_port).orElse(80)));
				uc = (HttpsURLConnection)url.openConnection(proxy);
			}else{
				uc = (HttpsURLConnection)url.openConnection();
			}
			uc.setSSLSocketFactory(factory);
			uc.setDoOutput(true);
			uc.setReadTimeout(0);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Content-Type", "application/json");
			uc.setRequestProperty("Content-Length", Integer.toString(jsonstr.getBytes("utf8").length));
			uc.connect();
			OutputStreamWriter osw = new OutputStreamWriter(uc.getOutputStream(), "utf8");
			osw.write(jsonstr);
			osw.flush();
			httpresponsecode = uc.getResponseCode();
			if (httpresponsecode != 200){
				throw new RuntimeException("HTTP response " + httpresponsecode);
			}
			headconsumer.accept(uc.getContentType(), uc.getHeaderFields());

			try(InputStream in = uc.getInputStream();ByteArrayOutputStream bo = new ByteArrayOutputStream()){
				byte[] buf = new byte[1024];
				int n;
				while((n = in.read(buf)) >= 0){
					bo.write(buf, 0, n);
				}
				bo.flush();
				String body = bo.toString();
				bodyconsumer.accept(body);
				Map<String, Object> map = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());
				jsonconsumer.accept(gson.toJson(map));
			}
		}catch(Exception e){
		   throw new RuntimeException(e);
		}
	}
	/**
	 * 受信→未整形JSONと整形JSON を BiConsumer＜未整形JSON, 整形JSON＞で処理.
	 * <PRE>
	 * （注意）送受信前に、Gsonbuilderは、registerTypeAdapter で GenericMapDeserializer が登録されて、setPrettyPrinting() が実行される。
	 * </PRE>
	 * @param object JSONにして送信する対象
	 * @param headconsumer Content-typeとHTTPヘッダ受信マップのBiConsumer
	 * @param consumer BiConsumer＜未整形JSON, 整形JSON＞
	 */
	@Override
	public void execute(Object object, BiConsumer<String, Map<String, List<String>>> headconsumer, BiConsumer<String, String> consumer){
		Gson gson = gsonbuilder
				.registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(), new GenericMapDeserializer())
				.setPrettyPrinting().create();
		String jsonstr = gson.toJson(object);
		try{
			SSLContext ctx = SSLContext.getInstance("SSL");
			ctx.init(null, new X509TrustManager[]{ new NonAuthentication() }, null);
			SSLSocketFactory factory = ctx.getSocketFactory();

			HttpsURLConnection uc;
			if (proxy_server != null){
				// Proxy利用
				if (proxy_user != null && proxy_passwd != null){
					Authenticator.setDefault(new Authenticator(){
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(proxy_user, proxy_passwd.toCharArray());
						}
					});
				}
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_server, Optional.ofNullable(proxy_port).orElse(80)));
				uc = (HttpsURLConnection)url.openConnection(proxy);
			}else{
				uc = (HttpsURLConnection)url.openConnection();
			}
			uc.setSSLSocketFactory(factory);
			uc.setDoOutput(true);
			uc.setReadTimeout(0);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Content-Type", "application/json");
			uc.setRequestProperty("Content-Length", Integer.toString(jsonstr.getBytes("utf8").length));
			uc.connect();
			OutputStreamWriter osw = new OutputStreamWriter(uc.getOutputStream(), "utf8");
			osw.write(jsonstr);
			osw.flush();
			httpresponsecode = uc.getResponseCode();
			if (httpresponsecode != 200){
				throw new RuntimeException("HTTP response " + httpresponsecode);
			}
			headconsumer.accept(uc.getContentType(), uc.getHeaderFields());

			try(InputStream in = uc.getInputStream();ByteArrayOutputStream bo = new ByteArrayOutputStream()){
				byte[] buf = new byte[1024];
				int n;
				while((n = in.read(buf)) >= 0){
					bo.write(buf, 0, n);
				}
				bo.flush();
				String body = bo.toString();
				Map<String, Object> map = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());
				consumer.accept(body, gson.toJson(map));
			}
		}catch(Exception e){
		   throw new RuntimeException(e);
		}
	}
	/* @see org.yipuran.gsonhelper.http.JsonHttpClient#getHttpresponsecode() */
	@Override
	public int getHttpresponsecode(){
		return httpresponsecode;
	}
}