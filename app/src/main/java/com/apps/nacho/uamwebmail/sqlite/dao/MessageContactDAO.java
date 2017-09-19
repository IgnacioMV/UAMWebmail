package com.apps.nacho.uamwebmail.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.apps.nacho.uamwebmail.sqlite.model.MessageContact;
import com.apps.nacho.uamwebmail.sqlite.model.MySQLiteHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nacho on 12/10/16.
 */

public class MessageContactDAO {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private Context mContext;
    private String[] allColumns = { MySQLiteHelper.COLUMN_MESSAGECONTACT_ID,
            MySQLiteHelper.COLUMN_MESSAGECONTACT_MESSAGE_ID,
            MySQLiteHelper.COLUMN_MESSAGECONTACT_CONTACT_ID,
            MySQLiteHelper.COLUMN_MESSAGECONTACT_TYPE};

    public MessageContactDAO(Context context) {
        this.mContext = context;
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public MessageContact createMessageContact(long messageId, long contactId, int type) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_MESSAGECONTACT_MESSAGE_ID, messageId);
        values.put(MySQLiteHelper.COLUMN_MESSAGECONTACT_CONTACT_ID, contactId);
        values.put(MySQLiteHelper.COLUMN_MESSAGECONTACT_TYPE, type);
        long insertId = database.insert(MySQLiteHelper.TABLE_MESSAGECONTACTS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_MESSAGECONTACTS,
                allColumns, MySQLiteHelper.COLUMN_MESSAGECONTACT_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        MessageContact newMessageContact = cursorToMessageContact(cursor);
        cursor.close();
        return newMessageContact;
    }

    public List<MessageContact> getAllMessageContacts() {
        List<MessageContact> listMessageContacts = new ArrayList<MessageContact>();

        Cursor cursor = database.query(dbHelper.TABLE_MESSAGECONTACTS, allColumns,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MessageContact messageContact= cursorToMessageContact(cursor);
            listMessageContacts.add(messageContact);
            cursor.moveToNext();
        }

        cursor.close();
        return listMessageContacts;
    }

    public List<MessageContact> getMessageContactsByMessageId(long messageId) {
        List<MessageContact> listMessageContacts = new ArrayList<MessageContact>();

        Cursor cursor = database.query(dbHelper.TABLE_MESSAGECONTACTS, allColumns, dbHelper.COLUMN_MESSAGECONTACT_MESSAGE_ID+ " = ?",
                new String[] {String.valueOf(messageId)}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MessageContact messageContact= cursorToMessageContact(cursor);
            listMessageContacts.add(messageContact);
            cursor.moveToNext();
        }

        cursor.close();
        return listMessageContacts;
    }

    public List<MessageContact> getMessageContactsByContactId(long contactId) {
        List<MessageContact> listMessageContacts = new ArrayList<MessageContact>();

        Cursor cursor = database.query(dbHelper.TABLE_MESSAGECONTACTS, allColumns, dbHelper.COLUMN_MESSAGECONTACT_CONTACT_ID+ " = ?",
                new String[] {String.valueOf(contactId)}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MessageContact messageContact= cursorToMessageContact(cursor);
            listMessageContacts.add(messageContact);
            cursor.moveToNext();
        }

        cursor.close();
        return listMessageContacts;
    }

    public boolean existsMessageContact(long messageId, long contactId, long type) {
        Cursor cursor = database.query(dbHelper.TABLE_MESSAGECONTACTS, allColumns, dbHelper.COLUMN_MESSAGECONTACT_MESSAGE_ID + " = ?"
                + " AND " + dbHelper.COLUMN_MESSAGECONTACT_CONTACT_ID + " = ?"
                + " AND " + dbHelper.COLUMN_MESSAGECONTACT_TYPE + " = ?",
                new String[] {String.valueOf(messageId), String.valueOf(contactId), String.valueOf(type)}, null, null, null);
        if (cursor.getCount() == 0) {
            return false;
        }
        return true;
    }

    public void deleteMessageContact(long mesageContactId) {
        database.delete(dbHelper.TABLE_MESSAGECONTACTS, dbHelper.COLUMN_MESSAGECONTACT_ID + " = ?", new String[] {String.valueOf(mesageContactId)});
    }

    private MessageContact cursorToMessageContact(Cursor cursor) {
        MessageContact messageContact = new MessageContact();
        messageContact.setId(cursor.getLong(0));
        messageContact.setMessageId(cursor.getLong(1));
        messageContact.setContactId(cursor.getLong(2));
        messageContact.setType(cursor.getInt(3));
        return messageContact;
    }

}
