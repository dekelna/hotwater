package com.dekel.hotwater;

public class Logger {
	public static void log(String s) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
		System.out.println(ste.getClassName() + "::" + ste.getMethodName() + " - " + s);
	}
}
