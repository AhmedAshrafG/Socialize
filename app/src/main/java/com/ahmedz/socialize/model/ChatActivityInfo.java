package com.ahmedz.socialize.model;

import java.util.ArrayList;
import java.util.List;



public class ChatActivityInfo {

	private final List<UserModel> userList;
	private final String chatUID;

	public ChatActivityInfo(List<UserModel> userList, String chatUID) {
		this.userList = userList;
		this.chatUID = chatUID;
	}

	public String getChatUID() {
		return chatUID;
	}

	public List<UserModel> getUserList() {
		return userList;
	}

	public UserModel getUserModel(String userEmail) {
		for (UserModel user: userList)
			if (user.getEmail().equals(userEmail))
				return user;

		return null;
	}

	public List<String> getTokens(String senderEmail) {
		List<String> tokenList = new ArrayList<>();
		for (UserModel user: userList)
			if (user.getEmail() != senderEmail)
			tokenList.add(user.getToken());
		return tokenList;
	}

	public String getPartnerNickname(String userEmail) {
		for (UserModel user: userList)
			if (!user.getEmail().equals(userEmail))
				return user.getNickName();

		return "";
	}
}
