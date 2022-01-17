package com.tfip2021.module2.model;

import java.io.Serializable;
import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class Book implements Serializable {
    private static final int ID_LENGTH = 8;
    private String id;
    private String title;
    private String author;
    private String thumbnail;

    public Book() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < ID_LENGTH){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        this.id = sb.toString().substring(0, ID_LENGTH);
    }

    public Book(String id) {
        this.id = id;
    }

    public Book(String title, String author, String thumbnail) {
        this();
        this.title = title;
        this.author = author;
    }

    public Book(String id, String title, String author, String thumbnail) {
        this.id = id;
        this.title = title;
        this.author = author;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getThumbNail() { return thumbnail; }
    
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
}
