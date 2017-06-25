package com.ahmedz.socialize.model;


public class FacebookUserModel {
	private final String profilePicUrl;
	private final String username;
	private final String email;
	private final int gender;

	public FacebookUserModel(String profilePicUrl, String username, String email, int gender) {
		this.profilePicUrl = profilePicUrl;
		this.username = username;
		this.email = email;
		this.gender = gender;
	}

	public String getProfilePicUrl() {
		return profilePicUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public int getGender() {
		return gender;
	}
}
