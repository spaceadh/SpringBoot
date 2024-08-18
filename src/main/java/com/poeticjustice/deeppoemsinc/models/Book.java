package com.poeticjustice.deeppoemsinc.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.validator.constraints.NotBlank;

@Entity
public class Book {
    @Id
    // @GeneratedValue(strategy = GenerationType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank
    private String title;

    @NotBlank
    private String author;
    private String pdfUrl;
    private String imageUrl;
    private int userId;

    public Book() {  }

    public Book(String title, String author, String pdfUrl, String imageUrl,int userId) {
        this.setTitle(title);
        this.setContent(author);
        this.setPdfUrl(pdfUrl);
        this.setImageUrl(imageUrl);
        this.setUserId(userId);
    }

    public Book(int Id,String title, String author, String pdfUrl, String imageUrl,int userId) {
        this.setId(id);
        this.setTitle(title);
        this.setContent(author);
        this.setPdfUrl(pdfUrl);
        this.setImageUrl(imageUrl);
        this.setUserId(userId);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getTitle() {
        return title;
    }
   
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
   
    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
    
    public String getContent() {
        return author;
    }

    public void setContent(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + "'" +
                ", author='" + author + "'" +
                '}';
    }
}