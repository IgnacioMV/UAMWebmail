package com.apps.nacho.uamwebmail.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.apps.nacho.uamwebmail.sqlite.model.Folder;
import com.apps.nacho.uamwebmail.sqlite.model.MySQLiteHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nacho on 12/10/16.
 */

public class FolderDAO {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private Context mContext;
    private String[] allColumns = { MySQLiteHelper.COLUMN_FOLDER_ID,
            MySQLiteHelper.COLUMN_FOLDER_NAME,
            MySQLiteHelper.COLUMN_FOLDER_LAST_UID,
            MySQLiteHelper.COLUMN_FOLDER_HMSEQ,
            MySQLiteHelper.COLUMN_FOLDER_MESSAGE_NUMBER,
            MySQLiteHelper.COLUMN_FOLDER_NEW_MESSAGE_NUMBER,
            MySQLiteHelper.COLUMN_FOLDER_USER_ID };

    public FolderDAO(Context context) {
        this.mContext = context;
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Folder createFolder(String name, long lastUid, long hmseq, int messageNumber, int newMessageNumber, long userId) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_FOLDER_NAME, name);
        values.put(MySQLiteHelper.COLUMN_FOLDER_LAST_UID, lastUid);
        values.put(MySQLiteHelper.COLUMN_FOLDER_HMSEQ, hmseq);
        values.put(MySQLiteHelper.COLUMN_FOLDER_MESSAGE_NUMBER, messageNumber);
        values.put(MySQLiteHelper.COLUMN_FOLDER_NEW_MESSAGE_NUMBER, newMessageNumber);
        values.put(MySQLiteHelper.COLUMN_FOLDER_USER_ID, userId);
        long insertId = database.insert(MySQLiteHelper.TABLE_FOLDERS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_FOLDERS,
                allColumns, MySQLiteHelper.COLUMN_FOLDER_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Folder newFolder = cursorToFolder(cursor);
        cursor.close();
        return newFolder;
    }

    public List<Folder> getAllFolders() {
        List<Folder> listFolders = new ArrayList<Folder>();

        Cursor cursor = database.query(dbHelper.TABLE_FOLDERS, allColumns,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Folder folder = cursorToFolder(cursor);
            listFolders.add(folder);
            cursor.moveToNext();
        }

        cursor.close();
        return listFolders;
    }

    public List<Folder> getAllUserFolders(long userId) {
        List<Folder> listFolders = new ArrayList<Folder>();

        Cursor cursor = database.query(dbHelper.TABLE_FOLDERS, allColumns,
                dbHelper.COLUMN_FOLDER_USER_ID + " = ?", new String[] {String.valueOf(userId)}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Folder folder = cursorToFolder(cursor);
            listFolders.add(folder);
            cursor.moveToNext();
        }

        cursor.close();
        return listFolders;
    }

    public Folder getFolderById(long folderId) {
        Cursor cursor = database.query(dbHelper.TABLE_FOLDERS, allColumns, dbHelper.COLUMN_FOLDER_ID + " = ?",
                new String[] {String.valueOf(folderId)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        Folder folder = cursorToFolder(cursor);
        return folder;
    }

    public Folder getUserFolderByName(String name, long userId) {
        Cursor cursor = database.query(dbHelper.TABLE_FOLDERS, allColumns, dbHelper.COLUMN_FOLDER_NAME + " = ? AND "
                + dbHelper.COLUMN_FOLDER_USER_ID + " = ?",
                new String[] {name, String.valueOf(userId)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        Folder folder = cursorToFolder(cursor);
        return folder;
    }

    public boolean existsFolder(String name, long userId) {
        Cursor cursor = database.query(dbHelper.TABLE_FOLDERS, allColumns, dbHelper.COLUMN_FOLDER_NAME+ " = ? AND "
                + dbHelper.COLUMN_FOLDER_USER_ID + " = ?",
                new String[] {name, String.valueOf(userId)}, null, null, null);
        if (cursor.getCount() == 0) {
            return false;
        }
        return true;
    }

    public void updateFolder(Folder folder) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_FOLDER_LAST_UID, folder.getLastUID());
        values.put(MySQLiteHelper.COLUMN_FOLDER_HMSEQ, folder.getHmseq());
        values.put(MySQLiteHelper.COLUMN_FOLDER_MESSAGE_NUMBER, folder.getMessageNumber());
        values.put(MySQLiteHelper.COLUMN_FOLDER_NEW_MESSAGE_NUMBER, folder.getNewMessages());

        database.update(dbHelper.TABLE_FOLDERS, values, dbHelper.COLUMN_FOLDER_ID + " = ?", new String[] {String.valueOf(folder.getId())});
    }

    public void deleteFolder(long folderId) {
        database.delete(dbHelper.TABLE_FOLDERS, dbHelper.COLUMN_FOLDER_ID + " = ?", new String[] {String.valueOf(folderId)});
    }

    private Folder cursorToFolder(Cursor cursor) {
        Folder folder = new Folder();
        folder.setId(cursor.getLong(0));
        folder.setName(cursor.getString(1));
        folder.setLastUID(cursor.getLong(2));
        folder.setHmseq(cursor.getLong(3));
        folder.setMessageNumber(cursor.getInt(4));
        folder.setNewMessages(cursor.getInt((5)));
        folder.setUserId(cursor.getInt(6));
        return folder;
    }

}
