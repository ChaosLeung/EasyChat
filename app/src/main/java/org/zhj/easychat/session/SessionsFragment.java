package org.zhj.easychat.session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.squareup.picasso.Picasso;

import org.zhj.easychat.ArrayRecyclerAdapter;
import org.zhj.easychat.R;
import org.zhj.easychat.chat.ChatActivity;
import org.zhj.easychat.database.ChatMessage;
import org.zhj.easychat.database.DatabaseManager;
import org.zhj.easychat.database.User;
import org.zhj.easychat.database.UserDao;
import org.zhj.easychat.leancloud.LeanCloudUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.greenrobot.dao.query.QueryBuilder;


/**
 * @author Chaos
 *         2015/02/22.
 */
public class SessionsFragment extends Fragment {

    private RecyclerView sessionsView;
    private TextView noneTipsText;
    private List<ChatMessage> chatMessages;
    private SessionsAdapter sessionsAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle("聊天信息");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sessions, container, false);
        sessionsView = (RecyclerView) rootView.findViewById(R.id.sessions);
        sessionsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sessionsView.setItemAnimator(new DefaultItemAnimator());

        noneTipsText = (TextView) rootView.findViewById(R.id.noneTips);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatMessages = new ArrayList<ChatMessage>();
        if (AVUser.getCurrentUser() != null) {
            String sql = "select OTHER_PEER_ID,MSG,TIMESTAMP from CHAT_MESSAGE where PEER_ID ='" + AVUser.getCurrentUser().getObjectId() + "' GROUP BY OTHER_PEER_ID ORDER BY TIMESTAMP desc limit 1";
            Cursor cursor = DatabaseManager.getInstance().getSession().getDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                String otherPeerId = cursor.getString(0);
                String msg = cursor.getString(1);
                long timestamp = cursor.getLong(2);
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setMsg(msg);
                chatMessage.setOtherPeerId(otherPeerId);
                chatMessage.setTimestamp(timestamp);
                chatMessages.add(chatMessage);
            }
            cursor.close();
        }
        sessionsAdapter = new SessionsAdapter();
        sessionsView.setAdapter(sessionsAdapter);
        sessionsAdapter.replaceWith(chatMessages);
        sessionsAdapter.setOnItemClickListener(getActivity(), new SessionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ChatMessage chatMessage) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("UserId", chatMessage.getOtherPeerId());
                User user= DatabaseManager.getInstance().getUserDao().queryBuilder().where(UserDao.Properties.PeerId.eq(chatMessage.getOtherPeerId())).unique();
                intent.putExtra("Nickname", user.getNickname());
                getActivity().startActivity(intent);
            }
        });
        if (chatMessages.size() > 0) {
            noneTipsText.setVisibility(View.GONE);
        }
    }

    private static class SessionsAdapter extends ArrayRecyclerAdapter<ChatMessage, SessionsAdapter.ViewHolder> {

        private OnItemClickListener onItemClickListener;
        private Context context;

        public void setOnItemClickListener(Context context, OnItemClickListener onItemClickListener) {
            this.context = context;
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_session, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, int i) {
            final ChatMessage session = get(i);
            String otherPeerId = session.getOtherPeerId();
            QueryBuilder<User> qb = DatabaseManager.getInstance().getUserDao().queryBuilder();
            qb.where(UserDao.Properties.PeerId.eq(otherPeerId));
            final User user = qb.unique();

            viewHolder.time.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(session.getTimestamp()));
            viewHolder.content.setText(session.getMsg());
            viewHolder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(context, UserInfoActivity.class);
//                    intent.putExtra("UserId", session.getOtherPeerId());
//                    intent.putExtra("Nickname", user.getNickname());
//                    context.startActivity(intent);
                }
            });

            if (user != null) {
                setupUserInfo(viewHolder, user);
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
                            setupUserInfo(viewHolder, user);
                        }
                    }
                });
            }

            if (onItemClickListener != null) {
                viewHolder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onItemClick(session);
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

        @Override
        public void onViewRecycled(ViewHolder holder) {
            holder.avatar.setImageBitmap(null);
            super.onViewRecycled(holder);
        }

        public interface OnItemClickListener {
            void onItemClick(ChatMessage chatMessage);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView avatar;
            private TextView time;
            private TextView content;
            private TextView nickname;
            private View container;

            public ViewHolder(View itemView) {
                super(itemView);
                avatar = (ImageView) itemView.findViewById(R.id.avatar);
                time = (TextView) itemView.findViewById(R.id.time);
                content = (TextView) itemView.findViewById(R.id.content);
                nickname = (TextView) itemView.findViewById(R.id.nickname);
                container = itemView.findViewById(R.id.container);
            }
        }
    }
}
