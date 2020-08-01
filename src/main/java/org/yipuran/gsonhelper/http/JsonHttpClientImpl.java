package org.yipuran.gsonhelper.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.yipuran.gsonhelper.serialize.GenericMapDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * JsonHttpClient 実装.
 */
class JsonHttpClientImpl implements JsonHttpClient{
	private URL url;
	private GsonBuilder gsonbuilder;
	private int httpresponsecode;
	private Map<String, String> headerOptions;

	/**
	 * コンストラクタ.
	 * @param url URL
	 * @param gsonbuilder GsonBuilder
	 */
	protected JsonHttpClientImpl(URL url, GsonBuilder gsonbuilder, Map<String, String> headerOptions){
		this.url = url;
		this.gsonbuilder = gsonbuilder;
		this.headerOptions = headerOptions;
	}

	/**
	 * 受信→整形していないJSON
	 * @param object JSONにして送信する対象
	 * @param headconsumer Content-typeとHTTPヘッダ受信マップのBiConsumer
	 * @param bodyconsumer 受信したJSON文字列
	 */
	@Override
	public void execute(Object object,BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<String> bodyconsumer){
		Gson gson = gsonbuilder.create();
		String jsonstr = gson.toJson(object);
		try{
			HttpURLConnection uc = (HttpURLConnection)url.openConnection();
			uc.setDoOutput(true);
			uc.setReadTimeout(0);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Content-Type", "application/json");
			uc.setRequestProperty("Content-Length", Integer.toString(jsonstr.getBytes("utf8").length));
			if (headerOptions.size() > 0) {
				headerOptions.entrySet().stream().forEach(e->{
					uc.setRequestProperty(e.getKey(), e.getValue());
				});
			}
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
	@Override
	public void executePretty(Object object,BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<String> jsonconsumer){
		Gson gson = gsonbuilder
				.registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(), new GenericMapDeserializer())
				.setPrettyPrinting().create();
		String jsonstr = gson.toJson(object);
		try{
			HttpURLConnection uc = (HttpURLConnection)url.openConnection();
			uc.setDoOutput(true);
			uc.setReadTimeout(0);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Content-Type", "application/json");
			uc.setRequestProperty("Content-Length", Integer.toString(jsonstr.getBytes("utf8").length));
			if (headerOptions.size() > 0) {
				headerOptions.entrySet().stream().forEach(e->{
					uc.setRequestProperty(e.getKey(), e.getValue());
				});
			}
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
	public void execute(Object object,BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<String> bodyconsumer, Consumer<String> jsonconsumer){
		Gson gson = gsonbuilder
				.registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(), new GenericMapDeserializer())
				.setPrettyPrinting().create();
		String jsonstr = gson.toJson(object);
		try{
			HttpURLConnection uc = (HttpURLConnection)url.openConnection();
			uc.setDoOutput(true);
			uc.setReadTimeout(0);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Content-Type", "application/json");
			uc.setRequestProperty("Content-Length", Integer.toString(jsonstr.getBytes("utf8").length));
			if (headerOptions.size() > 0) {
				headerOptions.entrySet().stream().forEach(e->{
					uc.setRequestProperty(e.getKey(), e.getValue());
				});
			}
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
	public void execute(Object object,BiConsumer<String, Map<String, List<String>>> headconsumer, BiConsumer<String, String> consumer){
		Gson gson = gsonbuilder
				.registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(), new GenericMapDeserializer())
				.setPrettyPrinting().create();
		String jsonstr = gson.toJson(object);
		try{
			HttpURLConnection uc = (HttpURLConnection)url.openConnection();
			uc.setDoOutput(true);
			uc.setReadTimeout(0);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Content-Type", "application/json");
			uc.setRequestProperty("Content-Length", Integer.toString(jsonstr.getBytes("utf8").length));
			if (headerOptions.size() > 0) {
				headerOptions.entrySet().stream().forEach(e->{
					uc.setRequestProperty(e.getKey(), e.getValue());
				});
			}
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

	/* @see org.yipuran.gsonhelper.http.JsonHttpClient#getHttpresponsecode()
	 */
	@Override
	public int getHttpresponsecode(){
		return httpresponsecode;
	}
	/**
	 * JsonReader で受信
	 * @param object JSONにして送信する対象
	 * @param headconsumer Content-typeとHTTPヘッダ受信マップのBiConsumer
	 * @param jsonreadconsumer JsonReader
	 * @return HTTPステータス
	 */
	@Override
	public int executeJsonread(Object object, BiConsumer<String, Map<String, List<String>>> headconsumer, Consumer<JsonReader> jsonreadconsumer){
		Gson gson = gsonbuilder
				.registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(), new GenericMapDeserializer())
				.setPrettyPrinting().create();
		String jsonstr = gson.toJson(object);
		int status = 0;
		try{
			HttpURLConnection uc = (HttpURLConnection)url.openConnection();
			uc.setDoOutput(true);
			uc.setReadTimeout(0);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Content-Type", "application/json");
			uc.setRequestProperty("Content-Length", Integer.toString(jsonstr.getBytes("utf8").length));
			if (headerOptions.size() > 0) {
				headerOptions.entrySet().stream().forEach(e->{
					uc.setRequestProperty(e.getKey(), e.getValue());
				});
			}
			uc.connect();
			OutputStreamWriter osw = new OutputStreamWriter(uc.getOutputStream(), "utf8");
			osw.write(jsonstr);
			osw.flush();
			status = uc.getResponseCode();
			if (httpresponsecode != 200){
				throw new RuntimeException("HTTP response " + httpresponsecode);
			}
			headconsumer.accept(uc.getContentType(), uc.getHeaderFields());

			try(InputStream in = uc.getInputStream();InputStreamReader ir = new InputStreamReader(in, StandardCharsets.UTF_8);
				JsonReader jr = new JsonReader(ir)){
				jsonreadconsumer.accept(jr);
			}
		}catch(Exception e){
		   throw new RuntimeException(e);
		}
		return status;
	}
}
