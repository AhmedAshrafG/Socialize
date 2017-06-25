package com.ahmedz.socialize.activity;

import android.support.annotation.LayoutRes;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ahmedz.socialize.R;



public abstract class LoadingActivity extends BaseActivity {
	private MaterialDialog mLoadingDialog;
	private View mainView;

	@Override
	public void setContentView(@LayoutRes int layoutResID) {
		super.setContentView(layoutResID);
		this.mainView = getMainView();
		if (shouldLoadInitially())
			setLoading();
	}

	protected abstract boolean shouldLoadInitially();
	abstract View getMainView();

	void setLoading() {
		if (mainView != null)
			mainView.setVisibility(View.INVISIBLE);
		if (mLoadingDialog != null && mLoadingDialog.isShowing())
			mLoadingDialog.dismiss();
		mLoadingDialog = new MaterialDialog.Builder(this)
					.title(R.string.loading_title)
					.content(R.string.loading_content)
					.progress(true, 0)
					.cancelable(false)
					.show();
	}

	void setLoaded() {
		if (mainView != null)
			mainView.setVisibility(View.VISIBLE);
		if(mLoadingDialog != null)
			mLoadingDialog.dismiss();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mLoadingDialog != null && mLoadingDialog.isShowing())
			mLoadingDialog.dismiss();
	}
}
