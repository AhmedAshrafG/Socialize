package com.ahmedz.socialize.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.backend.FireBaseDBHelper;
import com.ahmedz.socialize.backend.FireBaseStorageHelper;
import com.ahmedz.socialize.model.UserModel;
import com.ahmedz.socialize.view.PicassoCache;

import butterknife.Bind;
import butterknife.OnClick;

import static com.ahmedz.socialize.utils.Util.escapeEmail;

public class ProfileActivity extends AuthActivity {

	private final String TAG = this.getClass().getSimpleName();
	@Bind(R.id.toolbar)
	Toolbar toolbar;
	@Bind(R.id.username)
	EditText inputUsername;
	@Bind(R.id.upload_photo)
	View uploadImageView;
	@Bind(R.id.save_changes_button)
	Button saveButton;
	@Bind(R.id.uploaded_imageView)
	ImageView uploadedImage;
	private Uri imagePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		setSupportActionBar(toolbar);
		loadData();
	}

	private void loadData() {
		FireBaseDBHelper.getInst()
				.getUserModel(escapeEmail(getUserEmail()))
				.doFinally(this::setLoaded)
				.subscribe(
						this::bindViews,
						throwable -> {
							throwable.printStackTrace();
							showToast(R.string.default_error_message);
						});

	}
	private void bindViews(UserModel userModel) {
		inputUsername.setText(userModel.getNickName());
		PicassoCache.with()
				.load(Uri.parse(userModel.getAvatar()))
				.error(R.drawable.ic_user)
				.into(uploadedImage);
	}

	@OnClick(R.id.upload_photo)
	public void uploadPhoto() {
		purposeManager.getPhotoFromGallery()
				.subscribe(result -> {
					Intent intent = result.data();
					int resultCode = result.resultCode();
					if (resultCode == RESULT_OK) {
						imagePath = intent.getData();
						Log.d(TAG, "Photo onResult: " + imagePath);
						PicassoCache.with()
								.load(intent.getData())
								.into(uploadedImage);
					}
				}, throwable -> throwable.printStackTrace());
	}

	@OnClick(R.id.save_changes_button)
	public void saveChangesClicked() {
		String username = inputUsername.getText().toString().trim();
		if (TextUtils.isEmpty(username)) {
			inputUsername.setError(getString(R.string.input_empty_error));
			return;
		}
		FireBaseStorageHelper.getInst()
				.putFile(getUserEmail(), imagePath)
				.flatMapCompletable(imageUri ->
						FireBaseDBHelper.getInst().updateProfile(getUserEmail(), imageUri, username))
				.doOnSubscribe(disposable -> setLoading())
				.doFinally(this::setLoaded)
				.subscribe(() -> {
					setResult(RESULT_OK);
					finish();
				}, throwable -> {
					throwable.printStackTrace();
					showToast(R.string.default_error_message);
				});
	}

	@Override
	protected boolean shouldShowAuthMenu() {
		return false;
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
