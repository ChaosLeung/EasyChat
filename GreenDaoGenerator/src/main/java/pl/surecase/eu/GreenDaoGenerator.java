package pl.surecase.eu;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class GreenDaoGenerator {

    public static void main(String args[]) throws Exception {
        GreenDaoGenerator generator = new GreenDaoGenerator();
        Schema schema = new Schema(1, "org.zhj.easychat.database");
        generator.addUserTable(schema);
        generator.addSessionTable(schema);
        generator.addFriendTable(schema);
        generator.addCommentTable(schema);
        new DaoGenerator().generateAll(schema, args[0]);
    }

    private void addSessionTable(Schema schema) {
        Entity session = schema.addEntity("ChatMessage");
        session.addIdProperty().primaryKey();
        //当前登录的用户
        session.addStringProperty("peerId").notNull();
        //私聊的另一位用户
        session.addStringProperty("otherPeerId").notNull();
        session.addBooleanProperty("isFrom").notNull();
        session.addStringProperty("msg").notNull();
        session.addLongProperty("timestamp").notNull();
    }

    private void addUserTable(Schema schema) {
        Entity user = schema.addEntity("User");
        user.addIdProperty().primaryKey();
        user.addStringProperty("peerId").unique().notNull();
        user.addStringProperty("avatarUrl");
        user.addStringProperty("introduction");
        user.addStringProperty("gender");
        user.addStringProperty("area");
        user.addStringProperty("nickname");
        user.addStringProperty("interest");
    }

    private void addFriendTable(Schema schema) {
        Entity friend = schema.addEntity("Friend");
        friend.addIdProperty().primaryKey();
        friend.addStringProperty("peerId");
        friend.addStringProperty("friendPeerId");
    }

    private void addCommentTable(Schema schema) {
        Entity comment = schema.addEntity("Comment");
        comment.addIdProperty().primaryKey();
        comment.addStringProperty("peerId");
        comment.addStringProperty("postId");
        comment.addStringProperty("content");
    }
}
