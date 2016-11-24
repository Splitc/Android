package in.splitc.share.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import in.splitc.share.data.User;

/**
 * Created by neo on 24/11/16.
 */
public class UserDBManager extends SQLiteOpenHelper {

    // columns
    private static final String ID = "userId";
    private static final String NAME = "fullName";
    private static final String IMAGE = "imageUrl";
    private static final String TIMESTAMP = "timestamp";

    // database version
    private static final int DATABASE_VERSION = 2;

    // table name
    private static final String CACHE_TABLE_NAME = "USERS";

    // table create query
    private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + CACHE_TABLE_NAME + " ("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + NAME + " VARCHAR, "
            + IMAGE + " VARCHAR(1000), "
            + TIMESTAMP + " INTEGER); ";

    private static final String DATABASE_NAME = "USERSDB";
    Context ctx;

    private String packageName;

    public UserDBManager(Context context) {
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
    public int addUser (User message, long timestamp) {
        ArrayList<User> users = getAllMessages();
        int result = -1;
        Cursor cursor = null;
        try {

            this.getReadableDatabase();

            SQLiteDatabase db = ctx.openOrCreateDatabase("/data/data/" + packageName + "/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READWRITE, null);
            ContentValues values = new ContentValues();
            values.put(TIMESTAMP, timestamp);

            boolean contains = false;

            for (User address : users) {
                if (message.getUserId() == address.getUserId()){
                    contains = true;
                    break;
                }
            }

            if (contains) {
                result = (int) db.update(CACHE_TABLE_NAME, values, ID + "=?",
                        new String[] { message.getUserId() + "" });

            } else {

                values.put(ID, message.getUserId());
                values.put(NAME, message.getUserName());
                values.put(IMAGE, message.getProfilePic());

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

    public ArrayList<User> getAllMessages() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<User> queries = new ArrayList<User>();

        try {
            this.getReadableDatabase();

            db = ctx.openOrCreateDatabase("/data/data/" + packageName + "/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READONLY, null);

            cursor = db.query(CACHE_TABLE_NAME, new String[] { ID, NAME, IMAGE, TIMESTAMP },
                    "", new String[] {}, null, null, TIMESTAMP + " DESC", "20");

            if (cursor != null)
                cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                User message = new User();
                int j = 0;

                message.setUserId(cursor.getInt(j++));
                message.setUserName(cursor.getString(j++));
                message.setProfilePic(cursor.getString(j++));

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

    public User getUser(int userId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        User queries = null;

        try {
            this.getReadableDatabase();

            db = ctx.openOrCreateDatabase("/data/data/" + packageName + "/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READONLY, null);

            cursor = db.query(CACHE_TABLE_NAME, new String[] { ID, NAME, IMAGE, TIMESTAMP },
                    ID + " =? ", new String[] {Integer.toString(userId)}, null, null, null, "1");

            if (cursor != null)
                cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                queries = new User();
                int j = 0;
                queries.setUserId(cursor.getInt(j++));
                queries.setUserName(cursor.getString(j++));
                queries.setProfilePic(cursor.getString(j++));
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