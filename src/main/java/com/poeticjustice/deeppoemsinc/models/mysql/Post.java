package com.poeticjustice.deeppoemsinc.models.mysql;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    private String content;
    private String userId; // Reference to User IDprivate String status;
    private String author;
    private String status;
    private String imageUrl;
    private int likes;
    private Set<String> likedBy; // Set to store unique userIds
    
    public Post() {
    }

    public Post(String title, String content, String author, String userId, String status, String imageUrl, int likes) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.userId = userId;
        this.status = status;
        this.imageUrl = imageUrl;
        this.likes = likes;
        this.likedBy = new HashSet<>();
    }
    
    public Post(Integer id,String title, String content, String author, String userId, String status, String imageUrl, int likes) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.userId = userId;
        this.status = status;
        this.imageUrl = imageUrl;
        this.likes = likes;
        this.likedBy = new HashSet<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Set<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(Set<String> likedBy) {
        this.likedBy = likedBy;
    }
}
