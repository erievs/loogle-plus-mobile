package com.ksportalcraft.pleasegod;

public class NotificationModel {
    private int post_id;
    private String sender;
    private String content;

    public NotificationModel(int post_id, String sender, String content) {
        this.post_id = post_id;
        this.sender = sender;
        this.content = content;
    }

    public int getPostId() {
        return post_id;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }
}
