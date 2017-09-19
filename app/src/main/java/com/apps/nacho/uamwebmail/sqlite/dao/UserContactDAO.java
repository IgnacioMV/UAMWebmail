package com.apps.nacho.uamwebmail.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.apps.nacho.uamwebmail.sqlite.model.MySQLiteHelper;
import com.apps.nacho.uamwebmail.sqlite.model.UserContact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nacho on 12/10/16.
 */

public class UserContactDAO {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private Context mContext;
    private String[] allColumns = { MySQLiteHelper.COLUMN_USERCONTACT_ID,
            MySQLiteHelper.COLUMN_USERCONTACT_USER_ID,
            MySQLiteHelper.COLUMN_USERCONTACT_CONTACT_ID };

    public UserContactDAO(Context context) {
        this.mContext = context;
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public UserContact createUserContact(long userId, long contactId) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_USERCONTACT_USER_ID, userId);
        values.put(MySQLiteHelper.COLUMN_USERCONTACT_CONTACT_ID, contactId);
        long insertId = database.insert(MySQLiteHelper.TABLE_USERCONTACTS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_USERCONTACTS,
                allColumns, MySQLiteHelper.COLUMN_USERCONTACT_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        UserContact newUserContact = cursorToUserContact(cursor);
        cursor.close();
        return newUserContact;
    }

    public List<UserContact> getAllContacts() {
        List<UserContact> listUserContacts = new ArrayList<UserContact>();

        Cursor cursor = database.query(dbHelper.TABLE_USERCONTACTS, allColumns,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            UserContact userContact= cursorToUserContact(cursor);
            listUserContacts.add(userContact);
            cursor.moveToNext();
        }

        cursor.close();
        return listUserContacts;
    }

    public List<UserContact> getUserContactsByUserId(long userId) {
        List<UserContact> listUserContacts = new ArrayList<UserContact>();

        Cursor cursor = database.query(dbHelper.TABLE_USERCONTACTS, allColumns, dbHelper.COLUMN_USERCONTACT_USER_ID+ " = ?",
                new String[] {String.valueOf(userId)}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            UserContact userContact= cursorToUserContact(cursor);
            listUserContacts.add(userContact);
            cursor.moveToNext();
        }

        cursor.close();
        return listUserContacts;
    }

    public List<UserContact> getUserContactsByContactId(long contactId) {
        List<UserContact> listUserContacts = new ArrayList<UserContact>();

        Cursor cursor = database.query(dbHelper.TABLE_USERCONTACTS, allColumns, dbHelper.COLUMN_USERCONTACT_CONTACT_ID+ " = ?",
                new String[] {String.valueOf(contactId)}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            UserContact userContact= cursorToUserContact(cursor);
            listUserContacts.add(userContact);
            cursor.moveToNext();
        }

        cursor.close();
        return listUserContacts;
    }

    public boolean existsUserContact(long userId, long contactId) {
        Cursor cursor = database.query(dbHelper.TABLE_CONTACTS, allColumns, dbHelper.COLUMN_USERCONTACT_USER_ID + " = ?"
                        + " AND " + dbHelper.COLUMN_USERCONTACT_CONTACT_ID + " = ?",
                new String[] {String.valueOf(userId), String.valueOf(contactId)}, null, null, null);
        if (cursor.getCount() == 0) {
            return false;
        }
        return true;
    }

    public void deleteUserContact(long userContactId) {
        database.delete(dbHelper.TABLE_USERCONTACTS, dbHelper.COLUMN_USERCONTACT_ID + " = ?", new String[] {String.valueOf(userContactId)});
    }

    private UserContact cursorToUserContact(Cursor cursor) {
        UserContact userContact = new UserContact();
        userContact.setId(cursor.getLong(0));
        userContact.setUserId(cursor.getLong(1));
        userContact.setContactId(cursor.getLong(2));
        return userContact;
    }

}
