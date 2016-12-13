package in.splitc.share.db;

import android.content.Context;

import java.util.ArrayList;

import in.splitc.share.data.Message;

/**
 * Created by neo on 24/11/16.
 */
public class MessagesDBWrapper {
    public static MessagesDBManager helper;

    public static void Initialize(Context context) {
        helper = new MessagesDBManager(context);
    }

    public static int addMessage(Message user, long timestamp) {
        return helper.addMessage(user, timestamp);
    }

    public static ArrayList<Message> getMessages() {
        return helper.getMessages();
    }
}