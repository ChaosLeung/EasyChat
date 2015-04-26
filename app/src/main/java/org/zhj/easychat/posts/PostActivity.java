package org.zhj.easychat.posts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.squareup.picasso.Picasso;

import org.zhj.easychat.R;
import org.zhj.easychat.app.BaseActionBarActivity;
import org.zhj.easychat.leancloud.LeanCloudUser;
import org.zhj.easychat.posts.comment.Comment;
import org.zhj.easychat.posts.comment.CommentFragment;
import org.zhj.easychat.user.UserInfoActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * @author Chaos
 *         2015/02/25.
 */
public class PostActivity extends BaseActionBarActivity {

    private TextView nicknameText;
    private TextView updateTimeText;
    private TextView contentText;

    private ImageView avatar;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        nicknameText = (TextView) findViewById(R.id.nickname);
        updateTimeText = (TextView) findViewById(R.id.updateTime);
        contentText = (TextView) findViewById(R.id.content);
        avatar = (ImageView) findViewById(R.id.avatar);

        String title = getIntent().getStringExtra("PostTitle");
        setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupControl();

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(userId)) {
                    Intent intent = new Intent(PostActivity.this, UserInfoActivity.class);
                    intent.putExtra("UserId", userId);
                    startActivity(intent);
                }
            }
        });
    }

    private void setupControl() {
        String postObjId = getIntent().getStringExtra("ObjId");
        AVQuery<Post> query = new AVQuery<Post>("Post");
        query.getInBackground(postObjId, new GetCallback<Post>() {
            @Override
            public void done(Post post, AVException e) {
                if (post != null) {
                    userId = post.getUser().getObjectId();
                    setTitle(post.getTitle());
                    contentText.setText(post.getContent());
                    updateTimeText.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(post.getUpdatedAt()));
                    post.getUser().fetchIfNeededInBackground(new GetCallback<AVObject>() {
                        @Override
                        public void done(AVObject avObject, AVException e) {
                            if (avObject != null) {
                                LeanCloudUser trueUser = ((LeanCloudUser) avObject);
                                if (!TextUtils.isEmpty(trueUser.getNickname())) {
                                    nicknameText.setText(trueUser.getNickname());
                                } else {
                                    nicknameText.setText(trueUser.getUsername());
                                }
                                Picasso.with(PostActivity.this)
                                        .load(trueUser.getAvatarUrl())
                                        .placeholder(R.drawable.person_image_empty)
                                        .error(R.drawable.person_image_empty)
                                        .resize(128, 128)
                                        .centerCrop()
                                        .into(avatar);
                            }
                        }
                    });
                }
            }
        });

        CommentFragment fragment = new CommentFragment();
        Bundle bundle = new Bundle();
        bundle.putString("PostId", postObjId);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }
}
