package com.ahmedz.socialize.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.backend.FireBaseDBHelper;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.OnClick;
import io.reactivex.Completable;
import rx_activity_result2.RxActivityResult;

import static com.ahmedz.socialize.utils.Util.escapeEmail;

public class SigninActivity extends AuthActivity {

	private final String TAG = this.getClass().getSimpleName();
	@Bind(R.id.password)
	EditText inputPassword;
	@Bind(R.id.email)
	EditText inputEmail;
	@Bind(R.id.facebook_builtin_btn)
	LoginButton loginButton;
	@Bind(R.id.facebook_button) Button facebookButton;
	@Bind(R.id.google_button) Button gmailButton;
	@Bind(R.id.sign_in_button) Button signInButton;
	@Bind(R.id.sign_up_button) TextView signUpButton;
	@Bind(R.id.reset_password) TextView resetPassButton;

	private boolean waiting;
	private CallbackManager mCallbackManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);
		waiting = getIntent().hasExtra(getString(R.string.waiting_extra));
		if (sessionExists())
			grantAccess();
		initFacebookLogin();
	}

	@OnClick(R.id.reset_password)
	void resetPasswordClicked() {
		showInputDialog((dialog, input) ->
						resetPassword(input)
							.subscribe(
									() -> showToast(getString(R.string.reset_pass_success)),
									throwable -> {
										throwable.printStackTrace();
										showToast(getString(R.string.default_error_message));
									}
							),
				getString(R.string.reset_pass_title),
				getString(R.string.reset_pass_content)
		).build()
		.show();
	}

	@OnClick(R.id.facebook_button)
	void signInWithFacebookClicked() {
		loginButton.performClick();
	}

	private void initFacebookLogin() {
		mCallbackManager = CallbackManager.Factory.create();
		loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
		loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
				Log.d(TAG, "facebook:onSuccess:" + loginResult);
				signInWithFacebook(loginResult.getAccessToken());
			}
			@Override
			public void onCancel() {
				Log.d(TAG, "facebook:onCancel");
			}
			@Override
			public void onError(FacebookException error) {
				Log.d(TAG, "facebook:onError", error);
			}
		});
	}

	@OnClick(R.id.sign_in_button)
	public void signInClicked() {
		String email = inputEmail.getText().toString();
		String password = inputPassword.getText().toString();

		if (TextUtils.isEmpty(email)) {
			inputEmail.setError(getString(R.string.input_email_error));
			return;
		}
		if (TextUtils.isEmpty(password)) {
			inputPassword.setError(getString(R.string.input_pass_error));
			return;
		}

		signIn(email, password)
				.doOnSubscribe(disposable -> setLoading())
				.doFinally(this::setLoaded)
				.subscribe(authResult -> {
					if (waiting) {
						setResult(RESULT_OK);
						finish();
					} else
						grantAccess();
				}, throwable -> {
					Throwable exception = throwable;
					String message = exception.getMessage();
					if (exception instanceof FirebaseNetworkException)
						message = getString(R.string.internet_failure);
					showToast(message);
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mCallbackManager.onActivityResult(requestCode, resultCode, data);
	}

	@OnClick(R.id.sign_up_button)
	public void signUpClicked() {
		Intent intent = new Intent(this, SignupActivity.class);
		if (waiting) {
			intent.putExtra(getString(R.string.waiting_extra), true);
			RxActivityResult.on(this)
					.startIntent(intent)
					.doFinally(this::setLoaded)
					.subscribe(result -> {
						int resultCode = result.resultCode();
						if (resultCode == RESULT_OK) {
							setResult(RESULT_OK);
							finish();
						}
					}, throwable -> {
						throwable.printStackTrace();
						showToast(R.string.default_error_message);
					});
		} else {
			startActivity(intent);
			finish();
		}
	}

	@OnClick(R.id.google_button)
	void signInWithGoogle() {
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		signInWithGoogleAuth(signInIntent)
				.flatMapCompletable(googleAccount -> {
					String imageUri = googleAccount.getPhotoUrl().toString();
					String username = googleAccount.getDisplayName();
					String email = googleAccount.getEmail();
					return FireBaseDBHelper.getInst().getUserModel(escapeEmail(email))
							.flatMapCompletable(userModel -> {
								if (userModel == null)
									return getUserCreationObservable(imageUri, username, email);
								else
									return Completable.complete();
							});
				})
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
					signOut();
				});
	}

	private void signInWithFacebook(AccessToken token) {
		Log.d(TAG, "signInWithFacebook:" + token);
		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
		signInWithCredential(credential)
				.flatMapSingle(authResult -> getFacebookUser(token))
				.flatMapCompletable(facebookUser -> {
					String imageUri = facebookUser.getProfilePicUrl();
					String username = facebookUser.getUsername();
					String email = facebookUser.getEmail();
					return FireBaseDBHelper.getInst().getUserModel(escapeEmail(email))
							.flatMapCompletable(userModel ->
									userModel == null ?
											getUserCreationObservable(imageUri, username, email)
											: Completable.complete());
				})
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
					signOut();
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
