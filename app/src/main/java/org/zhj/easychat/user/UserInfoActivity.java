package org.zhj.easychat.user;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

import org.zhj.easychat.R;
import org.zhj.easychat.app.BaseActionBarActivity;

/**
 * @author Chaos
 *         2015/03/12.
 */
public class UserInfoActivity extends BaseActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        Fragment userFragment = new UserFragment();
        Bundle data = new Bundle();
        data.putString("UserId", getIntent().getStringExtra("UserId"));
        userFragment.setArguments(data);
        getSupportFragmentManager().beginTransaction().add(R.id.container, userFragment).commit();

        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.user_info);
    }
}
