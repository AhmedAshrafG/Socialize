package com.ahmedz.socialize.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.view.PicassoCache;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by ahmed on 26-Jun-17.
 */

public class PostDetailsActivity extends BaseActivity {
	@Bind(R.id.toolbar)
	Toolbar toolbar;
	@Bind(R.id.toolbar_bg)
	ImageView toolbarBg;
	@Bind(R.id.description_tv)
	TextView descriptionTv;
	private String imageUri;
	private String title;
	private String description;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_details);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setExtras();
		bindUI();
	}

	private void setExtras() {
		Intent intent = getIntent();
		imageUri = intent.getStringExtra(getString(R.string.imageUri_extra));
		title = intent.getStringExtra(getString(R.string.title_extra));
		description = intent.getStringExtra(getString(R.string.desc_extra));
	}

	private void bindUI() {
		PicassoCache.get()
				.load(Uri.parse(imageUri))
				.error(R.drawable.drawer_header_bg)
				.into(toolbarBg);
		getSupportActionBar().setTitle(title);
		descriptionTv.setText(description);
	}

	@OnClick(R.id.fab)
	void shareContent() {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, description);
		startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)));
	}
}
