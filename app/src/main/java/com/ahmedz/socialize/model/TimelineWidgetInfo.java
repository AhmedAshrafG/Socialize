package com.ahmedz.socialize.model;

import java.util.ArrayList;


public class TimelineWidgetInfo {

	private final ArrayList<UserModel> userList;
	private final ArrayList<PostModel> postModels;

	public TimelineWidgetInfo(ArrayList<UserModel> userList, ArrayList<PostModel> postModels) {
		this.userList = userList;
		this.postModels = postModels;
	}

	public ArrayList<PostModel> getPostModels() {
		return postModels;
	}

	public ArrayList<UserModel> getUserList() {
		return userList;
	}

	public UserModel getUserModel(String userEmail) {
		for (UserModel user: userList)
			if (user.getEmail().equals(userEmail))
				return user;

		return null;
	}
}
