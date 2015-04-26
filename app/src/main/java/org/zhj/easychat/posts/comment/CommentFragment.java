package org.zhj.easychat.posts.comment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.squareup.picasso.Picasso;

import org.zhj.easychat.ArrayRecyclerAdapter;
import org.zhj.easychat.R;
import org.zhj.easychat.leancloud.LeanCloudUser;
import org.zhj.easychat.user.UserInfoActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Chaos
 *         2015/04/26.
 */
public class CommentFragment extends Fragment {

    private RecyclerView commentsView;

    private EditText inputText;

    private ImageButton sendButton;

    private SwipeRefreshLayout refreshLayout;

    private CommentsAdapter adapter;

    private String postId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postId = getArguments().getString("PostId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sub_comment, container, false);
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiping);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryComments();
            }
        });

        commentsView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        commentsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        commentsView.setItemAnimator(new DefaultItemAnimator());
        adapter = new CommentsAdapter();
        commentsView.setAdapter(adapter);

        inputText = (EditText) rootView.findViewById(R.id.input);
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sendButton = (ImageButton) rootView.findViewById(R.id.send);
        sendButton.setEnabled(false);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = inputText.getText().toString().trim();
                inputText.setText("");
                final Comment comment = new Comment();
                comment.setContent(text);
                comment.setPostId(postId);
                comment.setUser(LeanCloudUser.getCurrentUser2());
                comment.setFetchWhenSave(true);
                comment.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            Toast.makeText(getActivity(), "评论成功", Toast.LENGTH_SHORT).show();
                            adapter.add(0, comment);
                            adapter.notifyItemInserted(0);
                        } else {
                            Toast.makeText(getActivity(), "评论失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queryComments();
    }

    private void queryComments() {
        refreshLayout.setRefreshing(true);
        Comment.queryAll(new FindCallback<Comment>() {

            private List<Comment> comments;

            private int totalCount = 0;
            private int currentCount = 0;
            private GetCallback<AVObject> getCallback = new GetCallback<AVObject>() {
                @Override
                public void done(AVObject user, AVException e) {
                    if (++currentCount >= totalCount) {
                        adapter.replaceWith(comments);
                        refreshLayout.setRefreshing(false);
                    }
                }
            };

            @Override
            public void done(List<Comment> comments, AVException e) {
                if (comments != null && e == null) {
                    this.comments = comments;
                    for (Comment comment : comments) {
                        comment.getUser().fetchIfNeededInBackground(getCallback);
                    }
                }

                if (e != null) {
                    Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
                    refreshLayout.setRefreshing(false);
                }
            }
        }, postId);
    }

    private class CommentsAdapter extends ArrayRecyclerAdapter<Comment, CommentsAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ViewHolder(getActivity().getLayoutInflater().inflate(R.layout.item_comment, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, int i) {
            final Comment comment = get(i);
            viewHolder.content.setText(comment.getContent());
                viewHolder.time.setText(new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(
                        comment.getUpdatedAt() != null ? comment.getUpdatedAt() : new Date()));
            viewHolder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                    intent.putExtra("UserId", comment.getUser().getObjectId());
                    startActivity(intent);
                }
            });

            LeanCloudUser user = comment.getUser();
            if (user != null) {
                LeanCloudUser currentUser = LeanCloudUser.getCurrentUser2();
                if (currentUser != null && user.getObjectId().equals(currentUser.getObjectId())) {
                    setupUserInfo(viewHolder, currentUser);
                } else {
                    user.fetchIfNeededInBackground(new GetCallback<AVObject>() {
                        @Override
                        public void done(AVObject avObject, AVException e) {
                            if (avObject != null) {
                                LeanCloudUser trueUser = ((LeanCloudUser) avObject);
                                setupUserInfo(viewHolder, trueUser);
                            }
                        }
                    });
                }
            }
        }

        private void setupUserInfo(ViewHolder viewHolder, LeanCloudUser user) {
            if (!TextUtils.isEmpty(user.getNickname())) {
                viewHolder.nickname.setText(user.getNickname());
            } else {
                viewHolder.nickname.setText(user.getUsername());
            }
            Picasso.with(getActivity())
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.person_image_empty)
                    .error(R.drawable.person_image_empty)
                    .resize(128, 128)
                    .centerCrop()
                    .into(viewHolder.avatar);
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            holder.avatar.setImageBitmap(null);
            super.onViewRecycled(holder);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView avatar;
            private TextView time;
            private TextView content;
            private TextView nickname;

            public ViewHolder(View itemView) {
                super(itemView);
                avatar = (ImageView) itemView.findViewById(R.id.avatar);
                time = (TextView) itemView.findViewById(R.id.time);
                content = (TextView) itemView.findViewById(R.id.content);
                nickname = (TextView) itemView.findViewById(R.id.nickname);
            }
        }
    }
}
