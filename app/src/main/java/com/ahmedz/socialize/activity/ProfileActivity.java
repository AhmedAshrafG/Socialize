package com.ahmedz.socialize.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.backend.FireBaseDBHelper;
import com.ahmedz.socialize.backend.FireBaseStorageHelper;
import com.ahmedz.socialize.model.UserModel;
import com.ahmedz.socialize.view.PicassoCache;

import butterknife.Bind;
import butterknife.OnClick;
import io.reactivex.Completable;

import static com.ahmedz.socialize.utils.Util.escapeEmail;
import static com.ahmedz.socialize.utils.Util.isValid;

public class ProfileActivity extends AuthActivity {

	private final String TAG = this.getClass().getSimpleName();
	@Bind(R.id.toolbar)
	Toolbar toolbar;
	@Bind(R.id.username)
	EditText inputUsername;
	@Bind(R.id.uploaded_imageView)
	ImageView uploadedImage;
	private Uri imageUri;

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
				.error(R.drawable.ic_person)
				.into(uploadedImage);
	}

	@OnClick(R.id.choose_photo)
	public void choosePhoto() {
		purposeManager.getPhotoFromGallery()
				.subscribe(result -> {
					Intent intent = result.data();
					int resultCode = result.resultCode();
					if (resultCode == RESULT_OK) {
						imageUri = intent.getData();
						Log.d(TAG, "Photo onResult: " + imageUri);
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

		Completable updateCompletable;
		if (isValid(imageUri))
			updateCompletable = FireBaseStorageHelper.getInst()
					.putFile(getUserEmail(), imageUri)
					.flatMapCompletable(imageUri ->
							FireBaseDBHelper.getInst()
									.updateProfile(getUserEmail(), imageUri, username));
		else
			updateCompletable = FireBaseDBHelper.getInst()
					.updateProfile(getUserEmail(), username);

		updateCompletable
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
