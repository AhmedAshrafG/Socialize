package com.ahmedz.socialize.adapter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.callback.ItemCountChangeListener;
import com.ahmedz.socialize.callback.PostItemListener;
import com.ahmedz.socialize.model.PostModel;
import com.ahmedz.socialize.model.UserModel;
import com.ahmedz.socialize.view.CircleTransform;
import com.ahmedz.socialize.view.PicassoCache;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.ahmedz.socialize.utils.Util.convertTimestamp;
import static com.ahmedz.socialize.utils.Util.isValid;
import static java.util.concurrent.TimeUnit.SECONDS;


public class PostsRecyclerAdapter extends FirebaseRecyclerAdapter<PostModel,PostsRecyclerAdapter.PostViewHolder> {

	private final ItemCountChangeListener itemCountChangeListener;
	private final PostItemListener listener;
	private List<UserModel> userList;
	private Disposable subscription;
	private int lastCount = -1;

	public PostsRecyclerAdapter(ItemCountChangeListener itemCountChangeListener, PostItemListener listener, Query query, List<UserModel> userList) {
        super(PostModel.class, R.layout.post_item, PostViewHolder.class, query);
		this.itemCountChangeListener = itemCountChangeListener;
		this.listener = listener;
		this.userList = userList;
		startTimeHandler();
    }

	private void startTimeHandler() {
		subscription = Observable.interval(60, SECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(time -> notifyDataSetChanged());
	}

	public void stop() {
		if (subscription != null && !subscription.isDisposed())
			subscription.dispose();
	}

	@Override
	public int getItemCount() {
		int currentCount = super.getItemCount();
		if (currentCount != lastCount) {
			lastCount = currentCount;
			if (itemCountChangeListener != null)
				itemCountChangeListener.itemCountChanged(currentCount);
		}
		return currentCount;
	}

	@Override
    protected void populateViewHolder(PostViewHolder viewHolder, PostModel postModel, int position) {
	    UserModel ownerModel = getOwnerModel(postModel.getUserUID());
	    viewHolder.setNickName(ownerModel.getNickName());
        viewHolder.setTitle(postModel.getTitle());
        viewHolder.setDescription(postModel.getDescription());
        viewHolder.setTimestamp(postModel.getTimeInMillis());
	    viewHolder.setAvatar(ownerModel.getAvatar());
	    viewHolder.setImageFile(postModel.getImageFile(), listener);
	    viewHolder.setLink(postModel.getLink(), listener);
		viewHolder.setPostClickListener(listener, postModel);
    }

	private UserModel getOwnerModel(String userUID) {
		for (UserModel user: userList)
			if (user.getUserUID().equals(userUID))
				return user;
		return null;
	}

	public void setUserList(List<UserModel> userList) {
		this.userList = userList;
	}

	public static class PostViewHolder extends RecyclerView.ViewHolder {
	    @Bind(R.id.user_nickname) TextView tvNickName;
	    @Bind(R.id.timestamp) TextView tvTimestamp;
	    @Bind(R.id.post_title) TextView tvTitle;
	    @Bind(R.id.post_description) TextView tvDescription;
	    @Bind(R.id.user_avatar) ImageView userAvatar;
		@Bind(R.id.post_image) ImageView imageView;
		@Bind(R.id.link_text) TextView link_text;
		@Bind(R.id.post_container) View postContainer;

		public PostViewHolder(View view) {
            super(view);
	        ButterKnife.bind(this, view);
        }

		public void setAvatar(String avatarUri) {
			PicassoCache.get()
					.load(Uri.parse(avatarUri))
					.error(R.drawable.ic_person)
					.transform(new CircleTransform())
					.into(userAvatar);
		}

		public void setNickName(String nickName) {
			tvNickName.setText(nickName);
		}

		public void setTimestamp(long timestamp){
			tvTimestamp.setText(convertTimestamp(timestamp));
		}

		public void setTitle(String message){
			tvTitle.setText(message);
		}

		public void setDescription(String description) {
			tvDescription.setText(description);
		}

		public void setImageFile(String imageUri, PostItemListener listener) {
			if (!isValid(imageUri)) {
				imageView.setVisibility(View.GONE);
				return;
			}
			imageView.setVisibility(View.VISIBLE);
			PicassoCache.get()
					.load(Uri.parse(imageUri))
					.into(imageView);

			imageView.setOnClickListener(view -> {
				if (listener != null)
					listener.onImageClicked(imageUri);
			});
		}

		public void setLink(String link, PostItemListener listener) {
			if (!isValid(link))
				link_text.setVisibility(View.GONE);
			else {
				link_text.setVisibility(View.VISIBLE);
				link_text.setText(link);
				link_text.setOnClickListener(view -> {
					if (listener != null)
						listener.onActionClicked(link);
				});
			}
		}

		public void setPostClickListener(PostItemListener listener, PostModel postModel) {
			postContainer.setOnClickListener(view -> {
				if (listener != null)
					listener.onPostClicked(postModel);
			});
		}
	}
}
