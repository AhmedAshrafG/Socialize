package com.ahmedz.socialize.utils;

import android.net.Uri;
import android.text.format.DateUtils;



public class Util {
	public static String escapeEmail(String email) {
		return email.replaceAll("\\.", "***");
	}

	public static long getCurrentTime() {
		return System.currentTimeMillis();
	}

	public static String getLastSegmentOfURL(String deepLink) {
		String[] segments = deepLink.split("/");
		return segments[segments.length-1];
	}

	public static CharSequence convertTimestamp(long timestamp) {
		long currentTime = getCurrentTime();
		if (Math.abs(currentTime-timestamp) < 1000)
			return "Just now";
		else
			return DateUtils.getRelativeTimeSpanString(timestamp, currentTime, DateUtils.SECOND_IN_MILLIS);
	}

	public static boolean isValid(String str) {
		return str != null && !str.trim().isEmpty();
	}

	public static boolean isValid(Uri uri) {
		if (uri == null)
			return false;
		return isValid(uri.toString());
	}
}
