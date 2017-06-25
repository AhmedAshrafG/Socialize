package com.ahmedz.socialize.backend.FCM;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ahmedz.socialize.backend.Authenticator;
import com.ahmedz.socialize.backend.FireBaseDBHelper;



public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

	private final String TAG = this.getClass().getSimpleName();

	/**
	 * Called if InstanceID token is updated. This may occur if the security of
	 * the previous token had been compromised. Note that this is called when the InstanceID token
	 * is initially generated so this is where you would retrieve the token.
	 */
	// [START refresh_token]
	@Override
	public void onTokenRefresh() {
		// Get updated InstanceID token.
		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		Log.d(TAG, "Refreshed token: " + refreshedToken);

		sendRegistrationToServer(refreshedToken);
		// Once a token is generated, we subscribe to topic.
		FirebaseMessaging.getInstance()
				.subscribeToTopic("socialize_all");
	}
	// [END refresh_token]

	/**
	 * Persist token to third-party servers.
	 *
	 * Modify this method to associate the user's FCM InstanceID token with any server-side account
	 * maintained by your application.
	 *
	 * @param token The new token.
	 */
	private void sendRegistrationToServer(String token) {
		String userEmail = Authenticator.getInstance().getUserEmail();
		if (!userEmail.isEmpty()) {
			Log.d(TAG, "sendRegistrationToServer: found email!");
			FireBaseDBHelper.getInst()
					.updateToken(userEmail, token)
					.subscribe(
							() -> Log.d(TAG, "call: registration token saved!"),
							throwable -> throwable.printStackTrace()
					);
		} else {
			Log.d(TAG, "sendRegistrationToServer: email is empty");
		}
	}
}