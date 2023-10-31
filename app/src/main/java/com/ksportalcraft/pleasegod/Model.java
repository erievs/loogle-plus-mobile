package com.ksportalcraft.pleasegod;

public class Model {
    private String id;
    private String title;
    private String body;
    private String imageUrl; // New field for the image URL

    public Model(String id, String title, String body, String imageUrl) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
