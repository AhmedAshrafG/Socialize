package com.ahmedz.socialize.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ahmed on 28-Jun-17.
 */

public class AppPreference {
	private final static String UNIQUE_KEY = "UniqueKey";
	private static AppPreference instance;
	private final SharedPreferences prefs;

	public static AppPreference getInstance(Context appContext) {
		if (instance == null)
			return instance = new AppPreference(appContext);
		return instance;
	}

	private AppPreference(Context appContext) {
		prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
	}

	public int getUniqueKey() {
		int lastKey = getLastKey();
		saveLastKey(lastKey + 1);
		return lastKey + 1;
	}

	private void saveLastKey(int key) {
		prefs.edit().putInt(UNIQUE_KEY, key).apply();
	}

	private int getLastKey() {
		return prefs.getInt(UNIQUE_KEY, 0);
	}
}
