package com.apps.nacho.uamwebmail.sqlite.model;

/**
 * Created by nacho on 12/10/16.
 */

public class Folder {

    private long id;
    private String name;
    private long lastUID;
    private long hmseq;
    private int messageNumber;
    private int newMessages;
    private long userId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastUID() {
        return lastUID;
    }

    public void setLastUID(long lastUID) {
        this.lastUID = lastUID;
    }

    public long getHmseq() {
        return hmseq;
    }

    public void setHmseq(long hmseq) {
        this.hmseq = hmseq;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }

    public int getNewMessages() {
        return newMessages;
    }

    public void setNewMessages(int newMessages) {
        this.newMessages = newMessages;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
