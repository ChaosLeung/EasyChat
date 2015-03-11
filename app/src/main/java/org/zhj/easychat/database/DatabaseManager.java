package org.zhj.easychat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author Chaos
 *         2015/03/01.
 */
public class DatabaseManager {
    private boolean initialized;

    private DaoSession session;

    private static DatabaseManager instance;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void init(Context context) {
        if (initialized) {
            throw new IllegalStateException("init twice");
        }
        initialized = true;
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "Xsb", null);
        SQLiteDatabase writeDatabase = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(writeDatabase);
        session = daoMaster.newSession();
    }

    public DaoSession getSession() {
        return session;
    }

    public UserDao getUserDao() {
        return session.getUserDao();
    }

    public ChatMessageDao getChatMessageDao() {
        return session.getChatMessageDao();
    }
}
