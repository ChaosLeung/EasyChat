package org.zhj.easychat.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import org.zhj.easychat.database.User;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table USER.
*/
public class UserDao extends AbstractDao<User, Long> {

    public static final String TABLENAME = "USER";

    /**
     * Properties of entity User.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property PeerId = new Property(1, String.class, "peerId", false, "PEER_ID");
        public final static Property AvatarUrl = new Property(2, String.class, "avatarUrl", false, "AVATAR_URL");
        public final static Property Introduction = new Property(3, String.class, "introduction", false, "INTRODUCTION");
        public final static Property Gender = new Property(4, String.class, "gender", false, "GENDER");
        public final static Property Area = new Property(5, String.class, "area", false, "AREA");
        public final static Property Nickname = new Property(6, String.class, "nickname", false, "NICKNAME");
        public final static Property Interest = new Property(7, String.class, "interest", false, "INTEREST");
    };


    public UserDao(DaoConfig config) {
        super(config);
    }
    
    public UserDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'USER' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'PEER_ID' TEXT NOT NULL UNIQUE ," + // 1: peerId
                "'AVATAR_URL' TEXT," + // 2: avatarUrl
                "'INTRODUCTION' TEXT," + // 3: introduction
                "'GENDER' TEXT," + // 4: gender
                "'AREA' TEXT," + // 5: area
                "'NICKNAME' TEXT," + // 6: nickname
                "'INTEREST' TEXT);"); // 7: interest
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'USER'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, User entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getPeerId());
 
        String avatarUrl = entity.getAvatarUrl();
        if (avatarUrl != null) {
            stmt.bindString(3, avatarUrl);
        }
 
        String introduction = entity.getIntroduction();
        if (introduction != null) {
            stmt.bindString(4, introduction);
        }
 
        String gender = entity.getGender();
        if (gender != null) {
            stmt.bindString(5, gender);
        }
 
        String area = entity.getArea();
        if (area != null) {
            stmt.bindString(6, area);
        }
 
        String nickname = entity.getNickname();
        if (nickname != null) {
            stmt.bindString(7, nickname);
        }
 
        String interest = entity.getInterest();
        if (interest != null) {
            stmt.bindString(8, interest);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public User readEntity(Cursor cursor, int offset) {
        User entity = new User( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // peerId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // avatarUrl
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // introduction
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // gender
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // area
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // nickname
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7) // interest
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, User entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setPeerId(cursor.getString(offset + 1));
        entity.setAvatarUrl(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setIntroduction(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setGender(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setArea(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setNickname(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setInterest(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(User entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(User entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}