package com.ahmedz.socialize.model;

import com.google.firebase.database.Exclude;



public class UserModel {
	private String userUID;
	private String nickName;
	private String email;
	private String token;
	private String groupUID;
	private String avatar;
	@Exclude
	public static final int FEMALE = 1;
	@Exclude
	public static final int MALE = 0;

	private UserModel() {}

	public UserModel(String userUID, String groupUID, String avatar, String nickName, String email, String token) {
		this.userUID = userUID;
		this.nickName = nickName;
		this.email = email;
		this.avatar = avatar;
		this.token = token;
		this.groupUID = groupUID;
	}

	public String getUserUID() {
		return userUID;
	}

	public String getGroupUID() {
		return groupUID;
	}

	public String getNickName() {
		return nickName;
	}


	public String getEmail() {
		return email;
	}

	public String getToken() {
		return token;
	}

	public String getAvatar() {
		return avatar;
	}
}
