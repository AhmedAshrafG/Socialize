package com.ahmedz.socialize.backend.FCM;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.activity.ChatActivity;
import com.ahmedz.socialize.activity.MainActivity;
import com.ahmedz.socialize.backend.Authenticator;
import com.ahmedz.socialize.handler.AppPreference;
import com.ahmedz.socialize.handler.GlobalState;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import static com.ahmedz.socialize.backend.FCM.CloudMessenger.TYPE_CHAT;
import static com.ahmedz.socialize.utils.Util.isValid;


public class FCMService extends FirebaseMessagingService {

	private final String TAG = this.getClass().getSimpleName();
	private int counter = 0;

	/**
	 * Called when message is received.
	 *
	 * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
	 */
	// [START receive_message]
	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		// [START_EXCLUDE]
		// There are two types of messages data messages and notification messages. Data messages are handled
		// here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
		// traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
		// is in the foreground. When the app is in the background an automatically generated notification is displayed.
		// When the user taps on the notification they are returned to the app. Messages containing both notification
		// and data payloads are treated as notification messages. The Firebase console always sends notification
		// messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
		// [END_EXCLUDE]

		// Not getting messages here? See why this may be: https://goo.gl/39bRNJ
		Log.d(TAG, "From: " + remoteMessage.getFrom());

		Map<String, String> map = remoteMessage.getData();

		// Check if message contains a notification payload.
		String body = (remoteMessage.getNotification() == null) ? map.get(getString(R.string.message_FCM)) : remoteMessage.getNotification().getBody();

		if (remoteMessage.getNotification() != null) {
			Log.d(TAG, "Message Notification Body: " + body);
		}

		String senderEmail = map.get(getString(R.string.sender_email));
		String senderNickname = map.get(getString(R.string.sender_nickname));
		String currentEmail = Authenticator.getInstance().getUserEmail();
		String messageType = map.get(getString(R.string.type_FCM));
		// if I'm the sender or active, don't show notifications.
		if (!isValid(currentEmail) || (currentEmail != null && senderEmail.equals(currentEmail)) || GlobalState.getInst().isChatActive())
				return;

		Intent intent;
		switch(messageType) {
			case TYPE_CHAT:
				intent = new Intent(this, ChatActivity.class);
				break;
			default:
				intent = new Intent(this, MainActivity.class);
				break;
		}
		sendNotification(intent, body, senderNickname);
	}

	private Bitmap getLargeIcon() {
		Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon);
		return largeIcon;
	}

	private int getNotificationIcon() {
		int icon = R.drawable.app_icon;
		return icon;
	}

	private void sendNotification(Intent intent, String messageBody, String senderName) {
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		int uniqueKey = AppPreference.getInstance(getApplicationContext()).getUniqueKey();

		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
		inboxStyle.setSummaryText(senderName);
		inboxStyle.addLine(messageBody);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(getNotificationIcon())
				.setLargeIcon(getLargeIcon())
				.setContentTitle(getString(R.string.app_name))
				.setContentText(messageBody)
				.setAutoCancel(true)
				.setSound(defaultSoundUri)
				.setStyle(new NotificationCompat.MessagingStyle(messageBody))
				.setGroup(senderName)
				.setGroupSummary(true)
				.setContentIntent(pendingIntent);

		NotificationManagerCompat manager = NotificationManagerCompat.from(this);

		manager.notify(uniqueKey, notificationBuilder.build());
	}
}