package com.ahmedz.socialize.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.activity.MainActivity;
import com.ahmedz.socialize.backend.Authenticator;
import com.ahmedz.socialize.backend.FireBaseDBHelper;
import com.ahmedz.socialize.model.PostModel;
import com.ahmedz.socialize.model.UserModel;

import java.util.ArrayList;

/**
 * Created by ahmed on 28-Jun-17.
 */

public class TimelineWidgetProvider extends AppWidgetProvider {
	public static final String EXTRA_ITEM = "com.ahmedz.socialize.timeline_widget.EXTRA_ITEM";
	private static final String TOUCH_ACTION = "com.ahmedz.socialize.timeline_widget.TOUCH";

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if (intent.getAction().equals(TOUCH_ACTION))
			context.startActivity(new Intent(context, MainActivity.class));
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		Authenticator authenticator = Authenticator.getInstance();

		for (int appWidgetId : appWidgetIds) {
			Intent activityIntent = new Intent(context, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);

			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			rv.setPendingIntentTemplate(R.id.timeline_list, pendingIntent);

			if (authenticator.sessionExists()) {
				FireBaseDBHelper dbHelper = FireBaseDBHelper.getInst();
				dbHelper.getGroupUID(authenticator.getUserEmail())
						.flatMapSingle(groupUID -> dbHelper.getTimelineWidgetInfo(groupUID))
						.subscribe(timelineWidgetInfo -> {
							ArrayList<PostModel> postModels = timelineWidgetInfo.getPostModels();
							ArrayList<UserModel> userList = timelineWidgetInfo.getUserList();

							Intent intent = new Intent(context, WidgetService.class);
							Bundle bundle = new Bundle();
							bundle.putParcelableArrayList(context.getString(R.string.post_list_extra), postModels);
							bundle.putParcelableArrayList(context.getString(R.string.user_list_extra), userList);
							intent.putExtra(context.getString(R.string.bundle), bundle);

							rv.setRemoteAdapter(R.id.timeline_list, intent);
							rv.setEmptyView(R.id.timeline_container, R.id.empty_message);

							appWidgetManager.updateAppWidget(appWidgetId, rv);

						}, Throwable::printStackTrace);
			} else {
				rv.setViewVisibility(R.id.empty_message, View.VISIBLE);
				appWidgetManager.updateAppWidget(appWidgetId, rv);
			}
		}
	}
}
