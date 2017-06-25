package com.ahmedz.socialize.handler;

import android.app.Activity;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observable;



class RxPermissionManager {

	private final RxPermissions rxPermissions;

	public RxPermissionManager(Activity activity) {
		rxPermissions = new RxPermissions(activity);
	}

	public Observable<Boolean> requestPermissions(String[] permissions) {
		return rxPermissions.request(permissions);
	}
}
