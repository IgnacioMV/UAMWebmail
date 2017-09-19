package com.apps.nacho.uamwebmail.sqlite.model;

/**
 * Created by nacho on 28/10/16.
 */

public class UserContact {
    private long id;
    private long userId;
    private long contactId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }
}
