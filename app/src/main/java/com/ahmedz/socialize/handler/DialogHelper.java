package com.ahmedz.socialize.handler;

import android.content.Context;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ahmedz.socialize.R;


public class DialogHelper {
	private final Context mContext;

	public DialogHelper(Context mContext){
		this.mContext = mContext;
	}

	public void showMessageDialog(String title, String content){

		new MaterialDialog.Builder(mContext)
				.title(title)
				.content(content)
				.positiveText(R.string.dialog_ok)
				.show();
	}

	public void showConfirmationDialog(final DialogCallback listener, String title, String content, String positive, String negative){
		new MaterialDialog.Builder(mContext)
				.title(title)
				.content(content)
				.positiveText(positive)
				.negativeText(negative)
				.onPositive((dialog, which) -> listener.onDialogMissionFulfilled(true))
				.onNegative((dialog, which) -> listener.onDialogMissionFulfilled(false))
				.show();
	}

	public MaterialDialog.Builder showInputDialog(final InputCallback listener, String title, String content){
		return new MaterialDialog.Builder(mContext)
				.title(title)
				.titleColor(mContext.getResources().getColor(R.color.colorPrimaryDark))
				.inputType(InputType.TYPE_CLASS_TEXT)
				.input(content, "", (dialog, input) -> listener.onInputReceived(dialog, input.toString()));
	}

	public void showErrorDialog(String content) {
		showMessageDialog(mContext.getString(R.string.dialog_error), content);
	}

	public void showSimpleConfirmationDialog(DialogCallback listener, String content) {
		showConfirmationDialog(listener, "Confirmation Message", content, "Confirm", "Cancel");
	}

	public interface DialogCallback{
		void onDialogMissionFulfilled(boolean positive);
	}
	public interface InputCallback{
		void onInputReceived(MaterialDialog dialog, String input);
	}
}
