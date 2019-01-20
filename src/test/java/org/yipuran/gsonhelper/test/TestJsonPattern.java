package org.yipuran.gsonhelper.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;

import org.yipuran.gsonhelper.JsonPattern;

/**
 * TestJsonPattern.java
 */
public class TestJsonPattern{
	public static void main(String[] args) throws IOException, URISyntaxException{
		JsonPattern pattern = new JsonPattern(getRreader("form.json"));
		boolean res = pattern.validate(readString("data.json"));
		System.out.println("res = " + res);

		System.out.println("---- unmatches ----");
		pattern.unmatches().stream().forEach(e->{
			System.out.println(e.getKey() + " --> " + e.getValue() );
		});

		System.out.println("---- ignores ----");
		pattern.ignores().stream().forEach(e->{
			System.out.println(e.getKey() + " --> " + e.getValue() );
		});

		System.out.println("---- getPatternMap ----");
		pattern.getPatternMap().entrySet().stream().forEach(e->{
			System.out.println(e.getKey() + " --> " + e.getValue() );
		});

		System.out.println("---- format ----");
		System.out.println(pattern.format());

	}
	static Reader getRreader(String filename) throws IOException, URISyntaxException{
		return new FileReader(new File(ClassLoader.getSystemClassLoader()
		.getResource(TestJsonPattern.class.getPackage().getName()
		.replaceAll("\\.", "/") + "/" + filename).toURI()));
	}
	static String readString(String filename) throws IOException, URISyntaxException{
		try(InputStream in = new FileInputStream(new File(ClassLoader.getSystemClassLoader()
				.getResource(TestJsonPattern.class.getPackage().getName().replaceAll("\\.", "/") + "/" + filename).toURI()));
			ByteArrayOutputStream bo = new ByteArrayOutputStream()){
			byte[] b = new byte[1024];
			int len;
			while((len = in.read(b, 0, b.length)) > 0){
				bo.write(b, 0, len);
			}
			bo.flush();
			return bo.toString();
		}
	}
	/*** for Java11 ***
	public static Reader getRreader(Class<?> cls, String filename) throws IOException, URISyntaxException{
		return new FileReader(
			new File(ClassLoader.getSystemClassLoader().getResource(
					cls.getPackageName().replaceAll("\\.", "/") + "/" + filename).toURI()
			), StandardCharsets.UTF_8);
	}
	public static String readString(Class<?> cls, String filename) throws IOException, URISyntaxException{
		try(InputStream in = new FileInputStream(new File(ClassLoader.getSystemClassLoader()
			.getResource(cls.getPackageName().replaceAll("\\.", "/") + "/" + filename).toURI()));
		){
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
	******************/
}
