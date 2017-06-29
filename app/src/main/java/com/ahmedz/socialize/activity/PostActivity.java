package com.ahmedz.socialize.activity;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.backend.FireBaseDBHelper;
import com.ahmedz.socialize.backend.FireBaseStorageHelper;
import com.ahmedz.socialize.model.PostModel;
import com.ahmedz.socialize.view.CircleTransform;
import com.ahmedz.socialize.view.PicassoCache;
import com.ahmedz.socialize.widget.TimelineWidgetProvider;

import butterknife.Bind;
import butterknife.OnClick;
import io.reactivex.Completable;

import static com.ahmedz.socialize.utils.Util.isValid;

public class PostActivity extends LoadingActivity {
	String TAG = this.getClass().getSimpleName();
	@Bind(R.id.toolbar)
	Toolbar toolbar;
	@Bind(R.id.user_avatar)
	ImageView userAvatar;
	@Bind(R.id.user_nickname)
	TextView userNickname;
	@Bind(R.id.title_text)
	EditText titleText;
	@Bind(R.id.description_text)
	EditText descriptionText;
	@Bind(R.id.upload_check)
	ImageView uploadCheck;
	@Bind(R.id.link_text)
	EditText linkText;
	@Bind(R.id.choose_photo) EditText uploadPhoto;
	@Bind(R.id.post_btn) Button postButton;
	private String groupUID;
	private String userUID;
	private String avatarUri;
	private String nickname;
	private Uri imageUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setExtras();
		setupViews();
	}

	private void setupViews() {
		PicassoCache.get()
				.load(Uri.parse(avatarUri))
				.error(R.drawable.ic_person)
				.transform(new CircleTransform())
				.into(userAvatar);
		userNickname.setText(nickname);
	}

	@OnClick(R.id.post_btn)
	public void onPostClicked() {
		String title = titleText.getText().toString().trim();
		String description = descriptionText.getText().toString().trim();
		String link = linkText.getText().toString().trim();

		if (title.isEmpty()) {
			titleText.setError(getString(R.string.input_title_error));
			return;
		}
		if (description.isEmpty()) {
			descriptionText.setError(getString(R.string.input_description_error));
			return;
		}

		Completable postCompletable;
		if (isValid(imageUri)) {
			postCompletable = FireBaseStorageHelper.getInst()
					.putGroupImage(groupUID, imageUri)
					.flatMapCompletable(imageUri -> {
						PostModel postModel = new PostModel(userUID, title, description, link, imageUri);
						return FireBaseDBHelper.getInst().pushPost(groupUID, postModel);
					});
		} else {
			PostModel postModel = new PostModel(userUID, title, description, link);
			postCompletable = FireBaseDBHelper.getInst().pushPost(groupUID, postModel);
		}
		postCompletable
				.doOnSubscribe(disposable -> setLoading())
				.doFinally(this::setLoaded)
				.subscribe(() -> {
					showToast(R.string.post_success_toast);
					sendUpdateBroadcast();
					finish();
				}, throwable -> {
					throwable.printStackTrace();
					showToast(R.string.post_fail_toast);
				});
	}

	private void sendUpdateBroadcast() {
		Intent intent = new Intent(this, TimelineWidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { R.xml.appwidget_info });
		sendBroadcast(intent);
	}

	@OnClick(R.id.choose_photo)
	public void uploadPhoto() {
		purposeManager.getPhotoFromGallery()
				.subscribe(result -> {
					Intent intent = result.data();
					int resultCode = result.resultCode();
					if (resultCode == RESULT_OK) {
						((PostActivity)result.targetUI()).useSelectedImage(intent.getData());
						Log.d(TAG, "Photo onResult: " + intent.getData());
					}
				}, throwable -> {
					throwable.printStackTrace();
					showToast(R.string.error_permission);
				});
	}

	private void useSelectedImage(Uri uri) {
		imageUri = uri;
		Log.d(TAG, "Photo onResult: " + imageUri);
		PicassoCache.get()
				.load(uri)
				.resizeDimen(R.dimen.image_size, R.dimen.zero_dimen)
				.into(uploadCheck);
		uploadCheck.setVisibility(View.VISIBLE);
	}

	private void setExtras() {
		Intent intent = getIntent();
		if (intent.hasExtra(getString(R.string.userUID))) {
			groupUID = intent.getStringExtra(getString(R.string.groupUID));
			userUID = intent.getStringExtra(getString(R.string.userUID));
			avatarUri = intent.getStringExtra(getString(R.string.avatar));
			nickname = intent.getStringExtra(getString(R.string.nickname));
		}
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
