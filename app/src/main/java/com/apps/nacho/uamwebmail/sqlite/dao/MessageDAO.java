package com.apps.nacho.uamwebmail.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.apps.nacho.uamwebmail.sqlite.model.MyMessage;
import com.apps.nacho.uamwebmail.sqlite.model.MySQLiteHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nacho on 12/10/16.
 */

public class MessageDAO {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private Context mContext;
    private String[] allColumns = { MySQLiteHelper.COLUMN_MESSAGE_ID,
            MySQLiteHelper.COLUMN_MESSAGE_UID,
            MySQLiteHelper.COLUMN_MESSAGE_SUBJECT,
            MySQLiteHelper.COLUMN_MESSAGE_SENT_DATE,
            MySQLiteHelper.COLUMN_MESSAGE_SEEN,
            MySQLiteHelper.COLUMN_MESSAGE_SHOW_IMAGES,
            MySQLiteHelper.COLUMN_MESSAGE_FOLDER_ID };

    public MessageDAO(Context context) {
        this.mContext = context;
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public MyMessage createMessage(long uid, String subject, long sentDate, int seen, int showImages, long folderId) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_MESSAGE_UID, uid);
        values.put(MySQLiteHelper.COLUMN_MESSAGE_SUBJECT, subject);
        values.put(MySQLiteHelper.COLUMN_MESSAGE_SENT_DATE, sentDate);
        values.put(MySQLiteHelper.COLUMN_MESSAGE_SEEN, seen);
        values.put(MySQLiteHelper.COLUMN_MESSAGE_SHOW_IMAGES, showImages);
        values.put(MySQLiteHelper.COLUMN_MESSAGE_FOLDER_ID, folderId);
        long insertId = database.insert(MySQLiteHelper.TABLE_MESSAGES, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_MESSAGES,
                allColumns, MySQLiteHelper.COLUMN_MESSAGE_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        MyMessage newMessage = cursorToMessage(cursor);
        cursor.close();
        return newMessage;
    }

    public List<MyMessage> getAllMessages() {
        List<MyMessage> listMessages = new ArrayList<MyMessage>();

        Cursor cursor = database.query(dbHelper.TABLE_MESSAGES, allColumns,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MyMessage message = cursorToMessage(cursor);
            listMessages.add(message);
            cursor.moveToNext();
        }

        cursor.close();
        return listMessages;
    }

    public List<MyMessage> getAllFolderMessages(long folderId) {
        List<MyMessage> listMessages = new ArrayList<MyMessage>();

        Cursor cursor = database.query(dbHelper.TABLE_MESSAGES, allColumns,
                dbHelper.COLUMN_MESSAGE_FOLDER_ID + " = ?", new String[] {String.valueOf(folderId)}, null, null, dbHelper.COLUMN_MESSAGE_SENT_DATE+" DESC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MyMessage message = cursorToMessage(cursor);
            listMessages.add(message);
            cursor.moveToNext();
        }

        cursor.close();
        return listMessages;
    }

    public MyMessage getMessageById(long msgId) {
        Cursor cursor = database.query(dbHelper.TABLE_MESSAGES, allColumns, dbHelper.COLUMN_MESSAGE_ID + " = ?",
                new String[] {String.valueOf(msgId)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        MyMessage message = cursorToMessage(cursor);
        return message;
    }

    public long getMinimumUIDByFolder(long folderId) {

        Cursor cursor = database.rawQuery("SELECT MIN("+dbHelper.COLUMN_MESSAGE_UID+") FROM "+dbHelper.TABLE_MESSAGES+" WHERE "+dbHelper.COLUMN_MESSAGE_FOLDER_ID+"=?", new String[] {String.valueOf(folderId)});

        if (cursor != null) {
            cursor.moveToFirst();
        }

        long minUID = cursor.getLong(0);
        return minUID;

    }

    public long getMaximumUIDByFolder(long folderId) {


        Cursor cursor = database.rawQuery("SELECT MAX("+dbHelper.COLUMN_MESSAGE_UID+") FROM "+dbHelper.TABLE_MESSAGES+" WHERE "+dbHelper.COLUMN_MESSAGE_FOLDER_ID+"=?", new String[] {String.valueOf(folderId)});
        if (cursor != null) {
            cursor.moveToFirst();
        }

        long minUID = cursor.getLong(0);
        return minUID;

    }

    public boolean existsMessage(long uid, long folderId) {
        Cursor cursor = database.query(dbHelper.TABLE_MESSAGES, allColumns, dbHelper.COLUMN_MESSAGE_UID+ " = ? AND "
                        + dbHelper.COLUMN_MESSAGE_FOLDER_ID + " = ?",
                new String[] {String.valueOf(uid), String.valueOf(folderId)}, null, null, null);
        if (cursor.getCount() == 0) {
            return false;
        }
        return true;
    }

    public void updateMessage(MyMessage message) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_MESSAGE_SEEN, message.getSeen());
        values.put(MySQLiteHelper.COLUMN_MESSAGE_SHOW_IMAGES, message.getShowImages());
        database.update(dbHelper.TABLE_MESSAGES, values, dbHelper.COLUMN_MESSAGE_ID + " = ?", new String[] {String.valueOf(message.getId())});
    }

    public void deleteMessage(long messageId) {
        database.delete(dbHelper.TABLE_MESSAGES, dbHelper.COLUMN_MESSAGE_ID + " = ?", new String[] {String.valueOf(messageId)});
    }

    private MyMessage cursorToMessage(Cursor cursor) {
        MyMessage message = new MyMessage();
        message.setId(cursor.getLong(0));
        message.setUid(cursor.getLong(1));
        message.setSubject(cursor.getString(2));
        message.setSentDate(cursor.getLong(3));
        message.setSeen(cursor.getInt(4));
        message.setShowImages(cursor.getInt(5));
        message.setFolderId(cursor.getLong(6));
        return message;
    }

}
