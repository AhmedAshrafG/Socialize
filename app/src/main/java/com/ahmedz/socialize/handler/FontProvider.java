package com.ahmedz.socialize.handler;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.TextInputLayout;
import android.widget.TextView;


public class FontProvider {
	private static FontProvider instance;
	private final Typeface font;

	private FontProvider() {
		Context context = GlobalState.getInst().getAppContext();
		font = Typeface.createFromAsset(context.getAssets(), "font.otf");
	}

	public static FontProvider getInstance() {
		if (instance == null)
			instance = new FontProvider();
		return instance;
	}

	public void applyOn(TextInputLayout textView) {
		if (textView != null)
			textView.setTypeface(font);
	}
	public void applyOn(TextView textView) {
		if (textView != null)
			textView.setTypeface(font);
	}


}
