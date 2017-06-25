package com.ahmedz.socialize.activity;

import android.app.Application;

import com.ahmedz.socialize.handler.GlobalState;
import com.ahmedz.socialize.view.PicassoCache;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import rx_activity_result2.RxActivityResult;




public class SocializeApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		RxActivityResult.register(this);
		PicassoCache.setContext(getApplicationContext());
		GlobalState.getInst().setAppContext(getApplicationContext());
		FacebookSdk.sdkInitialize(getApplicationContext());
		AppEventsLogger.activateApp(this);
	}
}
