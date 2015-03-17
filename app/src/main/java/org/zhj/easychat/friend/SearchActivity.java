package org.zhj.easychat.friend;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;

import org.zhj.easychat.R;
import org.zhj.easychat.app.BaseActionBarActivity;
import org.zhj.easychat.database.DatabaseManager;
import org.zhj.easychat.database.Friend;
import org.zhj.easychat.database.User;
import org.zhj.easychat.database.UserDao;
import org.zhj.easychat.leancloud.LeanCloudUser;
import org.zhj.easychat.user.UserInfoActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chaos
 *         2015/03/13.
 */
public class SearchActivity extends BaseActionBarActivity {

    private FriendsAdapter usersAdapter;
    private RecyclerView resultsView;

    private FindCallback<LeanCloudUser> callback = new FindCallback<LeanCloudUser>() {
        @Override
        public void done(List<LeanCloudUser> list, AVException e) {
            if (e == null) {
                if (list != null && list.size() > 0) {
                    for (AVUser user : list) {
                        Friend friend = new Friend();
                        friend.setPeerId(AVUser.getCurrentUser().getObjectId());
                        friend.setFriendPeerId(user.getObjectId());
                        usersAdapter.add(friend);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "用户不存在", Toast.LENGTH_SHORT).show();
                }
            } else {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "查询失败",Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        resultsView = (RecyclerView) findViewById(R.id.results);
        resultsView.setLayoutManager(new LinearLayoutManager(this));
        usersAdapter = new FriendsAdapter(this);
        usersAdapter.setOnItemClickListener(new FriendsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Friend friend) {
                Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                intent.putExtra("UserId", friend.getFriendPeerId());
                User user = DatabaseManager.getInstance().getUserDao().queryBuilder().where(UserDao.Properties.PeerId.eq(friend.getFriendPeerId())).unique();
                intent.putExtra("Nickname", user.getNickname());
                startActivity(intent);
            }
        });
        resultsView.setAdapter(usersAdapter);

        View friendLL = findViewById(R.id.friend_request);
        ImageView icon = (ImageView) friendLL.findViewById(R.id.avatar);
        icon.setImageResource(R.drawable.ic_add_box_black);
        TextView tip = (TextView) findViewById(R.id.nickname);
        tip.setText("好友请求");
        friendLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), FriendRequestsActivity.class));
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("搜索用户");

        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                usersAdapter.clear();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void initData(){
        AVQuery<LeanCloudUser> usernameQuery = LeanCloudUser.getUserQuery(LeanCloudUser.class);
        usernameQuery.findInBackground(callback);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            AVQuery<LeanCloudUser> usernameQuery = LeanCloudUser.getUserQuery(LeanCloudUser.class);
            usernameQuery.whereContains("username", query);
            AVQuery<LeanCloudUser> nicknameQuery = LeanCloudUser.getUserQuery(LeanCloudUser.class);
            nicknameQuery.whereContains("nickname", query);

            List<AVQuery<LeanCloudUser>> queries = new ArrayList<AVQuery<LeanCloudUser>>();
            queries.add(usernameQuery);
            queries.add(nicknameQuery);

            AVQuery<LeanCloudUser> mainQuery = AVQuery.or(queries);
            mainQuery.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
            mainQuery.findInBackground(callback);
        }
    }
}
