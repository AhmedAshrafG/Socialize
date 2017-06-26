package com.ahmedz.socialize.model;

import static com.ahmedz.socialize.utils.Util.getCurrentTime;


public class PostModel {
	private String userUID;
	private String title;
	private String description;
	private String link;
	private String imageFile;
	private long timeInMillis;

	private PostModel() {}

	public PostModel(String userUID, String title, String description, String link, String imageFile) {
		this.userUID = userUID;
		this.title = title;
		this.description = description;
		this.link = link;
		this.imageFile = imageFile;
		this.timeInMillis = getCurrentTime();
	}

	public PostModel(String userUID, String title, String description, String link) {
		this(userUID, title, description, link, "");
	}

	public String getUserUID() {
		return userUID;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getLink() {
		return link;
	}

	public String getImageFile() {
		return imageFile;
	}

	public long getTimeInMillis() {
		return timeInMillis;
	}
}
