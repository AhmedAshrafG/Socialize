package com.ahmedz.socialize.activity;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ahmedz.socialize.handler.DialogHelper;
import com.ahmedz.socialize.handler.FontProvider;
import com.ahmedz.socialize.handler.PurposeManager;

import butterknife.ButterKnife;



public class BaseActivity extends AppCompatActivity {
	private DialogHelper dialogHelper;
	PurposeManager purposeManager;
	final FontProvider fontProvider = FontProvider.getInstance();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dialogHelper = new DialogHelper(this);
		purposeManager = new PurposeManager(this);
	}

	@Override
	public void setContentView(@LayoutRes int layoutResID) {
		super.setContentView(layoutResID);
		ButterKnife.bind(this);
	}

	public void showMessageDialog(String title, String content){
		dialogHelper.showMessageDialog(title, content);
	}

	void showConfirmationDialog(final DialogHelper.DialogCallback listener, String title, String content, String positive, String negative){
		dialogHelper.showConfirmationDialog(listener, title, content, positive, negative);
	}

	MaterialDialog.Builder showInputDialog(final DialogHelper.InputCallback listener, String title, String content){
		return dialogHelper.showInputDialog(listener, title, content);
	}

	public void showErrorDialog(String content) {
		dialogHelper.showErrorDialog(content);
	}
	public void showSimpleConfirmationDialog(DialogHelper.DialogCallback listener, String content) {
		dialogHelper.showSimpleConfirmationDialog(listener, content);
	}

	void showToast(String message) {
		runOnUiThread(() -> Toast.makeText(BaseActivity.this, message, Toast.LENGTH_SHORT).show());
	}
	void showToast(int stringID) {
		showToast(getString(stringID));
	}
}
