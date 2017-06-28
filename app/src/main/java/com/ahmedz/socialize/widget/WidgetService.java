package com.ahmedz.socialize.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.model.PostModel;
import com.ahmedz.socialize.model.UserModel;
import com.ahmedz.socialize.view.PicassoCache;

import java.io.IOException;
import java.util.ArrayList;

import static com.ahmedz.socialize.utils.Util.isValid;

/**
 * Created by ahmed on 28-Jun-17.
 */

public class WidgetService extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		Bundle bundle = intent
				.getBundleExtra(getString(R.string.bundle));
		ArrayList<PostModel> postModels = bundle.getParcelableArrayList(getString(R.string.post_list_extra));
		ArrayList<UserModel> userModels = bundle.getParcelableArrayList(getString(R.string.user_list_extra));
		return new StackRemoteViewsFactory(getApplicationContext(), postModels, userModels);
	}
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
	private Context context;
	private ArrayList<PostModel> postModels;
	private ArrayList<UserModel> userModels;

	public StackRemoteViewsFactory(Context context, ArrayList<PostModel> postModels, ArrayList<UserModel> userModels) {
		this.context = context;
		this.postModels = postModels;
		this.userModels = userModels;
	}

	@Override
	public void onCreate() {
	}
	@Override
	public void onDataSetChanged() {
	}
	@Override
	public void onDestroy() {
	}
	@Override
	public int getCount() {
		return postModels.size();
	}
	@Override
	public RemoteViews getViewAt(int position) {
		PostModel postModel = postModels.get(position);
		UserModel userModel = getUserModel(postModel.getUserUID());
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_item);
		rv.setTextViewText(R.id.post_title, postModel.getTitle());
		rv.setTextViewText(R.id.user_nickname, userModel.getNickName());
		String avatarUrl = userModel.getAvatar();
		if (isValid(avatarUrl))
			try {
				Bitmap bitmap = PicassoCache.get().load(Uri.parse(avatarUrl)).get();
				rv.setImageViewBitmap(R.id.user_avatar, bitmap);
			} catch (IOException e) {
				e.printStackTrace();
			}
		else
			rv.setImageViewResource(R.id.user_avatar, R.drawable.ic_person);

		rv.setOnClickFillInIntent(R.id.widget_item_container, new Intent());

		return rv;
	}

	private UserModel getUserModel(String userUID) {
		for (UserModel user: userModels)
			if (user.getUserUID().equals(userUID))
				return user;

		return null;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}
	@Override
	public int getViewTypeCount() {
		return 1;
	}
	@Override
	public long getItemId(int i) {
		return i;
	}
	@Override
	public boolean hasStableIds() {
		return false;
	}
}
