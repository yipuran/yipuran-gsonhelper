package org.yipuran.gsonhelper.test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.yipuran.gsonhelper.LocalDateAdapter;
import org.yipuran.gsonhelper.LocalDateTimeAdapter;
import org.yipuran.gsonhelper.LocalTimeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * TestDate.java
 */
public class TestDate{
	public static void main(String[] args){

		Gson gson = new GsonBuilder().serializeNulls()
				.registerTypeAdapter(new TypeToken<LocalDate>(){}.getType(), LocalDateAdapter.of("yyyy/MM/dd"))
				.registerTypeAdapter(new TypeToken<LocalDateTime>(){}.getType(), LocalDateTimeAdapter.of("yyyy/MM/dd HH:mm:ss"))
				.registerTypeAdapter(new TypeToken<LocalTime>(){}.getType(), LocalTimeAdapter.of("HH:mm"))
				.create();

		Foo foo = gson.fromJson("{date:'2019/03/06',datetime:'2019/03/06 21:02:33',time:'20:54'}", new TypeToken<Foo>(){}.getType());

		System.out.println(foo.date);
		System.out.println(foo.datetime);
		System.out.println(foo.time);

	}

}
