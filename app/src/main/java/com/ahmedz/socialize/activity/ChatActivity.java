package com.ahmedz.socialize.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.adapter.ChatRecyclerAdapter;
import com.ahmedz.socialize.backend.FireBaseDBHelper;
import com.ahmedz.socialize.backend.FireBaseStorageHelper;
import com.ahmedz.socialize.callback.ChatItemListener;
import com.ahmedz.socialize.callback.ItemCountChangeListener;
import com.ahmedz.socialize.handler.GlobalState;
import com.ahmedz.socialize.model.ChatActivityInfo;
import com.ahmedz.socialize.model.ChatMessageModel;
import com.ahmedz.socialize.model.ImageMessageInfo;
import com.ahmedz.socialize.model.UserModel;
import com.ahmedz.socialize.view.CustomEmojiIconActions;
import com.google.firebase.database.Query;

import butterknife.Bind;
import butterknife.OnClick;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import io.reactivex.Single;
import rx_activity_result2.RxActivityResult;

import static com.ahmedz.socialize.model.ChatMessageModel.TYPE_IMAGE;
import static com.ahmedz.socialize.model.ChatMessageModel.TYPE_TEXT;
import static com.ahmedz.socialize.utils.Util.isValid;

public class ChatActivity extends AuthActivity implements ItemCountChangeListener, ChatItemListener {

	private final String TAG = this.getClass().getSimpleName();
	@Bind(R.id.toolbar)
	Toolbar toolbar;
	@Bind(R.id.recycler_chat)
	RecyclerView chatRecycler;
	@Bind(R.id.emoji_btn)
	ImageView emoticonButton;
	@Bind(R.id.message_input)
	EmojiconEditText messageInput;
	private String groupUID;
	private ChatRecyclerAdapter chatAdapter;
	private ChatActivityInfo chatActivityInfo;
	private UserModel userModel;
	private CustomEmojiIconActions emoticonIcon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setExtras();
		setupEmojiLayout();
		retrieveUserModel();
	}

	private void setupEmojiLayout() {
		emoticonIcon = new CustomEmojiIconActions(this, findViewById(R.id.main_view), messageInput, emoticonButton);
		emoticonIcon.ShowEmojIcon();
	}

	private void retrieveUserModel() {
		FireBaseDBHelper dbHelper = FireBaseDBHelper.getInst();
		dbHelper.getGroupUIDWithEmail(getUserEmail(), groupUID)
				.doOnSuccess(groupUID -> this.groupUID = groupUID)
				.flatMapSingle(groupUID -> dbHelper.getChatActivityInfo(groupUID))
				.doFinally(this::setLoaded)
				.subscribe(chatInfo -> {
					this.chatActivityInfo = chatInfo;
					this.userModel = chatInfo.getUserModel(getUserEmail());
					GlobalState.getInst().setChatActive(true);
					setupGroupChatData();
				}, throwable -> {
					throwable.printStackTrace();
					showToast(R.string.default_error_message);
				});
	}

	@OnClick(R.id.btn_send_message)
	public void sendTextMessage() {
		String messageText = messageInput.getText().toString();
		emoticonIcon.closeEmojIcon();
		messageInput.setText(null);

		if (!isValid(messageText))
			return;

		sendMessage(new ChatMessageModel(TYPE_TEXT, userModel.getUserUID(), messageText.trim()))
				.subscribe(messageUID ->
								Log.d(TAG, "sendMessage: successful!")
								, throwable -> {
									throwable.printStackTrace();
									showToast(R.string.message_send_fail);
								});
	}

	private Single<String> sendMessage(ChatMessageModel messageModel) {
		FireBaseDBHelper.getInst()
				.sendChatFCM(getUserEmail(), groupUID, messageModel.getMessage())
				.subscribe(() -> Log.d(TAG, "chatFCM: done"), Throwable::printStackTrace);

		return FireBaseDBHelper.getInst()
				.pushChatMessage(chatActivityInfo.getChatUID(), messageModel);
	}
	private void uploadPhoto() {
		purposeManager.getPhotoFromGallery()
				.filter(result -> result.resultCode() == RESULT_OK)
				.flatMap(result -> {
					String imageUri = result.data().getData().toString();
					Intent intent = new Intent(this, PictureCommentActivity.class);
					intent.putExtra(getString(R.string.nickname), userModel.getNickName());
					intent.putExtra(getString(R.string.avatar), userModel.getAvatar());
					intent.putExtra(getString(R.string.full_screen_image), imageUri);
					Log.d(TAG, "Photo onResult: " + imageUri);
					return RxActivityResult.on(this)
							.startIntent(intent);
				})
				.subscribe(result -> {
					Intent intent = result.data();
					String imageUri = intent.getStringExtra(getString(R.string.full_screen_image));
					String messageText = intent.getStringExtra(getString(R.string.message_text));
					result.targetUI().uploadSelectedImage(imageUri, messageText);

				}, throwable -> throwable.printStackTrace());
	}

	private void uploadSelectedImage(String imageUriString, String messageText) {
		sendMessage(new ChatMessageModel(TYPE_IMAGE, userModel.getUserUID(), messageText))
				.flatMapMaybe(messageUID ->
						FireBaseStorageHelper.getInst()
							.putGroupImage(groupUID, Uri.parse(imageUriString))
							.map(imageUri -> new ImageMessageInfo(messageUID, imageUri)))
				.flatMapCompletable(messageInfo -> FireBaseDBHelper.getInst().updateImageMessage(chatActivityInfo.getChatUID(), messageInfo))
				.subscribe(() -> {
					Log.d(TAG, "image uploaded successfully!");
				}, throwable -> {
					throwable.printStackTrace();
					showToast(R.string.default_error_message);
				});
	}

	private void setupGroupChatData() {
		Query chatQuery = FireBaseDBHelper.getInst()
				.getChatQuery(chatActivityInfo.getChatUID());

		LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
		mLinearLayoutManager.setStackFromEnd(true);

		chatAdapter = new ChatRecyclerAdapter(this, this, chatQuery, chatActivityInfo.getUserList(), userModel);
		chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				super.onItemRangeInserted(positionStart, itemCount);
				int friendlyMessageCount = chatAdapter.getItemCount();
				int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
				if (lastVisiblePosition == -1 ||
						(positionStart >= (friendlyMessageCount - 1) &&
								lastVisiblePosition == (positionStart - 1))) {
					chatRecycler.scrollToPosition(positionStart);
				}
			}
		});
		chatRecycler.setLayoutManager(mLinearLayoutManager);
		chatRecycler.setAdapter(chatAdapter);
	}

	@Override
	public void imageClicked(String imageUriString) {
		Intent intent = new Intent(this, FullScreenImageActivity.class);
		intent.putExtra(getString(R.string.nickname), userModel.getNickName());
		intent.putExtra(getString(R.string.avatar), userModel.getAvatar());
		intent.putExtra(getString(R.string.full_screen_image), imageUriString);
		startActivity(intent);
	}

	@Override
	public void itemCountChanged(int itemCount) {
		setLoaded();
	}

	private void setExtras() {
		Intent intent = getIntent();
		String groupUID_key = getString(R.string.groupUID);
		if (intent.hasExtra(groupUID_key))
			this.groupUID = intent.getStringExtra(groupUID_key);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.action_gallery:
				uploadPhoto();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		GlobalState.getInst().setChatActive(false);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (chatAdapter != null)
			chatAdapter.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GlobalState.getInst().setChatActive(false);
		if (chatAdapter != null)
			chatAdapter.cleanup();
	}
	@Override
	protected boolean shouldShowAuthMenu() {
		return true;
	}

	@Override
	protected boolean shouldLoadInitially() {
		return true;
	}

	@Override
	View getMainView() {
		return findViewById(R.id.main_view);
	}
}
