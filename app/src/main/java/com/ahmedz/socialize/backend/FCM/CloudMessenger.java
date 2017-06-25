package com.ahmedz.socialize.backend.FCM;

import android.content.Context;
import android.util.Log;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.handler.GlobalState;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.facebook.login.widget.ProfilePictureView.TAG;

public class CloudMessenger {
	private static CloudMessenger instance;
	private final String BASE_URL = "https://fcm.googleapis.com/fcm/send";
	private final String AUTH_KEY = "AIzaSyDPf3voL3LIbaC4iXcd3p6YtapJ95oUm8c";
	public static final String TYPE_CHAT = "chat";

	public static synchronized CloudMessenger getInstance() {
		if (instance == null)
			instance = new CloudMessenger();
		return instance;
	}

	private String sendHTTPRequest(String senderEmail, String groupUID, String pushText, String type) throws Exception {
		// Prepare JSON containing the GCM message content. What to send and where to send.
		Context mContext = GlobalState.getInst().getAppContext();
		JSONObject jFcmData = new JSONObject();
		JSONObject jData = new JSONObject();
		jData.put(mContext.getString(R.string.message_FCM), pushText);
		jData.put(mContext.getString(R.string.type_FCM), type);
		jData.put(mContext.getString(R.string.sender_email), senderEmail);
		// Where to send GCM message.
		jFcmData.put("to", "/topics/"+groupUID);
		jFcmData.put("data",jData);

		// Create connection to send GCM Message request.
		URL url = new URL(BASE_URL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Authorization", "key=" + AUTH_KEY);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);

		// Send GCM message content.
		OutputStream outputStream = conn.getOutputStream();
		outputStream.write(jFcmData.toString().getBytes());

		// Read GCM response.
		InputStream inputStream = conn.getInputStream();
		String resp = IOUtils.toString(inputStream);
		System.out.println(resp);
		System.out.println("Check your device/emulator for notification or logcat for " +
				"confirmation of the receipt of the GCM message.");
		return resp;
	}

	public Completable sendFCMMessage(String userEmail, String groupUID, String pushText, String type) {
		return Completable.fromCallable(() -> {
			String response = sendHTTPRequest(userEmail, groupUID, pushText, type);
			Log.i(TAG, "sendFCMMessage: " + response);
			return response;
		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
	}
}
