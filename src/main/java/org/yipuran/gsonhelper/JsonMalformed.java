package org.yipuran.gsonhelper;

import java.util.function.Consumer;

/**
 * JsonMalformed. JSON書式エラー確認処理
 * <PRE>
 * JsonParser 、Gcon の fromJson で例外発生時、捕捉する Thowable に対して確認処理を行う。
 * JSON書式エラーで発生する MalformedJsonException を認識して
 * エラー行、カラム位置、JSON-Path ３つの引数の consumer である JsonErrorConsumer を実行する。
 * MalformedJsonException 以外のエラーは、Consumer&lt;Throwable&gt; を実行する。
 *
 * 注意：confirmメソッドは、MalformedJsonException 以外の例外発生で、true を返す。
 *
 *
 * （使用例）
 * JsonMalformed jsonmalformed = JsonMalformed.of((l, c, p)->{
 *       System.out.println("# line   = " + l );
 *       System.out.println("# column = " + c );
 *       System.out.println("# path   = " + p );
 * }, u->{
 *       System.out.println("# unknown :" + u.getClass().getName() + " " + + u.getMessage());
 * });
 * try{
 *    JsonReader reader = new JsonReader(new StringReader(<i>jsonstring</i>));
 *    new JsonParser().parse(reader);
 * }catch(Exception e){
 *    if (jsonmalformed.confirm(e)){
 *       // unknown exception
 *    }
 * }
 * </PRE>
 */
public class JsonMalformed{
	private JsonErrorConsumer c;
	private Consumer<Throwable> uc;

	private JsonMalformed(JsonErrorConsumer c,  Consumer<Throwable> uc){
		this.c = c;
		this.uc = uc;
	}
	/**
	 * JsonMalformed インスタンス生成.
	 * @param consumer JsonMalformed
	 * @param unknown MalformedJsonException 以外の例外捕捉 Consumer&lt;Throwable&gt;
	 * @return JsonMalformed
	 */
	public static JsonMalformed of(JsonErrorConsumer consumer, Consumer<Throwable> unknown){
		return new JsonMalformed(consumer, unknown);
	}
	/**
	 * 例外捕捉 MalformedJsonException確認.
	 * @param t 捕捉したThrowable
	 * @return true = MalformedJsonException 以外の例外発生
	 */
	public boolean confirm(Throwable t){
		return c.parse(t, uc);
	}
}
