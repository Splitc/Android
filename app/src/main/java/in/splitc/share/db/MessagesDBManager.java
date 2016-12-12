package in.splitc.share.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import in.splitc.share.data.Message;
import in.splitc.share.utils.CommonLib;

/**
 * Created by neo on 24/11/16.
 */
public class MessagesDBManager extends SQLiteOpenHelper {

    // columns
    private static final String ID = "messageId";
    private static final String SENDER = "sender";
    private static final String TO= "to";
    private static final String TYPE = "type";
    private static final String TYPE_ID = "typeId";
    private static final String MESSAGE = "message";
    private static final String TIMESTAMP = "timestamp";

    // database version
    private static final int DATABASE_VERSION = 2;

    // table name
    private static final String CACHE_TABLE_NAME = "MESSAGES";

    // table create query
    private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + CACHE_TABLE_NAME + " ("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + SENDER + " INTEGER, "
            + TO + " INTEGER, "
            + TYPE + " INTEGER, "
            + TYPE_ID + " INTEGER, "
            + MESSAGE + " VARCHAR, "
            + TIMESTAMP + " INTEGER); ";

    private static final String DATABASE_NAME = "MESSAGESDB";
    Context ctx;

    private String packageName;

    public MessagesDBManager(Context context) {
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
    public int addMessage (Message message, long timestamp) {
        ArrayList<Message> users = getAllMessages();
        int result = -1;
        Cursor cursor = null;
        boolean contains = false;
        try {

            this.getReadableDatabase();

            SQLiteDatabase db = ctx.openOrCreateDatabase("/data/data/" + packageName + "/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READWRITE, null);
            ContentValues values = new ContentValues();
            values.put(TIMESTAMP, timestamp);

            for (Message address : users) {
                if (message.getChatId() == address.getChatId()) {
                    contains = true;
                    break;
                }
            }

            if (contains) {
                result = (int) db.update(CACHE_TABLE_NAME, values, ID + "=? ",
                        new String[] { message.getChatId() + ""});

            } else {

                values.put(SENDER, message.getSender());
                values.put(TO, message.getTo());
                values.put(TYPE, message.getType());
                values.put(TYPE_ID, message.getTypeId());
                values.put(MESSAGE, message.getMessage());

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
        if (!contains) {
            ChatDBWrapper.addChat(message, System.currentTimeMillis() / 1000);
        }
        return result;
    }

    public ArrayList<Message> getAllMessages() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<Message> queries = new ArrayList<Message>();

        try {
            this.getReadableDatabase();

            db = ctx.openOrCreateDatabase("/data/data/" + packageName + "/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READONLY, null);

            cursor = db.query(CACHE_TABLE_NAME, null, null, new String[] {}, null, null, TIMESTAMP + " DESC", "20");

            if (cursor != null)
                cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                Message message = new Message();
                int j = 0;
                message.setChatId(cursor.getInt(j++));
                message.setSender(cursor.getInt(j++));
                message.setTo(cursor.getInt(j++));
                message.setType(cursor.getInt(j++));
                message.setTypeId(cursor.getInt(j++));
                message.setMessage(cursor.getString(j++));
                message.setTimestamp(cursor.getInt(j++));

                queries.add(message);
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

    public ArrayList<Message> getMessages(int type, int typeId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<Message> queries = new ArrayList<Message>();

        try {
            this.getReadableDatabase();

            db = ctx.openOrCreateDatabase("/data/data/" + packageName + "/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READONLY, null);

            cursor = db.query(CACHE_TABLE_NAME, null,
                    TYPE + " =? AND" + TYPE_ID + " =?", new String[] {Integer.toString(type), Integer.toString(typeId)}, null, null, null, null);

            if (cursor != null)
                cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                Message message = new Message();
                int j = 0;
                message.setChatId(cursor.getInt(j++));
                message.setSender(cursor.getInt(j++));
                message.setTo(cursor.getInt(j++));
                message.setType(cursor.getInt(j++));
                message.setTypeId(cursor.getInt(j++));
                message.setMessage(cursor.getString(j++));
                message.setTimestamp(cursor.getInt(j++));
                queries.add(message);
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