package com.ahmedz.socialize.model;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class GroupModel {
	private String groupUID;
	private String chatUID;
	private HashMap<String, String> users;

	private GroupModel() {}

	public GroupModel(String groupUID, String chatUID) {
		this.groupUID = groupUID;
		this.chatUID = chatUID;
	}

	public HashMap<String, String> getUsers() {
		return users;
	}
	public String getGroupUID() {
		return groupUID;
	}
	public String getChatUID() {
		return chatUID;
	}

	@Exclude
	public List<String> getUserUIDs() {
		return new ArrayList<>(users.values());
	}
}
