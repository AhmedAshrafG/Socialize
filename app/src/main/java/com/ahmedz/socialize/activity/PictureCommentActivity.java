package com.ahmedz.socialize.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.view.CircleTransform;
import com.ahmedz.socialize.view.PicassoCache;

import butterknife.Bind;
import butterknife.OnClick;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class PictureCommentActivity extends LoadingActivity {
	@Bind(R.id.toolbar)
	Toolbar toolbar;
	@Bind(R.id.avatarIV)
	ImageView avatarIV;
	@Bind(R.id.usernameTV)
	TextView usernameTV;
	@Bind(R.id.imageView)
	ImageView imageView;
	@Bind(R.id.emoji_btn)
	ImageView emojiBtn;
	@Bind(R.id.message_input)
	EmojiconEditText messageInput;
	private String username;
	private String avatarUrl;
	private String imageUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_comment);

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		EmojIconActions emoticonIcon = new EmojIconActions(this, getMainView(), messageInput, emojiBtn);
		emoticonIcon.ShowEmojIcon();

		setValues();
	}

	@OnClick(R.id.btn_send_message)
	public void sendTextMessage() {
		String messageText = messageInput.getText().toString();
		Intent intent = new Intent();
		intent.putExtra(getString(R.string.full_screen_image), imageUrl);
		intent.putExtra(getString(R.string.message_text), messageText);
		setResult(RESULT_OK, intent);
		finish();
	}

	private void setValues() {
		username = getIntent().getStringExtra(getString(R.string.nickname));
		avatarUrl = getIntent().getStringExtra(getString(R.string.avatar));
		imageUrl = getIntent().getStringExtra(getString(R.string.full_screen_image));

		usernameTV.setText(username);

		PicassoCache.get()
				.load(Uri.parse(avatarUrl))
				.error(R.drawable.ic_person)
				.resizeDimen(R.dimen.user_avatar_size, R.dimen.user_avatar_size)
				.centerCrop()
				.transform(new CircleTransform()).into(avatarIV);

		PicassoCache.get()
				.load(Uri.parse(imageUrl))
				.into(imageView);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected boolean shouldLoadInitially() {
		return false;
	}

	@Override
	View getMainView() {
		return findViewById(R.id.main_view);
	}
}

