package org.yipuran.gsonhelper.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * gson fromJson で作成した Map＜String, Object＞ を閲覧する処理クラス.
 * <PRE>
 * json パスでネストの深い位置の値の取得、配列インデックス [n] による配列が指す１つの値の取得を
 * Generic（総称型）メソッドで取得できる。
 *             public <T> T search(String...keys) ： JSONキーをJSONキー構造に沿って並べて指定
 *             public <T> T search(String path, Character separator) ： JSONパスとパスの区切り文字を指定
 * 検索で取得できるオブジェクトは、
 *     String
 *     Long または、long
 *     Double または、double
 *     boolean または、Boolean
 *     List＜Object＞
 *     Map＜String, Object＞
 * のいずれかである。
 * 誤ったJSONパスを指定して参照できない場合、IllegalArgumentException あるいは、
 * 配列で誤ったインデックス値を指定すれば、IndexOutOfBoundsException が発生する
 *
 * 対象にするMapは、gson の fromJson で生成するとき、GenericMapDeserializer を使用しているのが前提である。
 *
 * （例）
 *      Gson gson = new GsonBuilder()
 *      .registerTypeAdapter(new TypeToken＜Map＜String, Object＞＞(){}.getType(), new GenericMapDeserializer())
 *      .serializeNulls()
 *      .create();
 *      Map＜String, Object＞ map = gson.fromJson(string, new TypeToken＜Map＜String, Object＞＞(){}.getType());
 *      StringMapObject sm = StringMapObject.of(map);
 *
 *      long value = sm.search("area", "A", "length");
 *      long value = sm.search("area.A.length", '.');
 *      double value = sm.search("area/A/width", '/');
 *      boolean check = sm.search("area/A/alpha", '/');
 *      String name = sm.search("area/A/name", '/');
 *      String postname = sm.search("area.A.post[2]", '.');
 *
 * </PRE>
 * @since Version 4.22
 */
public final class StringMapObject{
	private Map<String, Object> map;
	private Pattern aryptn;
	private Pattern idexptn;
	/**
	 * コンストラクタ.
	 * @param map Map<String, Object>
	 */
	public StringMapObject(Map<String, Object> map){
		this.map = map;
		aryptn = Pattern.compile("^.+\\[\\d+\\]$");
		idexptn = Pattern.compile("\\[\\d+\\]$");
	}
	/**
	 * インスタンス生成
	 * @param map Map<String, Object>
	 * @return StringMapObject
	 */
	public static StringMapObject of(Map<String, Object> map){
		return new StringMapObject(map);
	}
	/**
	 * 検索（path指定）.
	 * @param path JSONパス
	 * @param separator JSONパス指定の区切り文字
	 * @return T
	 */
	public <T> T search(String path, Character separator){
		if (".-+*^$?{}[]()".contains(separator.toString())) {
			return search(path.split("\\"+ separator.toString()));
		}
		return search(path.split(separator.toString()));
	}
	/**
	 * 検索（path配列指定）.
	 * @param keys JSONパス配列
	 * @return T
	 */
	@SuppressWarnings("rawtypes")
	public <T> T search(String...keys){
		Iterator<String> it = Arrays.asList(keys).iterator();
		String key = it.next();
		if (aryptn.matcher(key).matches()){
			Object o = map.get(key.replaceAll("\\[\\d+\\]$", ""));
			if (!(o instanceof List)){
				throw new IllegalArgumentException(key + " get Obejct is not List");
			}
			Matcher matcher = idexptn.matcher(key);
			matcher.find();
			int i = Integer.valueOf(matcher.group().replaceFirst("\\[", "").replaceFirst("\\]", ""));
			return deep(it, ((List)o).get(i));
		}
		if (!map.containsKey(key)) {
			throw new IllegalArgumentException(key + " is not contains Key");
		}
		Object o = map.get(key);
		return deep(it, o);
	}

	@SuppressWarnings("unchecked")
	private <T> T deep(Iterator<String> it, Object obj){
		if (it.hasNext()){
			String key = it.next();
			if (aryptn.matcher(key).matches()){
				Object o = ((Map<String, Object>)obj).get(key.replaceAll("\\[\\d+\\]$", ""));
				if (!(o instanceof List)){
					throw new IllegalArgumentException(key + " get Obejct is not List");
				}
				Matcher matcher = idexptn.matcher(key);
				matcher.find();
				int i = Integer.valueOf(matcher.group().replaceFirst("\\[", "").replaceFirst("\\]", ""));
				return deep(it, ((List<Object>)o).get(i));
			}
			if (!(obj instanceof Map)){
				throw new IllegalArgumentException(key + " is not Path key");
			}
			Object o = ((Map<String, Object>)obj).get(key);
			return deep(it, o);
		}
		return obj==null ? null : (T)obj;
	}
	/**
	 * マップへのObject追加
	 * @param key
	 * @param obj
	 */
	public void put(String key, Object obj){
		map.put(key, obj);
	}
	/**
	 * 現在のMapを取得
	 * @return
	 */
	public Map<String, Object> getMap(){
		return map;
	}
}
