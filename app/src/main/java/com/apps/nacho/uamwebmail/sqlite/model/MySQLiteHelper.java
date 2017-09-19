package com.apps.nacho.uamwebmail.sqlite.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by nacho on 12/10/16.
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static  final String COLUMN_USER_ACTIVE = "active";

    public static final String TABLE_FOLDERS = "folders";
    public static final String COLUMN_FOLDER_ID = "_id";
    public static final String COLUMN_FOLDER_NAME = "name";
    public static final String COLUMN_FOLDER_LAST_UID = "last_uid";
    public static final String COLUMN_FOLDER_HMSEQ = "hmseq";
    public static final String COLUMN_FOLDER_MESSAGE_NUMBER = "message_number";
    public static final String COLUMN_FOLDER_NEW_MESSAGE_NUMBER = "new_message_number";
    public static final String COLUMN_FOLDER_USER_ID = "user_id";

    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_MESSAGE_ID = "_id";
    public static final String COLUMN_MESSAGE_UID = "uid";
    public static final String COLUMN_MESSAGE_SUBJECT = "subject";
    public static final String COLUMN_MESSAGE_SENT_DATE = "sent_date";
    public static final String COLUMN_MESSAGE_SEEN = "seen";
    public static final String COLUMN_MESSAGE_SHOW_IMAGES = "show_images";
    public static final String COLUMN_MESSAGE_FOLDER_ID = "folder_id";

    public static final String TABLE_CONTACTS = "contacts";
    public static final String COLUMN_CONTACT_ID = "_id";
    public static final String COLUMN_CONTACT_EMAIL = "email";
    public static final String COLUMN_CONTACT_NAME = "name";

    public static final String TABLE_MESSAGECONTACTS = "message_contacts";
    public static final String COLUMN_MESSAGECONTACT_ID = "_id";
    public static final String COLUMN_MESSAGECONTACT_MESSAGE_ID = "message_id";
    public static final String COLUMN_MESSAGECONTACT_CONTACT_ID = "contact_id";
    public static final String COLUMN_MESSAGECONTACT_TYPE = "type";

    public static final String TABLE_USERCONTACTS = "user_contacts";
    public static final String COLUMN_USERCONTACT_ID = "_id";
    public static final String COLUMN_USERCONTACT_USER_ID = "user_id";
    public static final String COLUMN_USERCONTACT_CONTACT_ID = "contact_id";

    public static final String TABLE_ATTACHMENTS = "attachments";
    public static final String COLUMN_ATTACHMENT_ID = "_id";
    public static final String COLUMN_ATTACHMENT_NAME = "name";
    public static final String COLUMN_ATTACHMENT_FORMAT = "format";
    public static final String COLUMN_ATTACHMENT_PATH = "path";
    public static final String COLUMN_ATTACHMENT_MESSAGE_ID = "msg_id";

    private static final String DATABASE_NAME = "emails.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_EMAIL + " TEXT NOT NULL, "
            + COLUMN_USER_PASSWORD + " TEXT NOT NULL, "
            + COLUMN_USER_ACTIVE + " INTEGER NOT NULL "
            + ");";

    private static final String SQL_CREATE_TABLE_FOLDERS = "CREATE TABLE " + TABLE_FOLDERS + "("
            + COLUMN_FOLDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_FOLDER_NAME + " TEXT NOT NULL, "
            + COLUMN_FOLDER_LAST_UID + " INTEGER NOT NULL, "
            + COLUMN_FOLDER_HMSEQ + " INTEGER NOT NULL, "
            + COLUMN_FOLDER_MESSAGE_NUMBER + " INTEGER NOT NULL, "
            + COLUMN_FOLDER_NEW_MESSAGE_NUMBER + " INTEGER NOT NULL, "
            + COLUMN_FOLDER_USER_ID + " INTEGER NOT NULL "
            + ");";

    private static final String SQL_CREATE_TABLE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGES + "("
            + COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_MESSAGE_UID + " INTEGER NOT NULL, "
            + COLUMN_MESSAGE_SUBJECT + " TEXT NOT NULL, "
            + COLUMN_MESSAGE_SENT_DATE + " INTEGER NOT NULL, "
            + COLUMN_MESSAGE_SEEN + " INTEGER NOT NULL, "
            + COLUMN_MESSAGE_SHOW_IMAGES + " INTEGER NOT NULL, "
            + COLUMN_MESSAGE_FOLDER_ID + " INTEGER NOT NULL "
            + ");";

    private static final String SQL_CREATE_TABLE_CONTACTS = "CREATE TABLE " + TABLE_CONTACTS + "("
            + COLUMN_CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_CONTACT_EMAIL + " TEXT NOT NULL, "
            + COLUMN_CONTACT_NAME + " TEXT NOT NULL "
            + ");";

    public static final String SQL_CREATE_TABLE_MESSAGECONTACTS = "CREATE TABLE " + TABLE_MESSAGECONTACTS + "("
            + COLUMN_MESSAGECONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_MESSAGECONTACT_MESSAGE_ID + " INTEGER NOT NULL, "
            + COLUMN_MESSAGECONTACT_CONTACT_ID + " INTEGER NOT NULL, "
            + COLUMN_MESSAGECONTACT_TYPE + " INTEGER NOT NULL "
            + ");";

    public static final String SQL_CREATE_TABLE_USERCONTACTS = "CREATE TABLE " + TABLE_USERCONTACTS + "("
            + COLUMN_USERCONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USERCONTACT_USER_ID + " INTEGER NOT NULL, "
            + COLUMN_USERCONTACT_CONTACT_ID + " INTEGER NOT NULL "
            + ");";

    private static final String SQL_CREATE_TABLE_ATTACHMENTS = "CREATE TABLE " + TABLE_ATTACHMENTS + "("
            + COLUMN_ATTACHMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ATTACHMENT_NAME + " TEXT NOT NULL, "
            + COLUMN_ATTACHMENT_FORMAT + " TEXT NOT NULL, "
            + COLUMN_ATTACHMENT_PATH + " TEXT, "
            + COLUMN_ATTACHMENT_MESSAGE_ID + " INTEGER NOT NULL "
            + ");";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_TABLE_USERS);
        database.execSQL(SQL_CREATE_TABLE_FOLDERS);
        database.execSQL(SQL_CREATE_TABLE_MESSAGES);
        database.execSQL(SQL_CREATE_TABLE_CONTACTS);
        database.execSQL(SQL_CREATE_TABLE_MESSAGECONTACTS);
        database.execSQL(SQL_CREATE_TABLE_USERCONTACTS);
        database.execSQL(SQL_CREATE_TABLE_ATTACHMENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG,
                "Upgrading the database from version " + oldVersion + " to " + newVersion);
        //clear all data
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGECONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERCONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTACHMENTS);

        //recreate the tables
        onCreate(db);
    }

    public MySQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }
}
