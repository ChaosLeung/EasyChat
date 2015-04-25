package org.zhj.easychat.posts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.squareup.picasso.Picasso;

import org.zhj.easychat.R;
import org.zhj.easychat.leancloud.LeanCloudUser;
import org.zhj.easychat.ui.widget.BezelImageView;

/**
 * @author Chaos
 *         2015/02/22.
 */
public class NewPostActivity extends Activity implements View.OnClickListener {

    private BezelImageView mAvatar;

    private EditText mTitleInput;
    private EditText mContentInput;

    private TextView mIntroduction;
    private TextView mNicknameText;

    private boolean mSending = false;

    private Post mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mAvatar = (BezelImageView) findViewById(R.id.avatar);
        mTitleInput = (EditText) findViewById(R.id.title);
        mIntroduction = (TextView) findViewById(R.id.introduction);
        mContentInput = (EditText) findViewById(R.id.content);
        mNicknameText = (TextView) findViewById(R.id.nickname);
        ImageButton sendButton = (ImageButton) findViewById(R.id.send);
        sendButton.setOnClickListener(this);

        setupControl();
    }


    private void setupControl() {
        LeanCloudUser user = AVUser.getCurrentUser(LeanCloudUser.class);
        if (user != null) {
            if (!TextUtils.isEmpty(user.getNickname())) {
                mNicknameText.setText(user.getNickname());
            } else {
                mNicknameText.setText(user.getUsername());
            }
            mIntroduction.setText(user.getIntroduction());
            if (!TextUtils.isEmpty(user.getAvatarUrl())) {
                Picasso.with(this)
                        .load(user.getAvatarUrl())
                        .placeholder(R.drawable.person_image_empty)
                        .error(R.drawable.person_image_empty)
                        .resize(128, 128)
                        .centerCrop()
                        .into(mAvatar);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                createNewPost();
                break;
        }
    }

    private void createNewPost() {
        String title = mTitleInput.getText().toString().trim();
        String content = mContentInput.getText().toString().trim();
        if (!mSending && title.length() > 0 && content.length() > 0) {
            mSending = true;
            if (mPost == null) {
                mPost = new Post();
            }
            mPost.setTitle(title);
            mPost.setContent(content);
            mPost.setUser(LeanCloudUser.getCurrentUser(LeanCloudUser.class));
            mPost.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        Toast.makeText(getApplicationContext(), "发送成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "发送失败", Toast.LENGTH_SHORT).show();
                    }
                    mSending = false;
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (mSending) {
            return;
        }
        super.onBackPressed();
    }
}
