package com.ahmedz.socialize.model;



public class ImageMessageInfo {
	private final String messageUID;
	private final String imageUri;

	public ImageMessageInfo(String messageUID, String imageUri) {

		this.messageUID = messageUID;
		this.imageUri = imageUri;
	}

	public String getMessageUID() {
		return messageUID;
	}

	public String getImageUri() {
		return imageUri;
	}
}
