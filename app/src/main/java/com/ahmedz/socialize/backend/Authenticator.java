package com.ahmedz.socialize.backend;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.activity.MainActivity;
import com.ahmedz.socialize.model.FacebookUserModel;
import com.ahmedz.socialize.model.UserModel;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.json.JSONObject;

import durdinapps.rxfirebase2.RxFirebaseAuth;
import durdinapps.rxfirebase2.RxFirebaseUser;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import rx_activity_result2.RxActivityResult;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.auth.FirebaseAuth.AuthStateListener;



public class Authenticator {
	private static final String TAG = "Authenticator";
	private static Authenticator instance;
	private final FirebaseAuth mAuth;

	public synchronized static Authenticator getInstance() {
		if (instance == null)
			instance = new Authenticator();
		return instance;
	}

	private Authenticator() {
		mAuth = FirebaseAuth.getInstance();
	}

	public void grantAccess(Activity activity) {
		Toast.makeText(activity, activity.getString(R.string.welcome), Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(activity, MainActivity.class);
		activity.startActivity(intent);
		activity.finish();
	}

	public Maybe<AuthResult> signIn(String email, String password) {
		return RxFirebaseAuth.signInWithEmailAndPassword(mAuth, email, password);
	}

	public boolean verifyAuth() {
		boolean sessionExists = mAuth.getCurrentUser() != null;
		return sessionExists;
	}

	public Maybe<AuthResult> createUser(String email, String password) {
		return RxFirebaseAuth.createUserWithEmailAndPassword(mAuth, email, password);
	}

	public Completable updateUserProfile(UserProfileChangeRequest profileUpdates) {
		return RxFirebaseUser.updateProfile(mAuth.getCurrentUser(), profileUpdates);
	}

	public void addAuthListener(AuthStateListener authListener) {
		mAuth.addAuthStateListener(authListener);
	}

	public void removeAuthListener(AuthStateListener authListener) {
		if (authListener != null) {
			mAuth.removeAuthStateListener(authListener);
		}
	}

	public void signOut(GoogleApiClient mGoogleApiClient) {
		if (mAuth != null)
			mAuth.signOut();
		LoginManager.getInstance().logOut();
		Auth.GoogleSignInApi
				.signOut(mGoogleApiClient)
				.setResultCallback(status -> Log.i(TAG, "signOut: " + status.toString()));
	}

	public String getUserEmail() {
		FirebaseUser user = mAuth.getCurrentUser();
		return user == null? "": user.getEmail();
	}

	public Uri getPhotoUri() {
		FirebaseUser user = mAuth.getCurrentUser();
		return user == null? null: user.getPhotoUrl();
	}

	public Maybe<AuthResult> signInWithCredential(AuthCredential credential) {
		return RxFirebaseAuth.signInWithCredential(mAuth, credential);
	}

	public Single<FacebookUserModel> getFacebookUser(AccessToken token) {
		return Single.create(subscriber -> {
			Bundle params = new Bundle();
			params.putString("fields", "picture.type(small),name,email,gender");
			new GraphRequest(token, "me", params, HttpMethod.GET, response -> {
				try {
					JSONObject data = response.getJSONObject();
					if (data.has("picture")) {
						String profilePicUrl = data.getJSONObject("picture").getJSONObject("data").getString("url");
						String username = data.getString("name");
						String email = data.getString("email");
						int gender = data.getString("gender").equals("male")? UserModel.MALE: UserModel.FEMALE;
						FacebookUserModel facebookUser = new FacebookUserModel(profilePicUrl, username, email, gender);
						subscriber.onSuccess(facebookUser);
					}
				} catch (Exception e) {
					subscriber.onError(e);
				}
			}).executeAsync();
		});
	}

	public Observable<GoogleSignInAccount> signInWithGoogleAuth(Activity activity, Intent signInIntent) {
		return RxActivityResult.on(activity)
				.startIntent(signInIntent)
				.filter(result -> result.resultCode() == RESULT_OK)
				.flatMap(activityResult -> {
					Intent data = activityResult.data();
					GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
					GoogleSignInAccount googleAccount = signInResult.getSignInAccount();
					AuthCredential credential = GoogleAuthProvider.getCredential(googleAccount.getIdToken(), null);
					return signInWithCredential(credential)
							.flatMapObservable(authResult -> Observable.just(googleAccount));
				});
	}

	public Completable resetPassword(String userEmail) {
		return RxFirebaseAuth.sendPasswordResetEmail(mAuth, userEmail);
	}
}
