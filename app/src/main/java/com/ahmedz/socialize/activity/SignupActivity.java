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
import android.widget.TextView;

import com.ahmedz.socialize.BuildConfig;
import com.ahmedz.socialize.R;
import com.ahmedz.socialize.backend.FireBaseDBHelper;
import com.ahmedz.socialize.backend.FireBaseStorageHelper;
import com.ahmedz.socialize.view.PicassoCache;

import butterknife.Bind;
import butterknife.OnClick;

public class SignupActivity extends AuthActivity {

	private final String TAG = this.getClass().getSimpleName();
	@Bind(R.id.toolbar)
	Toolbar toolbar;
	@Bind(R.id.email)
	EditText inputEmail;
	@Bind(R.id.username)
	EditText inputUsername;
	@Bind(R.id.password)
	EditText inputPassword;
	@Bind(R.id.repeat_password)
	EditText inputRepeatPassword;
	@Bind(R.id.uploaded_imageView)
	ImageView uploadedImage;
	@Bind(R.id.sign_up_button) Button signUpButton;
	@Bind(R.id.sign_in_button) TextView signInButton;
	private Uri imagePath;
	private boolean waiting;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		setSupportActionBar(toolbar);
		waiting = getIntent().hasExtra(getString(R.string.waiting_extra));

		if (BuildConfig.DEBUG) {
			inputUsername.setText("ahmed");
			inputEmail.setText("ahmed.ashraf33@yahoo.com");
			inputPassword.setText("ahmedz333");
			inputRepeatPassword.setText("ahmedz333");
		}
	}

	@OnClick(R.id.sign_in_button)
	public void signInClicked() {
		startActivity(new Intent(this, SigninActivity.class));
		finish();
	}

	@OnClick(R.id.choose_photo)
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
				}, Throwable::printStackTrace);
	}

	@OnClick(R.id.sign_up_button)
	public void signUpClicked() {
		String username = inputUsername.getText().toString().trim();
		String email = inputEmail.getText().toString().trim();
		String password = inputPassword.getText().toString().trim();
		String repeatedPassword = inputRepeatPassword.getText().toString().trim();

		if (TextUtils.isEmpty(username)) {
			inputUsername.setError(getString(R.string.input_empty_error));
			return;
		}
		if (TextUtils.isEmpty(email)) {
			inputEmail.setError(getString(R.string.input_empty_error));
			return;
		}
		if (TextUtils.isEmpty(password)) {
			inputPassword.setError(getString(R.string.input_empty_error));
			return;
		}
		if (TextUtils.isEmpty(repeatedPassword)) {
			inputRepeatPassword.setError(getString(R.string.input_empty_error));
			return;
		}
		if (password.length() < 6) {
			inputPassword.setError(getString(R.string.input_short_pass_error));
			return;
		}
		if (!password.equals(repeatedPassword)) {
			inputRepeatPassword.setError(getString(R.string.input_pass_match_error));
			return;
		}

		createUser(email, password)
				.doOnComplete(() -> Log.i(TAG, "signUpClicked: user authenticated successfully!"))
				.flatMapCompletable(authResult -> {
					if (imagePath != null) {
						Log.i(TAG, "signUpClicked: uploading image!");
						return FireBaseStorageHelper.getInst()
								.putFile(email, imagePath)
								.doOnComplete(() -> Log.i(TAG, "signUpClicked: image uploaded!"))
								.flatMapCompletable(imageUri ->
										FireBaseDBHelper.getInst().createUser(imageUri, username, email));
					} else {
						return FireBaseDBHelper.getInst().createUser(username, email);
					}
				})
				.doOnComplete(() -> Log.i(TAG, "signUpClicked: user signed up successfully!"))
				.doOnSubscribe(disposable -> setLoading())
				.doFinally(this::setLoaded)
				.subscribe(() -> {
					if (waiting) {
						setResult(RESULT_OK);
						finish();
					} else {
						grantAccess();
					}
				}, throwable -> {
					showToast(R.string.default_error_message);
					throwable.printStackTrace();
				});
	}

	@Override
	protected boolean shouldShowAuthMenu() {
		return false;
	}

	@Override
	protected boolean shouldLoadInitially() {
		return false;
	}

	@Override
	View getMainView() {
		return null;
	}
}
