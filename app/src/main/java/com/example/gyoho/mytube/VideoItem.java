package com.example.gyoho.mytube;

/**
 * Created by gyoho on 10/15/15.
 */

// Model for a custom ArrayAdapter
public class VideoItem {
    private String id;
    private String title;
    private String publishedDate;
    private String description;
    private String thumbnailURL;
    private int viewCount;


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

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnail) {
        this.thumbnailURL = thumbnail;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void incrementViewCount() {
        viewCount++;
    }

}