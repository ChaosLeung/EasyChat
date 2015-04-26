package org.zhj.easychat.posts.comment;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

import org.zhj.easychat.leancloud.LeanCloudUser;
import org.zhj.easychat.posts.Post;

/**
 * @author Chaos
 *         2015/02/24.
 */
@AVClassName("Comment")
public class Comment extends AVObject {

    private static final String OBJ_NAME = "Comment";

    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_USER = "user";
    private static final String FIELD_START_TIME = "createdAt";
    private static final String FIELD_POST = "post";

    public void setContent(String content) {
        put(FIELD_CONTENT, content);
    }

    public void setUser(LeanCloudUser user) {
        put(FIELD_USER, user);
    }

    public void setPostId(String postId) {
        put(FIELD_POST, postId);
    }

    public String getContent() {
        return getValue(FIELD_CONTENT);
    }

    public LeanCloudUser getUser() {
        return getAVUser(FIELD_USER, LeanCloudUser.class);
    }

    public String getPostId() {
        return getValue(FIELD_POST);
    }

    private <T> T getValue(String key) {
        return (T) get(key);
    }

    public static void queryAll(FindCallback<Comment> callback, String postId) {
        AVQuery<Comment> query = new AVQuery<Comment>(OBJ_NAME);
        query.orderByDescending(FIELD_START_TIME);
        query.whereEqualTo(FIELD_POST, postId);
        query.findInBackground(callback);
    }
}
