package org.zhj.easychat.friend;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.squareup.picasso.Picasso;

import org.zhj.easychat.ArrayRecyclerAdapter;
import org.zhj.easychat.R;
import org.zhj.easychat.database.DatabaseManager;
import org.zhj.easychat.database.Friend;
import org.zhj.easychat.database.User;
import org.zhj.easychat.database.UserDao;
import org.zhj.easychat.leancloud.LeanCloudUser;
import org.zhj.easychat.user.UserInfoActivity;

import de.greenrobot.dao.query.QueryBuilder;

public class FriendsAdapter extends ArrayRecyclerAdapter<Friend, FriendsAdapter.ViewHolder> {
    private OnItemClickListener onItemClickListener;
    private Context context;

    public FriendsAdapter(Context context){
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Friend friend = get(position);

        final String otherPeerId = friend.getFriendPeerId();
        QueryBuilder<User> qb = DatabaseManager.getInstance().getUserDao().queryBuilder();
        qb.where(UserDao.Properties.PeerId.eq(otherPeerId));
        final User user = qb.unique();

        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserInfoActivity.class);
                intent.putExtra("UserId", otherPeerId);
                intent.putExtra("Nickname", user.getNickname());
                context.startActivity(intent);
            }
        });

        if (user != null) {
            setupUserInfo(holder, user);
        } else {
            AVQuery<LeanCloudUser> query = AVUser.getUserQuery(LeanCloudUser.class);
            query.whereEqualTo("objectId", otherPeerId);
            query.getFirstInBackground(new GetCallback<LeanCloudUser>() {
                @Override
                public void done(LeanCloudUser avUser, AVException e) {
                    if (avUser != null && e == null) {
                        User user = new User();
                        user.setPeerId(avUser.getObjectId());
                        user.setGender(avUser.getGender());
                        user.setArea(avUser.getArea());
                        user.setAvatarUrl(avUser.getAvatarUrl());
                        user.setIntroduction(avUser.getIntroduction());
                        user.setInterest(avUser.getInterest());
                        user.setNickname(avUser.getNickname());
                        DatabaseManager.getInstance().getUserDao().insert(user);
                        setupUserInfo(holder, user);
                    }
                }
            });
        }

        if (onItemClickListener != null) {
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(friend);
                }
            });
        }
    }

    private void setupUserInfo(ViewHolder viewHolder, User user) {
        if (!TextUtils.isEmpty(user.getNickname())) {
            viewHolder.nickname.setText(user.getNickname());
        } else {
            viewHolder.nickname.setText("未知");
        }
        Picasso.with(context)
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.default_avatar_blue)
                .error(R.drawable.default_avatar_blue)
                .resize(128, 128)
                .centerCrop()
                .into(viewHolder.avatar);
    }

    public interface OnItemClickListener {
        void onItemClick(Friend friend);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView avatar;
        private TextView nickname;
        private View container;

        public ViewHolder(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            nickname = (TextView) itemView.findViewById(R.id.nickname);
            container = itemView.findViewById(R.id.container);
        }
    }
}