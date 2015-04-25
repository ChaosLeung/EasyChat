package org.zhj.easychat.posts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.squareup.picasso.Picasso;

import org.zhj.easychat.ArrayRecyclerAdapter;
import org.zhj.easychat.R;
import org.zhj.easychat.leancloud.LeanCloudUser;
import org.zhj.easychat.user.UserInfoActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * @author Chaos
 *         2015/02/22.
 */
public class PostsFragment extends Fragment implements View.OnClickListener {

    private ImageButton mCreateFab;
    private RecyclerView postsView;
    private SwipeRefreshLayout refreshLayout;
    private PostsAdapter adapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle("主页");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_posts, container, false);
        mCreateFab = (ImageButton) rootView.findViewById(R.id.create);
        mCreateFab.setOnClickListener(this);

        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiping);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryPosts();
            }
        });

        postsView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        postsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postsView.setItemAnimator(new DefaultItemAnimator());
        adapter = new PostsAdapter();
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Post item) {
                Intent intent = new Intent(getActivity(), PostActivity.class);
                intent.putExtra("ObjId", item.getObjectId());
                intent.putExtra("PostTitle", item.getTitle());
                startActivity(intent);
            }
        });
        postsView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queryPosts();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create:
                startActivity(new Intent(getActivity(), NewPostActivity.class));
                break;
        }
    }

    private void queryPosts() {
        refreshLayout.setRefreshing(true);
        Post.queryAll(new FindCallback<Post>() {

            private List<Post> posts;

            private int totalCount = 0;
            private int currentCount = 0;
            private GetCallback<AVObject> getCallback = new GetCallback<AVObject>() {
                @Override
                public void done(AVObject user, AVException e) {
                    if (++currentCount >= totalCount) {
                        adapter.replaceWith(posts);
                        refreshLayout.setRefreshing(false);
                    }
                }
            };

            @Override
            public void done(List<Post> posts, AVException e) {
                if (posts != null && e == null) {
                    this.posts = posts;
                    for (Post post : posts) {
                        post.getUser().fetchIfNeededInBackground(getCallback);
                    }
                }

                if (e != null) {
                    Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
                    refreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private class PostsAdapter extends ArrayRecyclerAdapter<Post, PostsAdapter.ViewHolder> {

        private OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ViewHolder(getActivity().getLayoutInflater().inflate(R.layout.item_post, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, int i) {
            final Post post = get(i);
            viewHolder.title.setText(post.getTitle());
            viewHolder.content.setText(post.getContent());
            viewHolder.time.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(post.getUpdatedAt()));
            viewHolder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                    intent.putExtra("UserId", post.getUser().getObjectId());
                    startActivity(intent);
                }
            });

            LeanCloudUser user = post.getUser();
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

            if (onItemClickListener != null) {
                viewHolder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onItemClick(post);
                    }
                });
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
            private TextView title;
            private TextView content;
            private TextView nickname;
            private View container;

            public ViewHolder(View itemView) {
                super(itemView);
                avatar = (ImageView) itemView.findViewById(R.id.avatar);
                time = (TextView) itemView.findViewById(R.id.time);
                title = (TextView) itemView.findViewById(R.id.title);
                content = (TextView) itemView.findViewById(R.id.content);
                nickname = (TextView) itemView.findViewById(R.id.nickname);
                container = itemView.findViewById(R.id.container);
            }
        }
    }
}
