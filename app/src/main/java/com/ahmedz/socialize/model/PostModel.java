package com.ahmedz.socialize.model;

import android.os.Parcel;
import android.os.Parcelable;

import static com.ahmedz.socialize.utils.Util.getCurrentTime;


public class PostModel implements Parcelable {
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

	protected PostModel(Parcel in) {
		userUID = in.readString();
		title = in.readString();
		description = in.readString();
		link = in.readString();
		imageFile = in.readString();
		timeInMillis = in.readLong();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(userUID);
		dest.writeString(title);
		dest.writeString(description);
		dest.writeString(link);
		dest.writeString(imageFile);
		dest.writeLong(timeInMillis);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<PostModel> CREATOR = new Creator<PostModel>() {
		@Override
		public PostModel createFromParcel(Parcel in) {
			return new PostModel(in);
		}

		@Override
		public PostModel[] newArray(int size) {
			return new PostModel[size];
		}
	};

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
