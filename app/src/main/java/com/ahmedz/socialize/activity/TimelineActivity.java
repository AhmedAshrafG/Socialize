package com.ahmedz.socialize.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.adapter.PostsRecyclerAdapter;
import com.ahmedz.socialize.backend.FireBaseDBHelper;
import com.ahmedz.socialize.callback.ItemCountChangeListener;
import com.ahmedz.socialize.callback.PostItemListener;
import com.ahmedz.socialize.model.UserModel;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.database.Query;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import rx_activity_result2.RxActivityResult;

public class TimelineActivity extends AuthActivity implements PostItemListener, ItemCountChangeListener {

	private final String TAG = this.getClass().getSimpleName();
	@Bind(R.id.toolbar)
	Toolbar toolbar;
	@Bind(R.id.empty_message)
	TextView emptyMessage;
	@Bind(R.id.posts_recycler)
	RecyclerView postsRecycler;
	private String groupUID;
	private PostsRecyclerAdapter postsAdapter;
	private List<UserModel> userModelList;
	private UserModel userModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		setSupportActionBar(toolbar);
		setExtras();
		setupTimeline();
	}

	@OnClick(R.id.fab)
	public void onAddPostFabClicked() {
		Intent intent = new Intent(this, PostActivity.class);
		intent.putExtra(getString(R.string.groupUID), userModel.getGroupUID());
		intent.putExtra(getString(R.string.userUID), userModel.getUserUID());
		intent.putExtra(getString(R.string.avatar), userModel.getAvatar());
		intent.putExtra(getString(R.string.nickname), userModel.getNickName());
		startActivity(intent);
	}

	private void setupTimeline() {
		FireBaseDBHelper dbHelper = FireBaseDBHelper.getInst();
		dbHelper.getGroupUsers(groupUID)
				.doFinally(this::setLoaded)
				.subscribe(userModelList -> {
					this.userModelList = userModelList;
					this.userModel = getCurrentUserModel(getUserEmail());
					populatePosts();
				}, throwable -> {
					throwable.printStackTrace();
					showToast(R.string.default_error_message);
				});
	}

	private UserModel getCurrentUserModel(String userEmail) {
		for (UserModel model : userModelList) {
			if (model.getEmail().equals(userEmail))
				return model;
		}
		return null;
	}

	private void populatePosts() {
		Query postsQuery = FireBaseDBHelper.getInst().getPostsQuery(groupUID);
		LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
		mLinearLayoutManager.setReverseLayout(true);
		mLinearLayoutManager.setStackFromEnd(true);
		postsAdapter = new PostsRecyclerAdapter(this, this, postsQuery, userModelList);
		postsRecycler.setLayoutManager(mLinearLayoutManager);
		postsRecycler.setAdapter(postsAdapter);
		postsRecycler.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
			@Override
			public void onChildViewAttachedToWindow(View view) {
				if (postsAdapter.getItemCount() != 0) {
					emptyMessage.setVisibility(View.INVISIBLE);
				} else {
					emptyMessage.setVisibility(View.VISIBLE);
				}
			}
			@Override
			public void onChildViewDetachedFromWindow(View view) {
			}
		});
	}

	private void setExtras() {
		Intent intent = getIntent();
		String groupUID_key = getString(R.string.groupUID);
		if (intent.hasExtra(groupUID_key)) {
			this.groupUID = intent.getStringExtra(groupUID_key);
		}
	}
	private void sendInvitation() {
		purposeManager.sendAppInvitations(groupUID)
				.subscribe(result -> {
					int resultCode = result.resultCode();
					if (resultCode == RESULT_OK) {
						Intent data = result.data();
						String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
						for (String id : ids)
							Log.d(TAG, "onActivityResult: sent invitation " + id);
					}
				}, throwable -> {
					throwable.printStackTrace();
					showToast(R.string.default_error_message);
				});
	}

	private void startProfileActivity() {
		Intent intent = new Intent(this, ProfileActivity.class);
		RxActivityResult.on(this)
				.startIntent(intent)
				.filter(result -> result.resultCode() == RESULT_OK)
				.flatMapSingle(result -> FireBaseDBHelper.getInst().getGroupUsers(groupUID))
				.subscribe(userList -> {
					this.userModelList = userList;
					postsAdapter.setUserList(userList);
					postsAdapter.notifyDataSetChanged();
				}, throwable -> throwable.printStackTrace());
	}

	private void startChatActivity() {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(getString(R.string.groupUID), groupUID);
		startActivity(intent);
	}

	@Override
	public void onActionClicked(String link) {
		if (!link.startsWith("http"))
			link = "http://" + link;
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivity(intent);
		} else {
			showToast(R.string.invalid_intent);
		}
	}

	@Override
	public void onImageClicked(String imageUriStr) {
		Intent intent = new Intent(this, FullScreenImageActivity.class);
		intent.putExtra(getString(R.string.nickname), userModel.getNickName());
		intent.putExtra(getString(R.string.avatar), userModel.getAvatar());
		intent.putExtra(getString(R.string.full_screen_image), imageUriStr);
		startActivity(intent);
	}

	@Override
	public void itemCountChanged(int itemCount) {
		if (itemCount > 0) {
			emptyMessage.setVisibility(View.GONE);
			postsRecycler.smoothScrollToPosition(itemCount);
		} else {
			emptyMessage.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_chat:
				startChatActivity();
				return true;
			case R.id.action_invite:
				sendInvitation();
				return true;
			case R.id.action_profile:
				startProfileActivity();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_timeline, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (postsAdapter != null)
			postsAdapter.stop();
	}

	@Override
	protected boolean shouldShowAuthMenu() {
		return true;
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
