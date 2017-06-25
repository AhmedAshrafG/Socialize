package com.ahmedz.socialize.model;


import com.google.firebase.database.Exclude;

import static com.ahmedz.socialize.utils.Util.getCurrentTime;


public class ChatMessageModel {
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_TEXT = "text";
	private String imageFile;
    private String userUID;
    private String message;
	private long timeInMillis;
	private String type;

    private ChatMessageModel() {
    }

    public ChatMessageModel(String type, String userUID, String message) {
	    this.type = type;
	    this.userUID = userUID;
	    this.message = message;
	    this.timeInMillis = getCurrentTime();
    }

	public ChatMessageModel(String type, String userUID, String message, String imageFile) {
		this(type, userUID, message);
		this.imageFile = imageFile;
	}

	public String getType() {
		return type;
	}

    public String getUserUID() {
        return userUID;
    }

    public void setString(String userModel) {
        this.userUID = userModel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

	public String getImageFile() {
		return imageFile;
	}

	@Exclude
    @Override
    public String toString() {
        return "ChatMessageModel(" +
                "timeInMillis='" + timeInMillis + '\'' +
                ", message='" + message + '\'' +
                ", userUID=" + userUID +
                ')';
    }
}
