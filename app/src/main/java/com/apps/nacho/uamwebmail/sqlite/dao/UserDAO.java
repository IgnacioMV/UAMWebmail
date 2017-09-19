package com.apps.nacho.uamwebmail.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.apps.nacho.uamwebmail.sqlite.model.MySQLiteHelper;
import com.apps.nacho.uamwebmail.sqlite.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nacho on 12/10/16.
 */

public class UserDAO {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private Context mContext;
    private String[] allColumns = { MySQLiteHelper.COLUMN_USER_ID,
            MySQLiteHelper.COLUMN_USER_EMAIL,
            MySQLiteHelper.COLUMN_USER_PASSWORD,
            MySQLiteHelper.COLUMN_USER_ACTIVE };

    public UserDAO(Context context) {
        this.mContext = context;
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public User createUser(String email, String password) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_USER_EMAIL, email);
        values.put(MySQLiteHelper.COLUMN_USER_PASSWORD, password);
        values.put(MySQLiteHelper.COLUMN_USER_ACTIVE, 0);
        long insertId = database.insert(MySQLiteHelper.TABLE_USERS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_USERS,
                allColumns, MySQLiteHelper.COLUMN_USER_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        User newUser = cursorToUser(cursor);
        cursor.close();
        return newUser;
    }

    public List<User> getAllUsers() {
        List<User> listUsers = new ArrayList<User>();

        Cursor cursor = database.query(dbHelper.TABLE_USERS, allColumns,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            User user = cursorToUser(cursor);
            listUsers.add(user);
            cursor.moveToNext();
        }

        cursor.close();
        return listUsers;
    }

    public User getUserByEmail(String email) {
        Cursor cursor = database.query(dbHelper.TABLE_USERS, allColumns, dbHelper.COLUMN_USER_EMAIL+ " = ?",
                new String[] {email}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        User user = cursorToUser(cursor);
        return user;
    }

    public boolean existsUser(String email) {
        Cursor cursor = database.query(dbHelper.TABLE_USERS, allColumns, dbHelper.COLUMN_USER_EMAIL+ " = ?",
                new String[] {email}, null, null, null);
        if (cursor.getCount() == 0) {
            return false;
        }
        return true;
    }

    public User getActiveUser() {
        Cursor cursor = database.query(dbHelper.TABLE_USERS, allColumns, dbHelper.COLUMN_USER_ACTIVE + " = ?",
                new String[] {String.valueOf(1)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        else {
            return null;
        }
        if (cursor.getCount() == 0) {
            return null;
        }

        User user = cursorToUser(cursor);
        return user;
    }

    public void setActiveUser(User user) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_USER_ACTIVE, 0);
        List<User> userList = this.getAllUsers();
        for (User usr:userList) {
            usr.setActive(0);
            this.updateUser(usr);
        }
        this.updateUser(user);
    }

    public void setNoActiveUser() {
        List<User> userList = this.getAllUsers();
        for (User usr:userList) {
            usr.setActive(0);
            this.updateUser(usr);
        }
    }

    public void updateUser(User user) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_USER_EMAIL, user.getEmail());
        values.put(MySQLiteHelper.COLUMN_USER_PASSWORD, user.getPassword());
        values.put(MySQLiteHelper.COLUMN_USER_ACTIVE, user.getActive());

        database.update(dbHelper.TABLE_USERS, values, dbHelper.COLUMN_USER_ID + " = ?", new String[] {String.valueOf(user.getId())});
    }

    public void deleteUser(long userId) {
        database.delete(dbHelper.TABLE_USERS, dbHelper.COLUMN_USER_ID + " = ?", new String[] {String.valueOf(userId)});
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(0));
        user.setEmail(cursor.getString(1));
        user.setPassword(cursor.getString(2));
        user.setActive(cursor.getInt(3));
        return user;
    }

}
