package com.ahmedz.socialize.handler;

import android.content.Context;



public class GlobalState {

	private static GlobalState instance;
	private Context appContext;
	private boolean chatActive;

	public boolean isChatActive() {
		return chatActive;
	}

	public void setChatActive(boolean flag) {
		chatActive = flag;
	}

	public static GlobalState getInst() {
		if (instance == null)
			instance = new GlobalState();
		return instance;
	}

	private GlobalState() {
		this.chatActive = false;
	}

	public void setAppContext(Context appContext) {
		this.appContext = appContext;
	}

	public Context getAppContext() {
		return appContext;
	}
}
