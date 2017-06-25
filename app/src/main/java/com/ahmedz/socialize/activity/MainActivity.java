package com.ahmedz.socialize.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.backend.FireBaseDBHelper;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.messaging.FirebaseMessaging;

import rx_activity_result2.RxActivityResult;

import static com.ahmedz.socialize.utils.Util.getLastSegmentOfURL;

public class MainActivity extends AuthActivity {

	private final String TAG = this.getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	void buildGoogleApiClient() {
		// Create an auto-managed GoogleApiClient with access to App Invites.
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(AppInvite.API)
				.enableAutoManage(this, connectionResult -> Log.d(TAG, "onConnectionFailed: failed to connect to googleApiClient!"))
				.addOnConnectionFailedListener(connectionResult -> Log.d(TAG, "initGoogleLogin: " + connectionResult.getErrorMessage()))
				.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
					@Override
					public void onConnected(@Nullable Bundle bundle) {
						Log.d(TAG, "onConnected: googleApiClient connected!");
					}
					@Override
					public void onConnectionSuspended(int i) {
						Log.d(TAG, "onConnectionSuspended: googleApiClient suspended!");
					}
				})
				.build();

		AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, false)
				.setResultCallback(result -> {
					Log.d(TAG, "getInvitation:onResult:" + result.getStatus());
					if (result.getStatus().isSuccess()) {
						Intent intent = result.getInvitationIntent();
						handleReferralLink(intent);
					} else {
						handleNormalUserEntry();
					}
				});
	}

	private void handleReferralLink(Intent intent) {
		String groupUID = getInvitationGroupUID(intent);
		if (sessionExists()) {
			joinGroup(groupUID);

		} else {
			Intent signInIntent = new Intent(this, SigninActivity.class);
			signInIntent.putExtra(getString(R.string.waiting_extra), true);
			RxActivityResult.on(this)
					.startIntent(signInIntent)
					.subscribe(result -> {
						int resultCode = result.resultCode();
						if (resultCode == RESULT_OK)
							joinGroup(groupUID);

					}, throwable -> {
						throwable.printStackTrace();
						showToast(R.string.default_error_message);
					});
		}
	}

	private void joinGroup(String groupUID) {
		// for group specific FCM.
		FirebaseMessaging.getInstance()
				.subscribeToTopic(groupUID);

		FireBaseDBHelper.getInst()
				.joinGroup(groupUID, getUserEmail())
				.subscribe(
						() -> startTimeLineActivity(groupUID),
						throwable -> {
							throwable.printStackTrace();
							showToast(R.string.default_error_message);
						});
	}

	private void handleNormalUserEntry() {
		if (sessionExists()) {
			FireBaseDBHelper.getInst()
					.getGroupUID(getUserEmail())
					.subscribe(
							this::startTimeLineActivity,
							throwable -> {
								throwable.printStackTrace();
								showToast(R.string.default_error_message);
							});
		} else {
			goToSignIn();
			finish();
		}
	}

	private String getInvitationGroupUID(Intent intent) {
		// Extract referral information from the intent.
		String invitationId = AppInviteReferral.getInvitationId(intent);
		String deepLink = AppInviteReferral.getDeepLink(intent);
		Log.d(TAG, "setupInvitationClient: " + deepLink + " " + invitationId);
		return getLastSegmentOfURL(deepLink);
	}

	private void startTimeLineActivity(String groupUID) {
		FirebaseMessaging.getInstance()
				.subscribeToTopic(groupUID);

		Intent intent = new Intent(this, TimelineActivity.class);
		intent.putExtra(getString(R.string.groupUID), groupUID);
		startActivity(intent);
		finish();
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
