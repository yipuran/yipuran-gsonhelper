package org.yipuran.gsonhelper.serialize;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import org.yipuran.gsonhelper.Exclude;
import org.yipuran.gsonhelper.JsonPath;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;

/**
 * @JsonPath によるシリアライザ.
 * <PRE>
 * フィールドに付与した@JsonPath により、階層化してシリアライズする。
 * （使い方）
 * シリアライズ対象クラスを指定して GsonBuilder の registerTypeAdapter で指定する。
 * Gson gson = new GsonBuilder().serializeNulls()
 * 				.registerTypeAdapter(Data.class, new AutoPathSerializer<Data>())
 * 				.setPrettyPrinting()
 * 				.create();
 * </PRE>
 * @since 4.17
 */
public class AutoPathSerializer<T> implements JsonSerializer<T>{

	@Override
	public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context){
		Map<String, JsonObject> jmap = new HashMap<>();
		JsonObject jo = new JsonObject();
		jmap.put("$", jo);
		Field[] fields = src.getClass().getDeclaredFields();
		for(Field f:fields) {
			if (f.getAnnotation(Exclude.class) != null) continue;
			try{
				f.setAccessible(true);
				Object obj = f.get(src);

				JsonPath jpath = f.getAnnotation(JsonPath.class);
				if (jpath != null) {
					List<String> plist = tokenToList(jpath.value(), '.', '\\');
					String path = plist.remove(0);
					for(String t:plist){
						JsonObject parent = jmap.get(path);
						path = path + "." + t;
						JsonObject jt = jmap.get(path);
						if (jt==null) {
							jmap.put(path, new JsonObject());
						}
						jt = jmap.get(path);
						parent.add(t, jt);
					}
					JsonObject addjo = jmap.get(path);
					if (obj instanceof String) {
						addjo.addProperty(Optional.ofNullable(f.getAnnotation(SerializedName.class)).map(e->e.value()).orElse(f.getName())
								, Optional.ofNullable(obj).map(e->e.toString()).orElse(null));
					}else {
						addjo.add(Optional.ofNullable(f.getAnnotation(SerializedName.class)).map(e->e.value()).orElse(f.getName())
								, context.serialize(obj, f.getType()));
					}
					jmap.put(path, addjo);
				}else{
					if (obj instanceof String) {
						jo.addProperty(Optional.ofNullable(f.getAnnotation(SerializedName.class)).map(e->e.value()).orElse(f.getName())
								, Optional.ofNullable(obj).map(e->e.toString()).orElse(null));
					}else {
						jo.add(Optional.ofNullable(f.getAnnotation(SerializedName.class)).map(e->e.value()).orElse(f.getName())
								, context.serialize(obj, f.getType()));
					}
				}
			}catch(IllegalAccessException e){
				throw new RuntimeException(e);
			}
		}
		return jo;
	}
	private List<String> tokenToList(String str, char sep, char escape){
		List<String> list = new ArrayList<>();
		String sp = new String(new char[]{ sep });
		String escapes = new String(new char[]{ escape, escape });
		StringTokenizer st = new StringTokenizer(str, sp, true);
		String s = "";
		while (st.hasMoreTokens()){
			String c = st.nextToken();
			if (c.equals(sp)) {
				if (s.charAt(s.length()-1)==escape) {
					s += c;
				}else{
					list.add(s.replaceAll(escapes, ""));
					s = "";
				}
			}else {
				s += c;
			}
		}
		list.add(s.replaceAll(escapes, ""));
		return list;
	}
}

