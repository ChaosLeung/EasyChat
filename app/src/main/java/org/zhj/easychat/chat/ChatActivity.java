package org.zhj.easychat.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.squareup.picasso.Picasso;

import org.zhj.easychat.ArrayRecyclerAdapter;
import org.zhj.easychat.R;
import org.zhj.easychat.app.BaseActionBarActivity;
import org.zhj.easychat.database.ChatMessage;
import org.zhj.easychat.database.ChatMessageDao;
import org.zhj.easychat.database.DatabaseManager;
import org.zhj.easychat.database.User;
import org.zhj.easychat.database.UserDao;
import org.zhj.easychat.leancloud.LeanCloudUser;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;


/**
 * @author Chaos
 *         2015/02/26.
 */
public class ChatActivity extends BaseActionBarActivity implements ChatMessageReceiver.MessageListener {

    private String otherPeerId;
    private List<ChatMessage> chatMessages;

    private RecyclerView messagesView;

    private EditText inputText;

    private ImageButton sendButton;

    private SwipeRefreshLayout refreshLayout;

    private MessagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (!getIntent().hasExtra("UserId")) {
            finish();
            return;
        }

        setupUserInfo();
        SessionService.getInstance().openSession();
        SessionService.getInstance().watchPeer(otherPeerId);
        setupActionBar();

        messagesView = (RecyclerView) findViewById(R.id.recyclerView);
        messagesView.setLayoutManager(new LinearLayoutManager(this));
        inputText = (EditText) findViewById(R.id.input);
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

        sendButton = (ImageButton) findViewById(R.id.send);
        sendButton.setEnabled(false);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = inputText.getText().toString().trim();
                SessionService.getInstance().sendMessage(otherPeerId, text);
                inputText.setText("");
            }
        });

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiping);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryChatMessage();
                refreshLayout.setRefreshing(false);
            }
        });

        chatMessages = new ArrayList<ChatMessage>();
        adapter = new MessagesAdapter(this);
        messagesView.setAdapter(adapter);

        queryChatMessage();
        messagesView.scrollToPosition(adapter.getItemCount() - 1);

        ChatMessageReceiver.addMessageListener(this);
    }

    @Override
    protected void onDestroy() {
        ChatMessageReceiver.removeMessageListener(this);
        super.onDestroy();
    }

    private void setupUserInfo() {
        otherPeerId = getIntent().getStringExtra("UserId");
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("Nickname"));
    }

    private void queryChatMessage() {
        QueryBuilder<ChatMessage> qb = DatabaseManager.getInstance().getChatMessageDao().queryBuilder();
        qb.where(ChatMessageDao.Properties.OtherPeerId.eq(otherPeerId), ChatMessageDao.Properties.PeerId.eq(AVUser.getCurrentUser().getObjectId()));
        qb.offset(chatMessages.size()).limit(50).orderAsc(ChatMessageDao.Properties.Id);
        adapter.addAll(0, qb.list());
        adapter.notifyDataSetChanged();
    }

    private void addMessageToBottom(ChatMessage message) {
        adapter.add(message);
        adapter.notifyItemInserted(adapter.getItemCount() - 1);
        messagesView.smoothScrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onMessage(ChatMessage message) {
        if (otherPeerId.equals(message.getOtherPeerId())) {
            addMessageToBottom(message);
        }
    }

    @Override
    public void onMessageSent(ChatMessage message) {
        if (otherPeerId.equals(message.getOtherPeerId())) {
            addMessageToBottom(message);
        }
    }

    @Override
    public void onMessageDelivered(ChatMessage message) {

    }

    @Override
    public void onMessageFailure(ChatMessage message) {
        if (otherPeerId.equals(message.getOtherPeerId())) {
            Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT).show();
        }
    }

    private class MessagesAdapter extends ArrayRecyclerAdapter<ChatMessage, MessagesAdapter.ViewHolder> {

        private static final int TYPE_LEFT = 1;
        private static final int TYPE_RIGHT = 2;

        private Context context;

        public MessagesAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getItemViewType(int position) {
            if (get(position).getIsFrom()) {
                return TYPE_LEFT;
            } else {
                return TYPE_RIGHT;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView;
            if (viewType == TYPE_LEFT) {
                rootView = getLayoutInflater().inflate(R.layout.item_chat_left, parent, false);
            } else {
                rootView = getLayoutInflater().inflate(R.layout.item_chat_right, parent, false);
            }
            return new ViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            ChatMessage message = get(position);
            holder.message.setText(message.getMsg());
            QueryBuilder<User> qb = DatabaseManager.getInstance().getUserDao().queryBuilder();
            qb.where(UserDao.Properties.PeerId.eq(message.getIsFrom() ? message.getOtherPeerId() : message.getPeerId()));
            User user = qb.unique();
            //todo 两个用户本地变量存着就好
            if (user != null) {
                loadAvatar(holder, user.getAvatarUrl());
            } else {
                AVQuery<LeanCloudUser> query = AVUser.getUserQuery(LeanCloudUser.class);
                query.whereEqualTo("objectId", message.getIsFrom() ? message.getOtherPeerId() : message.getPeerId());
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
                            loadAvatar(holder, user.getAvatarUrl());
                        }
                    }
                });
            }
        }

        private void loadAvatar(ViewHolder viewHolder, String url) {
            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.default_avatar_blue)
                    .error(R.drawable.default_avatar_blue)
                    .resize(128, 128)
                    .centerCrop()
                    .into(viewHolder.avatar);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView message;
            private ImageView avatar;

            public ViewHolder(View itemView) {
                super(itemView);
                message = (TextView) itemView.findViewById(R.id.message);
                avatar = (ImageView) itemView.findViewById(R.id.avatar);
            }
        }
    }
}
