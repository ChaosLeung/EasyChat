package org.zhj.easychat.chat;

import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.Session;
import com.avos.avoscloud.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Chaos
 *         2015/03/03.
 */
public class SessionService {
    private static SessionService instance;

    private List<String> peerIds;

    private SessionService() {
    }

    public static SessionService getInstance() {
        if (instance == null) {
            instance = new SessionService();
        }
        return instance;
    }

    private Session session;

    public void openSession() {
        if (AVUser.getCurrentUser() != null && (session == null || !session.isOpen())) {
            session = SessionManager.getInstance(AVUser.getCurrentUser().getObjectId());
            session.open(new LinkedList<String>());
        }
    }

    public void closeSession() {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    public void watchPeer(String peerId) {
        if (peerIds == null) {
            peerIds = new ArrayList<String>();
        }
        if (!session.isWatching(peerId)) {
            peerIds.add(peerId);
            session.watchPeers(peerIds);
        }
    }

    public void sendMessage(String peerId, String msg) {
        watchPeer(peerId);
        AVMessage avMessage = new AVMessage(msg);
        avMessage.setToPeerIds(Arrays.asList(peerId));
        session.sendMessage(avMessage);
    }
}
