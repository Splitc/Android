package in.splitc.share.db;

import android.content.Context;

import java.util.ArrayList;

import in.splitc.share.data.User;

/**
 * Created by neo on 24/11/16.
 */
public class UserDBWrapper {
    public static UserDBManager helper;

    public static void Initialize(Context context) {
        helper = new UserDBManager(context);
    }

    public static int addUser(User user, long timestamp) {
        return helper.addUser(user, timestamp);
    }

    public static ArrayList<User> getUsers() {
        return helper.getAllMessages();
    }

    public static User getUser(int userId) {
        return helper.getUser(userId);
    }
}
