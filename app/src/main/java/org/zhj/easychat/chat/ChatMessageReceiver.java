package org.zhj.easychat.chat;

import android.content.Context;
import android.util.Log;

import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.AVMessageReceiver;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.Session;

import org.zhj.easychat.database.ChatMessage;
import org.zhj.easychat.database.DatabaseManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Chaos
 *         2015/02/26.
 */
public class ChatMessageReceiver extends AVMessageReceiver {

    private static Set<MessageListener> messageListeners = new HashSet<MessageListener>();

    @Override
    public void onSessionOpen(Context context, Session session) {
        logE("onSessionOpen");
    }

    @Override
    public void onSessionPaused(Context context, Session session) {
        logE("onSessionPaused");
    }

    @Override
    public void onSessionResumed(Context context, Session session) {
        logE("onSessionResumed");
    }

    @Override
    public void onPeersWatched(Context context, Session session, List<String> strings) {
        logE("onPeersWatched");
    }

    @Override
    public void onPeersUnwatched(Context context, Session session, List<String> strings) {
        logE("onPeersUnwatched");
    }

    @Override
    public void onMessage(Context context, Session session, AVMessage avMessage) {
        logE("onMessage");
        ChatMessage message = covertMessage(avMessage);
        DatabaseManager.getInstance().getChatMessageDao().insert(message);
        for (MessageListener listener : messageListeners) {
            listener.onMessage(message);
        }
    }

    @Override
    public void onMessageSent(Context context, Session session, AVMessage avMessage) {
        logE("onMessageSent");
        ChatMessage message = covertMessage(avMessage);
        DatabaseManager.getInstance().getChatMessageDao().insert(message);
        for (MessageListener listener : messageListeners) {
            listener.onMessageSent(message);
        }
    }

    @Override
    public void onMessageDelivered(Context context, Session session, AVMessage avMessage) {
        logE("onMessageDelivered");
        for (MessageListener listener : messageListeners) {
            listener.onMessageDelivered(covertMessage(avMessage));
        }
    }

    @Override
    public void onMessageFailure(Context context, Session session, AVMessage avMessage) {
        logE("onMessageFailure");
        for (MessageListener listener : messageListeners) {
            listener.onMessageFailure(covertMessage(avMessage));
        }
    }

    @Override
    public void onStatusOnline(Context context, Session session, List<String> strings) {
        logE("onStatusOnline");
    }

    @Override
    public void onStatusOffline(Context context, Session session, List<String> strings) {
        logE("onStatusOffline");
    }

    @Override
    public void onError(Context context, Session session, Throwable throwable) {
        logE("onError ");
        throwable.printStackTrace();
    }

    private void logE(String msg) {
        Log.e("MessageReceiver", msg);
    }

    private ChatMessage covertMessage(AVMessage avMessage) {
        String userId = AVUser.getCurrentUser().getObjectId();
        boolean isFrom = !avMessage.getFromPeerId().equals(AVUser.getCurrentUser().getObjectId());
        ChatMessage message = new ChatMessage();
        message.setMsg(avMessage.getMessage());
        message.setPeerId(userId);
        message.setOtherPeerId(isFrom ? avMessage.getFromPeerId() : avMessage.getToPeerIds().get(0));
        message.setTimestamp(avMessage.getTimestamp());
        message.setIsFrom(isFrom);
        return message;
    }

    public static void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public static void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    public interface MessageListener {
        void onMessage(ChatMessage message);

        void onMessageSent(ChatMessage message);

        void onMessageDelivered(ChatMessage message);

        void onMessageFailure(ChatMessage message);
    }
}
