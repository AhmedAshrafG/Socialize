package com.ahmedz.socialize.callback;


import com.ahmedz.socialize.model.PostModel;

public interface PostItemListener {
	void onActionClicked(String link);
	void onImageClicked(String imageUriString);
	void onPostClicked(PostModel postModel);
}
