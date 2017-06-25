package com.ahmedz.socialize.view;


import android.content.Context;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;



public class PicassoCache {

	/**
	 * Static Picasso Instance
	 */
	private static Picasso picassoInstance = null;
	private static Context appContext;

	/**
	 * PicassoCache Constructor
	 *
	 */
	private PicassoCache() {

		Downloader downloader   = new OkHttp3Downloader(appContext, Integer.MAX_VALUE);
		Picasso.Builder builder = new Picasso.Builder(appContext);
		builder.downloader(downloader);

		picassoInstance = builder.build();
	}

	/**
	 * Get Singleton Picasso Instance
	 *
	 * @return Picasso instance
	 */
	public static Picasso with() {

		if (picassoInstance == null) {

			new PicassoCache();
			return picassoInstance;
		}

		return picassoInstance;
	}

	public static void setContext(Context context) {
		appContext = context;
	}
}