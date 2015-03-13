package org.zhj.easychat.friend;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.FollowCallback;

import org.zhj.easychat.R;
import org.zhj.easychat.app.BaseActionBarActivity;
import org.zhj.easychat.database.DatabaseManager;
import org.zhj.easychat.database.Friend;

import java.util.List;

/**
 * @author Chaos
 *         2015/03/13.
 */
public class FriendRequestsActivity extends BaseActionBarActivity {

    private RecyclerView requestsView;
    private FriendsAdapter requestsAdapter;
    private TextView noneTipsText;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        requestsAdapter = new FriendsAdapter(this);
        requestsAdapter.setOnItemClickListener(new FriendsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final Friend friend) {
                AVUser.getCurrentUser().followInBackground(friend.getFriendPeerId(), new FollowCallback() {
                    @Override
                    public void done(AVObject avObject, AVException e) {
                        if (e == null) {
                            Toast.makeText(getApplicationContext(), "添加好友成功", Toast.LENGTH_SHORT).show();
                            DatabaseManager.getInstance().getFriendDao().insert(friend);
                            requestsAdapter.remove(friend);
                            if (requestsAdapter.size() > 0) {
                                noneTipsText.setVisibility(View.GONE);
                            }else {
                                noneTipsText.setVisibility(View.VISIBLE);
                            }
                        } else if (e.getCode() == AVException.DUPLICATE_VALUE) {
                            Toast.makeText(getApplicationContext(), "已是好友", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        requestsView = (RecyclerView) findViewById(R.id.requests);
        requestsView.setLayoutManager(new LinearLayoutManager(this));
        requestsView.setAdapter(requestsAdapter);
        noneTipsText = (TextView) findViewById(R.id.noneTips);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("好友请求");

        getFriendRequests();
    }

    private void getFriendRequests() {
        String currentUserId = AVUser.getCurrentUser().getObjectId();
        //关注
        AVQuery<AVUser> followeeQuery = AVUser.followeeQuery(currentUserId, AVUser.class);
        //粉丝
        AVQuery<AVUser> followerQuery = AVUser.followerQuery(currentUserId, AVUser.class);
        followerQuery.whereDoesNotMatchKeyInQuery("follower", "followee", followeeQuery);
        followerQuery.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> list, AVException e) {
                if (e == null && list != null && list.size() > 0) {
                    for (AVUser user : list) {
                        Friend friend = new Friend();
                        friend.setPeerId(AVUser.getCurrentUser().getObjectId());
                        friend.setFriendPeerId(user.getObjectId());
                        if (!requestsAdapter.contains(friend)) {
                            requestsAdapter.add(friend);
                        }
                    }
                    if (requestsAdapter.size() > 0) {
                        noneTipsText.setVisibility(View.GONE);
                    }
                } else {
                    noneTipsText.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
