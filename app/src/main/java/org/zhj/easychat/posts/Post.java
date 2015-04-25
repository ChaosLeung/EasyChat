package org.zhj.easychat.posts;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

import org.zhj.easychat.leancloud.LeanCloudUser;

/**
 * @author Chaos
 *         2015/02/24.
 */
@AVClassName("Post")
public class Post extends AVObject {

    private static final String OBJ_NAME = "Post";

    private static final String FIELD_TITLE = "title";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_START_TIME = "createdAt";
    private static final String FIELD_USER = "user";

    public void setTitle(String title) {
        put(FIELD_TITLE, title);
    }

    public void setContent(String content) {
        put(FIELD_CONTENT, content);
    }

    public void setUser(LeanCloudUser user) {
        put(FIELD_USER, user);
    }

    public String getTitle() {
        return getValue(FIELD_TITLE);
    }

    public String getContent() {
        return getValue(FIELD_CONTENT);
    }

    public LeanCloudUser getUser() {
        return getAVUser(FIELD_USER, LeanCloudUser.class);
    }

    private <T> T getValue(String key) {
        return (T) get(key);
    }

    public static void queryAll(FindCallback<Post> callback){
        AVQuery<Post> query = new AVQuery<Post>(OBJ_NAME);
        query.orderByDescending(FIELD_START_TIME);
        query.findInBackground(callback);
    }
}
