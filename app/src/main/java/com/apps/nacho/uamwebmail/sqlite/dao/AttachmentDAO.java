package com.apps.nacho.uamwebmail.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.apps.nacho.uamwebmail.sqlite.model.Attachment;
import com.apps.nacho.uamwebmail.sqlite.model.MySQLiteHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nacho on 12/10/16.
 */

public class AttachmentDAO {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private Context mContext;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ATTACHMENT_ID,
            MySQLiteHelper.COLUMN_ATTACHMENT_NAME,
            MySQLiteHelper.COLUMN_ATTACHMENT_FORMAT,
            MySQLiteHelper.COLUMN_ATTACHMENT_PATH,
            MySQLiteHelper.COLUMN_ATTACHMENT_MESSAGE_ID};

    public AttachmentDAO(Context context) {
        this.mContext = context;
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Attachment createAttachment(String name, String format, long msg_id) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_ATTACHMENT_NAME, name);
        values.put(MySQLiteHelper.COLUMN_ATTACHMENT_FORMAT, format);
        values.put(MySQLiteHelper.COLUMN_ATTACHMENT_PATH, "");
        values.put(MySQLiteHelper.COLUMN_ATTACHMENT_MESSAGE_ID, msg_id);
        long insertId = database.insert(MySQLiteHelper.TABLE_ATTACHMENTS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_ATTACHMENTS,
                allColumns, MySQLiteHelper.COLUMN_ATTACHMENT_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Attachment newAttachment = cursorToAttachment(cursor);
        cursor.close();
        return newAttachment;
    }

    public List<Attachment> getAllAttachments() {
        List<Attachment> listAttachments = new ArrayList<Attachment>();

        Cursor cursor = database.query(dbHelper.TABLE_ATTACHMENTS, allColumns,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Attachment attachment= cursorToAttachment(cursor);
            listAttachments.add(attachment);
            cursor.moveToNext();
        }

        cursor.close();
        return listAttachments;
    }

    public Attachment getAttachmentById(long id) {
        Cursor cursor = database.query(dbHelper.TABLE_ATTACHMENTS, allColumns, dbHelper.COLUMN_ATTACHMENT_ID + " = ?",
                new String[] {String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        Attachment attachment = cursorToAttachment(cursor);
        return attachment;
    }

    public List<Attachment> getAllMessageAttachments(long msg_id) {
        List<Attachment> listAttachments = new ArrayList<Attachment>();
        Cursor cursor = database.query(dbHelper.TABLE_ATTACHMENTS, allColumns, dbHelper.COLUMN_ATTACHMENT_MESSAGE_ID + " = ?",
                new String[] {String.valueOf(msg_id)}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Attachment attachment= cursorToAttachment(cursor);
            listAttachments.add(attachment);
            cursor.moveToNext();
        }

        cursor.close();
        return listAttachments;
    }

    public boolean existsAttachment(String name, String format, long msg_id) {
        Cursor cursor = database.query(dbHelper.TABLE_ATTACHMENTS, allColumns, dbHelper.COLUMN_ATTACHMENT_NAME + " = ? AND "
                + dbHelper.COLUMN_ATTACHMENT_FORMAT + " = ? AND "
                + dbHelper.COLUMN_ATTACHMENT_MESSAGE_ID + " = ?",
                new String[] {name, format, String.valueOf(msg_id)}, null, null, null);
        if (cursor.getCount() == 0) {
            return false;
        }
        return true;
    }

    public void updateAttachment(Attachment attachment) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_ATTACHMENT_PATH, attachment.getPath());

        database.update(dbHelper.TABLE_ATTACHMENTS, values, dbHelper.COLUMN_ATTACHMENT_ID + " = ?", new String[] {String.valueOf(attachment.getId())});
    }

    public void deleteAttachment(long attachmentId) {
        database.delete(dbHelper.TABLE_ATTACHMENTS, dbHelper.COLUMN_ATTACHMENT_ID + " = ?", new String[] {String.valueOf(attachmentId)});
    }

    private Attachment cursorToAttachment(Cursor cursor) {
        Attachment attachment = new Attachment();
        attachment.setId(cursor.getLong(0));
        attachment.setName(cursor.getString(1));
        attachment.setFormat(cursor.getString(2));
        attachment.setPath(cursor.getString(3));
        attachment.setMsgId(cursor.getLong(4));
        return attachment;
    }

}
