package org.zhj.easychat;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;

import org.zhj.easychat.chat.SessionService;
import org.zhj.easychat.database.DatabaseManager;
import org.zhj.easychat.posts.Post;
import org.zhj.easychat.posts.comment.Comment;

/**
 * @author Chaos
 *         2015/03/11.
 */
public class EasyChatApplication extends Application {

    private static final String APP_ID = "7iom8ztmtgn9eg1zr6cxyzx1elkhghxtj95ztmmmfwrv09bg";
    private static final String APP_KEY = "9m4wndqmgt6trs8m088vrn6kgkexs5623396oa2b8j42eiu7";

    @Override
    public void onCreate() {
        super.onCreate();
        AVObject.registerSubclass(Comment.class);
        AVObject.registerSubclass(Post.class);
        AVOSCloud.initialize(this, APP_ID, APP_KEY);
        //todo 搞回去
        AVOSCloud.setDebugLogEnabled(true);

        DatabaseManager.getInstance().init(this);

        SessionService.getInstance().openSession();
    }
}
