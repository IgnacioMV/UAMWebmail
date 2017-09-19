package com.apps.nacho.uamwebmail.sqlite.model;

/**
 * Created by nacho on 12/10/16.
 */

public class MyMessage {

    private long id;
    private long uid;
    private String subject;
    private long sentDate;
    private int seen;
    private int showImages;
    private long folderId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public long getSentDate() {
        return sentDate;
    }

    public void setSentDate(long sentDate) {
        this.sentDate = sentDate;
    }

    public int getSeen() {
        return seen;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public int getShowImages() { return showImages; }

    public void setShowImages(int showImages) { this.showImages = showImages; }

    public long getFolderId() {
        return folderId;
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }
}
