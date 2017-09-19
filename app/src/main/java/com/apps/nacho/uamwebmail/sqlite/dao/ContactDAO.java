package com.apps.nacho.uamwebmail.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.apps.nacho.uamwebmail.sqlite.model.Contact;
import com.apps.nacho.uamwebmail.sqlite.model.MySQLiteHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nacho on 12/10/16.
 */

public class ContactDAO {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private Context mContext;
    private String[] allColumns = { MySQLiteHelper.COLUMN_CONTACT_ID,
            MySQLiteHelper.COLUMN_CONTACT_EMAIL,
            MySQLiteHelper.COLUMN_CONTACT_NAME,};

    public ContactDAO(Context context) {
        this.mContext = context;
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Contact createContact(String email, String name) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_CONTACT_EMAIL, email);
        values.put(MySQLiteHelper.COLUMN_CONTACT_NAME, name);
        long insertId = database.insert(MySQLiteHelper.TABLE_CONTACTS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS,
                allColumns, MySQLiteHelper.COLUMN_CONTACT_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Contact newContact = cursorToContact(cursor);
        cursor.close();
        return newContact;
    }

    public List<Contact> getAllContacts() {
        List<Contact> listContacts = new ArrayList<Contact>();

        Cursor cursor = database.query(dbHelper.TABLE_CONTACTS, allColumns,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Contact contact= cursorToContact(cursor);
            listContacts.add(contact);
            cursor.moveToNext();
        }

        cursor.close();
        return listContacts;
    }

    public Contact getContactById(long id) {
        Cursor cursor = database.query(dbHelper.TABLE_CONTACTS, allColumns, dbHelper.COLUMN_CONTACT_ID + " = ?",
                new String[] {String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        Contact contact = cursorToContact(cursor);
        return contact;
    }

    public Contact getContactByEmail(String email) {
        Cursor cursor = database.query(dbHelper.TABLE_CONTACTS, allColumns, dbHelper.COLUMN_CONTACT_EMAIL+ " = ?",
                new String[] {email}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        Contact contact = cursorToContact(cursor);
        return contact;
    }

    public boolean existsContact(String email) {
        Cursor cursor = database.query(dbHelper.TABLE_CONTACTS, allColumns, dbHelper.COLUMN_CONTACT_EMAIL + " = ?",
                new String[] {email}, null, null, null);
        if (cursor.getCount() == 0) {
            return false;
        }
        return true;
    }

    public void updateContact(Contact contact) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_CONTACT_NAME, contact.getName());

        database.update(dbHelper.TABLE_CONTACTS, values, dbHelper.COLUMN_CONTACT_ID + " = ?", new String[] {String.valueOf(contact.getId())});
    }

    public void deleteContact(long contactId) {
        database.delete(dbHelper.TABLE_CONTACTS, dbHelper.COLUMN_CONTACT_ID + " = ?", new String[] {String.valueOf(contactId)});
    }

    private Contact cursorToContact(Cursor cursor) {
        Contact contact = new Contact();
        contact.setId(cursor.getLong(0));
        contact.setEmail(cursor.getString(1));
        contact.setName(cursor.getString(2));
        return contact;
    }

}
