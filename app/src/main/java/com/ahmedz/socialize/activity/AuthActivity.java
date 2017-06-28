package com.ahmedz.socialize.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.backend.Authenticator;
import com.ahmedz.socialize.backend.FireBaseDBHelper;
import com.ahmedz.socialize.model.FacebookUserModel;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;




public abstract class AuthActivity extends LoadingActivity implements FirebaseAuth.AuthStateListener {
	private static final String TAG = "AuthActivity";
	private Authenticator authenticator;
	GoogleApiClient mGoogleApiClient;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		authenticator = Authenticator.getInstance();
		buildGoogleApiClient();
	}

	void buildGoogleApiClient() {
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.server_client_id))
				.requestEmail()
				.build();
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, connectionResult -> Log.d(TAG, "onConnectionFailed: failed to connect to googleApiClient!"))
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();
	}

	String getUserEmail() {
		return authenticator.getUserEmail();
	}
	protected Uri getPhotoUrl() {
		return authenticator.getPhotoUri();
	}

	boolean sessionExists() {
		return authenticator.sessionExists();
	}

	void grantAccess() {
		authenticator.grantAccess(this);
	}

	Maybe<AuthResult> signIn(String email, String password) {
		return authenticator.signIn(email, password);
	}

	Observable<GoogleSignInAccount> signInWithGoogleAuth(Intent signInIntent) {
		return authenticator.signInWithGoogleAuth(this, signInIntent);
	}

	Maybe<AuthResult> signInWithCredential(AuthCredential credential) {
		return authenticator.signInWithCredential(credential);
	}

	Single<FacebookUserModel> getFacebookUser(AccessToken token) {
		return authenticator.getFacebookUser(token);
	}

	Maybe<AuthResult> createUser(String email, String password) {
		return authenticator.createUser(email, password);
	}

	Completable getUserCreationObservable(String imageUri, String username, String email) {
		Completable createUserObservable = FireBaseDBHelper.getInst().createUser(imageUri, username, email);
		return createUserObservable;
	}

	Completable resetPassword(String userEmail) {
		return authenticator.resetPassword(userEmail);
	}

	void signOut(){
		authenticator.signOut(mGoogleApiClient);
		goToSignIn();
	}

	void goToSignIn() {
		Intent intent = new Intent(this, SigninActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	@Override
	public void onStart() {
		super.onStart();
		authenticator.addAuthListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		authenticator.removeAuthListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (shouldShowAuthMenu())
		getMenuInflater().inflate(R.menu.auth_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	protected abstract boolean shouldShowAuthMenu();

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.action_signout:
				showLogoutDialog();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void showLogoutDialog() {
		showConfirmationDialog(
				positive -> {
					if (positive)
						signOut();
				},
				getString(R.string.logout_title),
				getString(R.string.logout_content),
				getString(R.string.dialog_yes), getString(R.string.dialog_cancel)
		);
	}

	@Override
	public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
		Log.i(TAG, "onAuthStateChanged: " + sessionExists());
	}
}
