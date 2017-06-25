package com.ahmedz.socialize.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.ahmedz.socialize.handler.FontProvider;

import java.util.List;



class SpinnerAdapter extends BaseAdapter {
	private final LayoutInflater mInflater;
	private final List<String> stringList;
	private final int layoutID;

	public SpinnerAdapter(Context context, int LayoutID, List<String> stringList) {
		mInflater = LayoutInflater.from(context);
		layoutID = LayoutID;
		this.stringList = stringList;
	}

	@Override
	public int getCount() {
		return stringList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CheckedTextView textView = (CheckedTextView) mInflater.inflate(layoutID, null);
		FontProvider.getInstance().applyOn(textView);
		textView.setText(stringList.get(position));
		return textView;
	}

}
