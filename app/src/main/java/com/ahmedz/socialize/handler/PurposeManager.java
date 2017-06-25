package com.ahmedz.socialize.handler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.ahmedz.socialize.R;
import com.google.android.gms.appinvite.AppInviteInvitation;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import rx_activity_result2.Result;
import rx_activity_result2.RxActivityResult;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;



public class PurposeManager {
	private final RxPermissionManager rxPermissionManager;
	private final Activity activity;

	public PurposeManager(Activity activity) {
		rxPermissionManager = new RxPermissionManager(activity);
		this.activity = activity;
	}

	private Observable<Boolean> requestPermission(String... permissions) {
		return rxPermissionManager.requestPermissions(permissions)
				.observeOn(AndroidSchedulers.mainThread());
	}

	public Observable<Result<Activity>> getPhotoFromGallery() {
		return requestPermission(READ_EXTERNAL_STORAGE)
				.flatMap(granted -> {
					if (granted) {
						Intent chooser = getImgChooserIntent();
						return RxActivityResult.on(activity)
								.startIntent(chooser);
					} else {
						return Observable.error(new Exception("Permission wasn't granted!"));
					}
				});
	}

	private Intent getImgChooserIntent() {
		Intent intent;
		intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		return intent;
	}

	public Observable<Result<Activity>> sendAppInvitations(String groupUID) {
		Intent intent = getInvitationIntent(groupUID);
		return RxActivityResult.on(activity)
				.startIntent(intent);
	}

	private Intent getInvitationIntent(String groupUID) {
		return new AppInviteInvitation.IntentBuilder(activity.getString(R.string.invitation_title))
				.setMessage("Invitation")
				.setDeepLink(Uri.parse("http://Socialize.com/invite/"+groupUID))
				.setCallToActionText("Find Out Who!")
				.build();
	}
}
