package com.ahmedz.socialize.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.view.CircleTransform;
import com.ahmedz.socialize.view.PicassoCache;
import com.ahmedz.socialize.view.TouchImageView;

import butterknife.Bind;

public class FullScreenImageActivity extends LoadingActivity {
	@Bind(R.id.toolbar)
	Toolbar toolbar;
	@Bind(R.id.avatarIV)
	ImageView avatarIV;
	@Bind(R.id.usernameTV)
	TextView usernameTV;
	@Bind(R.id.imageView)
	TouchImageView imageView;
	private String username;
	private String avatarUrl;
	private String imageUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_full_screen_image);
		supportPostponeEnterTransition();

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setValues();
	}

	private void setValues() {
		username = getIntent().getStringExtra(getString(R.string.nickname));
		avatarUrl = getIntent().getStringExtra(getString(R.string.avatar));
		imageUrl = getIntent().getStringExtra(getString(R.string.full_screen_image));

		usernameTV.setText(username);

		PicassoCache.get()
				.load(Uri.parse(avatarUrl))
				.error(R.drawable.ic_person)
				.resizeDimen(R.dimen.user_avatar_size, R.dimen.user_avatar_size)
				.centerCrop()
				.transform(new CircleTransform()).into(avatarIV);

		PicassoCache.get()
				.load(Uri.parse(imageUrl))
				.into(imageView);

		scheduleStartPostponedTransition(imageView);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected boolean shouldLoadInitially() {
		return false;
	}

	@Override
	View getMainView() {
		return findViewById(R.id.main_view);
	}
}

