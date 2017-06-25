package com.ahmedz.socialize.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahmedz.socialize.R;
import com.ahmedz.socialize.callback.ChatItemListener;
import com.ahmedz.socialize.model.ChatMessageModel;
import com.ahmedz.socialize.model.UserModel;
import com.ahmedz.socialize.view.CircleTransform;
import com.ahmedz.socialize.view.PicassoCache;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.ahmedz.socialize.model.ChatMessageModel.TYPE_IMAGE;
import static com.ahmedz.socialize.utils.Util.convertTimestamp;
import static java.util.concurrent.TimeUnit.SECONDS;


public class ChatRecyclerAdapter extends FirebaseRecyclerAdapter<ChatMessageModel,ChatRecyclerAdapter.MyChatViewHolder> {

	private final String TAG = this.getClass().getSimpleName();
	private static final int RIGHT_MSG = 0;
	private static final int LEFT_MSG = 1;
	private final Context mContext;
	private final ChatItemListener listener;
	private final List<UserModel> userList;
	private final UserModel userModel;
	private Disposable disposable;

	public ChatRecyclerAdapter(Context mContext, ChatItemListener listener, Query query, List<UserModel> userList, UserModel userModel) {
        super(ChatMessageModel.class, R.layout.item_message_left, ChatRecyclerAdapter.MyChatViewHolder.class, query);
		this.mContext = mContext;
		this.listener = listener;
		this.userList = userList;
		this.userModel = userModel;
		startTimeHandler();
    }

	private void startTimeHandler() {
		disposable = Observable.interval(60, SECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(time -> {
					notifyDataSetChanged();
				});
	}

	public void stop() {
		if (disposable != null && !disposable.isDisposed())
			disposable.dispose();
	}

	@Override
    public MyChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == RIGHT_MSG){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right,parent,false);
            return new MyChatViewHolder(view);
        } else {
	        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
	        return new MyChatViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessageModel model = getItem(position);
        if (model.getUserUID().equals(userModel.getUserUID())){
            return RIGHT_MSG;
        }else{
            return LEFT_MSG;
        }
    }

    @Override
    protected void populateViewHolder(MyChatViewHolder viewHolder, ChatMessageModel chatModel, int position) {
	    UserModel ownerModel = getOwnerModel(chatModel.getUserUID());
        viewHolder.setMessageText(chatModel.getMessage());
        viewHolder.setTimestamp(chatModel.getTimeInMillis());
	    viewHolder.setAvatar(ownerModel == null? null: ownerModel.getAvatar());
	    viewHolder.setImageFile(chatModel);
	    boolean shouldMerge = position - 1 >= 0 && getItem(position - 1).getUserUID().equals(chatModel.getUserUID());
	    viewHolder.setupAppearance(shouldMerge);
    }

	private UserModel getOwnerModel(String userUID) {
		for (UserModel user: userList)
			if (user.getUserUID().equals(userUID))
				return user;
		return null;
	}

	public class MyChatViewHolder extends RecyclerView.ViewHolder {
		@Bind(R.id.container) View container;
	    @Bind(R.id.timestamp) TextView tvTimestamp;
	    @Bind(R.id.chat_message_tv) EmojiconTextView txtMessage;
	    @Bind(R.id.user_avatar) ImageView userAvatar;
		@Bind(R.id.image_file) ImageView imageView;

        public MyChatViewHolder(View view) {
            super(view);
	        ButterKnife.bind(this, view);
        }
		@OnClick({R.id.chat_message_view, R.id.chat_message_tv})
		public void onMessageClicked() {
			if (tvTimestamp.getVisibility() == View.GONE)
				tvTimestamp.setVisibility(View.VISIBLE);
			else
				tvTimestamp.setVisibility(View.GONE);
		}

        public void setMessageText(String message){
            if (message == null || TextUtils.isEmpty(message))
	            txtMessage.setVisibility(View.GONE);
	        else
                txtMessage.setVisibility(View.VISIBLE);
	        txtMessage.setText(message);
        }

        public void setTimestamp(long timestamp){
            tvTimestamp.setText(convertTimestamp(timestamp));
        }

		public void setAvatar(String avatarUri) {
			if (avatarUri == null)
				avatarUri = "";
			Picasso.with(mContext)
					.load(Uri.parse(avatarUri))
					.placeholder(R.drawable.progress_placeholder)
					.error(R.drawable.ic_person)
					.transform(new CircleTransform())
					.into(userAvatar);
		}

		public void setImageFile(ChatMessageModel chatModel) {
			if (chatModel.getType().equals(TYPE_IMAGE)) {
				imageView.setVisibility(View.VISIBLE);
				String imageUri = chatModel.getImageFile();
				RequestCreator picassoRequest;
				if (TextUtils.isEmpty(imageUri)) {
					picassoRequest = PicassoCache.with()
							.load(R.drawable.progress_placeholder);
				} else {
					picassoRequest = PicassoCache.with()
							.load(Uri.parse(imageUri));
				}
				picassoRequest
						.resizeDimen(R.dimen.image_width, R.dimen.zero_dimen)
						.placeholder(R.drawable.progress_placeholder)
						.into(imageView); // TODO: 11/30/2016 delete image after the partner sees it.
			} else {
				imageView.setVisibility(View.GONE);
			}
			imageView.setOnClickListener(view -> {
				if (listener != null)
					listener.imageClicked(chatModel.getImageFile());
			});
		}

		public void setupAppearance(boolean shouldMerge) {
			if (shouldMerge) {
				userAvatar.setVisibility(View.INVISIBLE);
				setMargins(container, 0, 8, 0, 0);
			} else {
				userAvatar.setVisibility(View.VISIBLE);
				setMargins(container, 0, 50, 0, 0);
			}
		}
		public void setMargins (View v, int l, int t, int r, int b) {
			if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
				ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
				p.setMargins(l, t, r, b);
				v.requestLayout();
			}
		}
	}
}
