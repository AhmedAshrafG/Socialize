package com.ahmedz.socialize.callback;


import android.widget.ImageView;

import com.ahmedz.socialize.model.PostModel;

public interface PostItemListener {
	void onActionClicked(String link);
	void onImageClicked(String imageUriString, ImageView imageView);
	void onPostClicked(PostModel postModel);
}
