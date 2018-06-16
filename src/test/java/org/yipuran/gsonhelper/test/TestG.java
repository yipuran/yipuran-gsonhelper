package org.yipuran.gsonhelper.test;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * TestG
 */
public class TestG{

	/**
	 * @param args
	 */
	public static void main(String[] args){
		Gson gson = new GsonBuilder().serializeNulls().create();

		Map<String, Object> map = gson.fromJson("{a:0}", new TypeToken<Map<String, Object>>(){}.getType());



		System.out.println(map);
	}

}
