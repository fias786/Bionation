package com.persistent.bionation.data;

public class NewsArticlesData {

    String urlToImage,url,content,author,description,publishedAt,title;

    public NewsArticlesData(String urlToImage, String url, String content, String author, String description, String publishedAt, String title) {
        this.urlToImage = urlToImage;
        this.url = url;
        this.content = content;
        this.author = author;
        this.description = description;
        this.publishedAt = publishedAt;
        this.title = title;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
