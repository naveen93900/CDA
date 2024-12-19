package com.softgv.cda.util;

public class MyUtil {

	
	public static int getOTP() {
		double random=Math.random();
		return (int) (random*9000+1000);
		
	}
}
