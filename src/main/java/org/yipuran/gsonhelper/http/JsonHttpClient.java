package org.yipuran.gsonhelper.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.yipuran.gsonhelper.GenericMapDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * HTTP JSON クライアント.
 *  <PRE>
 * （使用例）
 * String path;  // 送信先URL
 * GsonBuilder gsonbuilder = new Gsonbuilder().serializeNulls();
 * JsonHttpClient client = JsonHttpClientBuilder.of(path, gsonbuilder);
 *
 * // (1) 受信→整形していないJSON
 * int status = client.execute(object, (type, hmap)->{
 * 	// type = Content-type
 *    // hmap = HTTPヘッダマップ
 * }, body->{
 *    // body = HTTP受信 contents 文字列
 * });
 *
 * // (2) 受信→整形したJSON
 * int status = client.executePretty(object, (type, hmap)->{
 * 	// type = Content-type
 *    // hmap = HTTPヘッダマップ
 * }, s->{
 *    // s = 整形したJSON
 * });
 *
 * // (3) 受信→未整形JSONと整形JSON を別々のConsumerで処理
 * int status = client.execute(object, (type, hmap)->{
 * 	// type = Content-type
 *    // hmap = HTTPヘッダマップ
 * }, body->{
 *    // body = HTTP受信 contents 文字列
 * }, s->{
 *    // s = 整形したJSON
 * });
 *
 * // (4) 受信→未整形JSONと整形JSON を BiConsumer＜未整形JSON, 整形JSON＞で処理
 * int status = client.execute(object, (type, hmap)->{
 * 	// type = Content-type
 *    // hmap = HTTPヘッダマップ
 * }, (b, s)->{
 *    // b = 未整形JSON
 *    // s = 整形したJSON
 * });
 *
 * （注意）(2)～(4) は、送受信前に Gsonbuilderは、registerTypeAdapter で GenericMapDeserializer が登録されて、setPrettyPrinting() が実行される。
 *  </PRE>
 */
public class JsonHttpClient{
	private URL url;
	private GsonBuilder gsonbuilder;
	private int httpresponsecode;

	/**
	 * コンストラクタ.
	 * @param url URL
	 * @param gsonbuilder GsonBuilder
	 */
	protected JsonHttpClient(URL url, GsonBuilder gsonbuilder){
		this.url = url;
		this.gsonbuilder = gsonbuilder;
	}

	/**
	 * 受信→整形していないJSON
	 * @param object JSONにして送信する対象
	 * @param headconsumer Content-typeとHTTPヘッダ受信マップのBiConsumer
	 * @param bodyconsumer 受信したJSON文字列
	 */
	public void execute(Object object,BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<String> bodyconsumer){
		Gson gson = gsonbuilder.create();
		String jsonstr = gson.toJson(object);
		try{
			HttpURLConnection uc = (HttpURLConnection)url.openConnection();
			uc = (HttpURLConnection)url.openConnection();
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
	 */
	public void executePretty(Object object,BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<String> jsonconsumer){
		Gson gson = gsonbuilder
				.registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(), new GenericMapDeserializer())
				.setPrettyPrinting().create();
		String jsonstr = gson.toJson(object);
		try{
			HttpURLConnection uc = (HttpURLConnection)url.openConnection();
			uc = (HttpURLConnection)url.openConnection();
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
	public void execute(Object object,BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<String> bodyconsumer, Consumer<String> jsonconsumer){
		Gson gson = gsonbuilder
				.registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(), new GenericMapDeserializer())
				.setPrettyPrinting().create();
		String jsonstr = gson.toJson(object);
		try{
			HttpURLConnection uc = (HttpURLConnection)url.openConnection();
			uc = (HttpURLConnection)url.openConnection();
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
	public void execute(Object object,BiConsumer<String, Map<String, List<String>>> headconsumer, BiConsumer<String, String> consumer){
		Gson gson = gsonbuilder
				.registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(), new GenericMapDeserializer())
				.setPrettyPrinting().create();
		String jsonstr = gson.toJson(object);
		try{
			HttpURLConnection uc = (HttpURLConnection)url.openConnection();
			uc = (HttpURLConnection)url.openConnection();
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

}
