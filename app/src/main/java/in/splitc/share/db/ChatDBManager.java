package in.splitc.share.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import in.splitc.share.data.Feed;
import in.splitc.share.data.Message;
import in.splitc.share.data.User;

/**
 * Created by neo on 03/12/16.
 */
public class ChatDBManager extends SQLiteOpenHelper {

    // columns
    private static final String ID = "chatId";
    private static final String FEED_ID = "feedId";
    private static final String FEED_TYPE = "feedType";
    private static final String FROM_ADDRESS = "from";
    private static final String TO_ADDRESS = "to";
    private static final String START_TIME = "startTime";
    private static final String REQUIRED_PERSONS = "requiredPersons";
    private static final String DESCRIPTION = "description";
    private static final String USER_ID = "userId";
    private static final String USER_NAME = "userName";
    private static final String USER_IMAGE_URL = "userImage";
    private static final String TIMESTAMP = "timestamp";

    // database version
    private static final int DATABASE_VERSION = 2;

    // table name
    private static final String CACHE_TABLE_NAME = "CHATS";

    // table create query
    private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + CACHE_TABLE_NAME + " ("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + FEED_ID + " INTEGER, "
            + FEED_TYPE + " INTEGER, "
            + FROM_ADDRESS + " VARCHAR, "
            + TO_ADDRESS + " VARCHAR, "
            + START_TIME + " INTEGER, "
            + REQUIRED_PERSONS + " INTEGER, "
            + DESCRIPTION + " VARCHAR(1000), "
            + USER_ID + " INTEGER, "
            + USER_NAME + " VARCHAR, "
            + USER_IMAGE_URL + " VARCHAR(1000), "
            + TIMESTAMP + " INTEGER); ";

    private static final String DATABASE_NAME = "CHATSDB";
    Context ctx;

    private String packageName;

    public ChatDBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
        packageName = ctx.getPackageName();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DICTIONARY_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Called when a message is sent
    public int addChat (Message message, long timestamp) {
        ArrayList<Message> users = getAllChats();
        int result = -1;
        Cursor cursor = null;
        try {

            this.getReadableDatabase();

            SQLiteDatabase db = ctx.openOrCreateDatabase("/data/data/" + packageName + "/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READWRITE, null);
            ContentValues values = new ContentValues();
            values.put(TIMESTAMP, timestamp);

            boolean contains = false;

            for (Message address : users) {
                if (message.getFeedId() == address.getFeedId() && message.getFeedType() == address.getFeedType()) {
                    contains = true;
                    break;
                }
            }

            if (contains) {
                result = (int) db.update(CACHE_TABLE_NAME, values, FEED_ID + "=? AND " + FEED_TYPE + "=?",
                        new String[] { message.getFeedId() + "", message.getFeedType() + ""});

            } else {

                values.put(FEED_ID, message.getFeedId());
                values.put(FEED_TYPE, message.getFeedType());
                values.put(FROM_ADDRESS, message.getSourceAddress());
                values.put(TO_ADDRESS, message.getDestinationAddress());
                values.put(START_TIME, message.getStartTime());
                values.put(REQUIRED_PERSONS, message.getRequiredPersons());
                values.put(DESCRIPTION, message.getDescription());
                values.put(USER_ID, message.getUserId());
                values.put(USER_NAME, message.getUserName());
                values.put(USER_IMAGE_URL, message.getProfilePic());

                // Inserting Row
                result = (int) db.insertOrThrow(CACHE_TABLE_NAME, null, values);
            }

            db.close();
            this.close();
        } catch (Exception E) {
            try {
                this.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            result = -1;
        }
        return result;
    }

    public ArrayList<Message> getAllChats() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<Message> queries = new ArrayList<Message>();

        try {
            this.getReadableDatabase();

            db = ctx.openOrCreateDatabase("/data/data/" + packageName + "/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READONLY, null);

            cursor = db.query(CACHE_TABLE_NAME, null, null, new String[] {}, null, null, TIMESTAMP + " DESC", null);

            if (cursor != null)
                cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                Message feed = new Message();
                int j = 1;

                feed.setFeedId(cursor.getInt(j++));
                feed.setFeedType(cursor.getInt(j++));
                feed.setSourceAddress(cursor.getString(j++));
                feed.setDestinationAddress(cursor.getString(j++));
                feed.setStartTime(cursor.getInt(j++));
                feed.setRequiredPersons(cursor.getInt(j++));
                feed.setDescription(cursor.getString(j++));
                feed.setUserId(cursor.getInt(j++));
                feed.setUserName(cursor.getString(j++));
                feed.setProfilePic(cursor.getString(j++));

                queries.add(feed);
            }

            cursor.close();
            db.close();
            this.close();
            return queries;
        } catch (SQLiteException e) {

            this.close();
        } catch (Exception E) {
            try {
                cursor.close();
                db.close();
                this.close();
            } catch (Exception ec) {
                try {
                    db.close();
                } catch (Exception e) {
                    this.close();
                }
                this.close();
            }
        }
        return queries;
    }

}