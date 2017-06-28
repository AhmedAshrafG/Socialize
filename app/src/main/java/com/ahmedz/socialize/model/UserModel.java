package com.ahmedz.socialize.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;



public class UserModel implements Parcelable {
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

	protected UserModel(Parcel in) {
		userUID = in.readString();
		nickName = in.readString();
		email = in.readString();
		token = in.readString();
		groupUID = in.readString();
		avatar = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(userUID);
		dest.writeString(nickName);
		dest.writeString(email);
		dest.writeString(token);
		dest.writeString(groupUID);
		dest.writeString(avatar);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
		@Override
		public UserModel createFromParcel(Parcel in) {
			return new UserModel(in);
		}

		@Override
		public UserModel[] newArray(int size) {
			return new UserModel[size];
		}
	};

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
