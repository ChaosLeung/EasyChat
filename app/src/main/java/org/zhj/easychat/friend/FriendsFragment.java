package org.zhj.easychat.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.squareup.picasso.Picasso;

import org.zhj.easychat.ArrayRecyclerAdapter;
import org.zhj.easychat.R;
import org.zhj.easychat.chat.ChatActivity;
import org.zhj.easychat.database.DatabaseManager;
import org.zhj.easychat.database.Friend;
import org.zhj.easychat.database.FriendDao;
import org.zhj.easychat.database.User;
import org.zhj.easychat.database.UserDao;
import org.zhj.easychat.leancloud.LeanCloudUser;
import org.zhj.easychat.user.UserInfoActivity;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * @author Chaos
 *         2015/03/11.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendsView;
    private TextView noneTipsText;
    private FriendsAdapter friendsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        friendsView = (RecyclerView) rootView.findViewById(R.id.friends);
        friendsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        friendsView.setItemAnimator(new DefaultItemAnimator());

        noneTipsText = (TextView) rootView.findViewById(R.id.noneTips);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        friendsAdapter = new FriendsAdapter();
        friendsView.setAdapter(friendsAdapter);
        if (AVUser.getCurrentUser() != null) {
            QueryBuilder<Friend> qb = DatabaseManager.getInstance().getFriendDao().queryBuilder();
            qb.where(FriendDao.Properties.PeerId.eq(AVUser.getCurrentUser().getObjectId()));
            friendsAdapter.replaceWith(qb.list());
        }
        friendsAdapter.setOnItemClickListener(getActivity(), new FriendsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Friend friend) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("UserId", friend.getFriendPeerId());
                User user = DatabaseManager.getInstance().getUserDao().queryBuilder().where(UserDao.Properties.PeerId.eq(friend.getFriendPeerId())).unique();
                intent.putExtra("Nickname", user.getNickname());
                getActivity().startActivity(intent);
            }
        });
        if (friendsAdapter.size() > 0) {
            noneTipsText.setVisibility(View.GONE);
        }

        String currentUserId = AVUser.getCurrentUser().getObjectId();
        //关注
        AVQuery<AVUser> followeeQuery = AVUser.followeeQuery(currentUserId, AVUser.class);
        //粉丝
        AVQuery<AVUser> followerQuery = AVUser.followerQuery(currentUserId, AVUser.class);
        followerQuery.whereMatchesKeyInQuery("follower", "followee", followeeQuery);
        followerQuery.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> list, AVException e) {
                if (e == null && list != null && list.size() > 0) {
                    for (AVUser user : list) {
                        Friend friend = new Friend();
                        friend.setPeerId(AVUser.getCurrentUser().getObjectId());
                        friend.setFriendPeerId(user.getObjectId());
                        if (!friendsAdapter.contains(friend)) {
                            friendsAdapter.add(friend);
                        }
                    }
                    String sql = "DELETE FROM FRIEND WHERE PEER_ID='" + AVUser.getCurrentUser().getObjectId() + "'";
                    DatabaseManager.getInstance().getFriendDao().getDatabase().execSQL(sql);
                    if (friendsAdapter.size() > 0) {
                        noneTipsText.setVisibility(View.GONE);
                        DatabaseManager.getInstance().getFriendDao().insertInTx(friendsAdapter);
                    }
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_friends, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_friend:
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class FriendsAdapter extends ArrayRecyclerAdapter<Friend, FriendsAdapter.ViewHolder> {
        private OnItemClickListener onItemClickListener;
        private Context context;

        public void setOnItemClickListener(Context context, OnItemClickListener onItemClickListener) {
            this.context = context;
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
}
