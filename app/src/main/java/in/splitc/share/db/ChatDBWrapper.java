package in.splitc.share.db;

import android.content.Context;

import java.util.ArrayList;

import in.splitc.share.data.Message;

/**
 * Created by neo on 03/12/16.
 */
public class ChatDBWrapper {
    public static ChatDBManager helper;

    public static void Initialize(Context context) {
        helper = new ChatDBManager(context);
    }

    public static int addChat(Message chat, long timestamp, int creatorId) {
        return helper.addChat(chat, timestamp, creatorId);
    }

    public static ArrayList<Message> getAllChats() {
        return helper.getAllChats();
    }
}
