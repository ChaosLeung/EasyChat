package org.zhj.easychat;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.avos.avoscloud.AVUser;

import org.zhj.easychat.friend.FriendsFragment;
import org.zhj.easychat.posts.PostsFragment;
import org.zhj.easychat.session.SessionsFragment;
import org.zhj.easychat.user.UserFragment;


public class MainActivity extends ActionBarActivity {

    /**
     * 双击 Back 键退出计时器
     */
    private long exitTime = 0;

    private static final String[] TITLES = new String[]{"话题", "会话", "好友", "个人"};

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!hasUser()) {
            startLoginActivity();
            finish();
            return;
        } else {
            setupViewPager();
            setupActionBar();
        }
    }

    private boolean hasUser() {
        return AVUser.getCurrentUser() != null;
    }

    private void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void setupViewPager() {
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setSelectedNavigationItem(position);
            }
        });
    }

    private void setupActionBar() {
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };
        for (String title : TITLES) {
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(title).setTabListener(tabListener));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private Fragment[] fragments;

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new Fragment[]{
                    new PostsFragment(),
                    new SessionsFragment(),
                    new FriendsFragment(),
                    new UserFragment()
            };
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, "再次按返回键退出", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }
}
