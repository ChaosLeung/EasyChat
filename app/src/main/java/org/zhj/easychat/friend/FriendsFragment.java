package org.zhj.easychat.friend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;

import org.zhj.easychat.R;
import org.zhj.easychat.chat.ChatActivity;
import org.zhj.easychat.database.DatabaseManager;
import org.zhj.easychat.database.Friend;
import org.zhj.easychat.database.FriendDao;
import org.zhj.easychat.database.User;
import org.zhj.easychat.database.UserDao;

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

        friendsAdapter = new FriendsAdapter(getActivity());
        friendsView.setAdapter(friendsAdapter);
        if (AVUser.getCurrentUser() != null) {
            QueryBuilder<Friend> qb = DatabaseManager.getInstance().getFriendDao().queryBuilder();
            qb.where(FriendDao.Properties.PeerId.eq(AVUser.getCurrentUser().getObjectId()));
            friendsAdapter.replaceWith(qb.list());
        }
        friendsAdapter.setOnItemClickListener(new FriendsAdapter.OnItemClickListener() {
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
    }

    @Override
    public void onResume() {
        super.onResume();
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
}
