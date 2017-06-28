package com.ahmedz.socialize.backend;

import com.ahmedz.socialize.backend.FCM.CloudMessenger;
import com.ahmedz.socialize.model.ChatActivityInfo;
import com.ahmedz.socialize.model.ChatMessageModel;
import com.ahmedz.socialize.model.GroupModel;
import com.ahmedz.socialize.model.ImageMessageInfo;
import com.ahmedz.socialize.model.PostModel;
import com.ahmedz.socialize.model.TimelineWidgetInfo;
import com.ahmedz.socialize.model.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;

import static com.ahmedz.socialize.backend.FCM.CloudMessenger.TYPE_CHAT;
import static com.ahmedz.socialize.model.ChatMessageModel.TYPE_TEXT;
import static com.ahmedz.socialize.utils.Util.escapeEmail;
import static com.ahmedz.socialize.utils.Util.isValid;



public class FireBaseDBHelper {

	private static FireBaseDBHelper instance;
	private final String USERS_NODE = "users";
	private final String POSTS_NODE = "posts";
	private final String GROUPS_NODE = "groups";
	private final String CHAT_NODE = "chat";
	private final String TOKEN_CHILD = "token";
	private final String GROUP_UID_CHILD = "groupUID";
	private final String CHAT_UID_CHILD = "chatUID";
	private final String TIME_CHILD = "timeInMillis";
	private final String NICKNAME_CHILD = "nickName";
	private final String AVATAR_CHILD = "avatar";
	private final DatabaseReference reference;

	public synchronized static FireBaseDBHelper getInst() {
		if (instance == null)
			instance = new FireBaseDBHelper();
		return instance;
	}

	private FireBaseDBHelper() {
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		database.setPersistenceEnabled(true);
		this.reference = database.getReference();
	}

	private Completable pushUser(String groupUID, String avatar, String nickname, String email, String token) {
		String userUID = escapeEmail(email);
		DatabaseReference usersRef = reference.child(USERS_NODE).child(userUID);
		UserModel userModel = new UserModel(userUID, groupUID, avatar, nickname, email, token);
		return RxFirebaseDatabase.setValue(usersRef, userModel);
	}

	public Completable updateToken(String email, String token) {
		DatabaseReference tokenRef = reference.child(USERS_NODE).child(escapeEmail(email));
		Map<String, Object> map = new HashMap<>();
		map.put(TOKEN_CHILD, token);
		return RxFirebaseDatabase.updateChildren(tokenRef, map);
	}

	public Maybe<String> getGroupUID(String userEmail) {
		DatabaseReference groupUIDRef = reference.child(USERS_NODE)
				.child(escapeEmail(userEmail))
				.child(GROUP_UID_CHILD);
		return RxFirebaseDatabase.observeSingleValueEvent(groupUIDRef, String.class);
	}

	public Maybe<String> getRegToken(String email) {
		DatabaseReference tokenRef = reference.child(USERS_NODE)
				.child(escapeEmail(email))
				.child(TOKEN_CHILD);
		return RxFirebaseDatabase.observeSingleValueEvent(tokenRef, String.class);
	}

	public Completable sendChatFCM(String userEmail, String groupUID, String messageText) {
		return CloudMessenger.getInstance().sendFCMMessage(userEmail, groupUID, messageText, TYPE_CHAT);
	}

	private Completable updateUserGroupUID(String email, String groupUID) {
		DatabaseReference userRef = reference.child(USERS_NODE).child(escapeEmail(email));
		Map<String, Object> map = new HashMap<>();
		map.put(GROUP_UID_CHILD, groupUID);
		return RxFirebaseDatabase.updateChildren(userRef, map);
	}

	private Maybe<GroupModel> getGroupModel(String groupUID) {
		DatabaseReference groupRef = reference.child(GROUPS_NODE).child(groupUID);
		return RxFirebaseDatabase.observeSingleValueEvent(groupRef, GroupModel.class);
	}

	public Query getChatQuery(String chatUID) {
		return reference.child(CHAT_NODE).child(chatUID).orderByChild(TIME_CHILD);
	}

	public Maybe<UserModel> getUserModel(String userUID) {
		DatabaseReference userRef = reference.child(USERS_NODE).child(userUID);
		return RxFirebaseDatabase.observeSingleValueEvent(userRef, UserModel.class);
	}

	public Single<String> pushChatMessage(String chatUID, ChatMessageModel chatMessageModel) {
		DatabaseReference messageRef = reference.child(CHAT_NODE).child(chatUID).push();
		return RxFirebaseDatabase.setValue(messageRef, chatMessageModel)
				.andThen(Single.just(messageRef.getKey()));
	}

	public Single<List<UserModel>> getGroupUsers(String groupUID) {
		Single<List<UserModel>> userListObservable =
				getGroupModel(groupUID)
						.map(groupModel -> groupModel.getUserUIDs())
						.flatMapObservable(userUIDs -> Observable.fromIterable(userUIDs))
						.flatMapMaybe(userUID -> getUserModel(userUID))
						.toList()
						.subscribeOn(AndroidSchedulers.mainThread())
						.observeOn(AndroidSchedulers.mainThread());
		return userListObservable;
	}

	public Maybe<String> getGroupUIDWithEmail(String userEmail) {
		return getUserModel(escapeEmail(userEmail))
				.map(userModel -> userModel.getGroupUID());
	}

	public Maybe<String> getGroupUIDWithEmail(String userEmail, String groupUID) {
		if (isValid(groupUID))
			return Maybe.just(groupUID);

		return getUserModel(escapeEmail(userEmail))
				.map(userModel -> userModel.getGroupUID());
	}

	public Single<ChatActivityInfo> getChatActivityInfo(String groupUID) {
		return Single.zip(
				getGroupUsers(groupUID),
				getChatUID(groupUID).toSingle(),
				(userModels, chatUID) -> new ChatActivityInfo(userModels, chatUID)
		);
	}

	private Maybe<String> getChatUID(String groupUID) {
		DatabaseReference chatChildRef = reference.child(GROUPS_NODE)
				.child(groupUID)
				.child(CHAT_UID_CHILD);
		return RxFirebaseDatabase.observeSingleValueEvent(chatChildRef, String.class);
	}

	public Query getPostsQuery(String groupUID) {
		return reference.child(POSTS_NODE)
				.child(groupUID)
				.orderByChild(TIME_CHILD);
	}

	public Completable pushPost(String groupUID, PostModel postModel) {
		DatabaseReference postsRef = reference.child(POSTS_NODE).child(groupUID);
		return RxFirebaseDatabase.setValue(postsRef.push(), postModel);
	}

	public Completable joinGroup(String groupUID, String userEmail) {
		String userUID = escapeEmail(userEmail);
		DatabaseReference groupUserRef = reference
				.child(GROUPS_NODE)
				.child(groupUID)
				.child(USERS_NODE).push();
		Completable userGroupUIDUpdate = updateUserGroupUID(userEmail, groupUID);
		Completable groupUserUIDUpdate = RxFirebaseDatabase.setValue(groupUserRef, userUID);
		return Completable.mergeArray(userGroupUIDUpdate, groupUserUIDUpdate);
	}

	public Completable createUser(String username, String userEmail) {
		return createUser("", username, userEmail);
	}

	public Completable createUser(String imageUri, String username, String userEmail) {
		String userUID = escapeEmail(userEmail);
		DatabaseReference groupRef = reference.child(GROUPS_NODE).push();
		DatabaseReference chatRef = reference.child(CHAT_NODE).push();
		String groupUID = groupRef.getKey();
		String chatUID = chatRef.getKey();

		FirebaseMessaging.getInstance().subscribeToTopic(groupUID);
		pushChatMessage(chatUID, new ChatMessageModel(TYPE_TEXT, userUID, "Hello there!"));

		Completable userUIDObservable = pushUser(groupUID, imageUri, username, userEmail, FirebaseInstanceId.getInstance().getToken());
		Completable createGroupObservable = RxFirebaseDatabase.setValue(groupRef, new GroupModel(groupUID, chatUID))
				.andThen(pushUser(groupRef, userUID));

		return Completable.mergeArray(userUIDObservable, createGroupObservable);
	}

	private Completable pushUser(DatabaseReference groupRef, String userUID) {
		DatabaseReference userRef = groupRef.child(USERS_NODE).push();
		return RxFirebaseDatabase.setValue(userRef, userUID);
	}

	public Completable updateImageMessage(String chatUID, ImageMessageInfo messageInfo) {
		DatabaseReference messageRef = reference.child(CHAT_NODE)
				.child(chatUID)
				.child(messageInfo.getMessageUID());
		Map<String, Object> map = new HashMap<>();
		map.put("imageFile", messageInfo.getImageUri());
		return RxFirebaseDatabase.updateChildren(messageRef, map);
	}

	public Completable updateProfile(String userEmail, String avatar, String nickname) {
		String userUID = escapeEmail(userEmail);
		DatabaseReference userRef = reference.child(USERS_NODE).child(userUID);

		HashMap<String, Object> map = new HashMap<>();
		map.put(AVATAR_CHILD, avatar);
		map.put(NICKNAME_CHILD, nickname);

		return RxFirebaseDatabase.updateChildren(userRef, map);
	}

	public Completable updateProfile(String userEmail, String nickname) {
		String userUID = escapeEmail(userEmail);
		DatabaseReference userRef = reference.child(USERS_NODE).child(userUID);

		HashMap<String, Object> map = new HashMap<>();
		map.put(NICKNAME_CHILD, nickname);

		return RxFirebaseDatabase.updateChildren(userRef, map);
	}

	public Single<List<PostModel>> getLatestPostsQuery(String groupUID) {
		Query latestPostsQuery = getPostsQuery(groupUID)
				.limitToFirst(10);
		return RxFirebaseDatabase.observeSingleValueEvent(latestPostsQuery, (Function<DataSnapshot, List<PostModel>>) dataSnapshot -> {
			ArrayList<PostModel> postList = new ArrayList<>();
			for (DataSnapshot child : dataSnapshot.getChildren()) {
				PostModel postModel = child.getValue(PostModel.class);
				postList.add(postModel);
			}
			return postList;
		}).toSingle(new ArrayList<>());
	}

	public Single<TimelineWidgetInfo> getTimelineWidgetInfo(String groupUID) {
		return Single.zip(
				getGroupUsers(groupUID),
				getLatestPostsQuery(groupUID),
				(userModels, postModels) -> new TimelineWidgetInfo((ArrayList) userModels, (ArrayList) postModels)
		);
	}
}