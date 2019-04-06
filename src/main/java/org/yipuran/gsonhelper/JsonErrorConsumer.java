package org.yipuran.gsonhelper;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;

/**
 * JsonErrorConsumer. JSONエラーMalformedJsonException 解析インターフェース
 * JsonMalformed JSON書式エラー確認処理で関数型インターフェースとして指定する。
 * <PRE>
 * （使用例）
 * JsonMalformed jsonmalformed = JsonMalformed.of((l, c, p, t)->{
 *       System.out.println("# line   = " + l );
 *       System.out.println("# column = " + c );
 *       System.out.println("# path   = " + p );
 *       System.out.println("# detail = " + t );
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
 *
 * 注意：JsonMalformed の confirmメソッドは、MalformedJsonException 以外の例外発生で、true を返す。
 *
 * </PRE>
 */
@FunctionalInterface
public interface JsonErrorConsumer{
	/**
	 * MalformedJsonException 特定解析処理.
	 * @param l エラー行番号
	 * @param c エラーカラム位置
	 * @param p JSONパス文字列
	 * @param t JsonErrorType
	 */
	void accept(Integer l, Integer c, String p, JsonErrorType t);

	default boolean parse(Throwable th, Consumer<Throwable> u){
		if (th instanceof JsonSyntaxException){
			Throwable cause = th.getCause();
			if (cause==null){
				u.accept(th);
				return true;
			}else if(cause instanceof MalformedJsonException){
				String msg = cause.getMessage();
				if (Pattern.compile("^.+ at line [0-9]+ column [0-9]+ path .+$").matcher(msg).matches()){
					Matcher matcher = Pattern.compile("[0-9]+").matcher(msg);
					matcher.find();
					int line = Integer.parseInt(matcher.group());
					matcher.find();
					int column = Integer.parseInt(matcher.group());
					String path = msg.replaceFirst(".+ at line [0-9]+ column [0-9]+ path ", "");
					accept(line, column, path,	JsonErrorType.parse(msg));
				}
				return false;
			}else{
				u.accept(cause);
			}
		}else{
			u.accept(th);
		}
		return true;
	}
}
