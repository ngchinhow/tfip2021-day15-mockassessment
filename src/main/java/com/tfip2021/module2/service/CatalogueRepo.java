package com.tfip2021.module2.service;

import com.tfip2021.module2.model.Book;

import jakarta.json.JsonObject;

public interface CatalogueRepo {
    public JsonObject listBooks(int pageNumber, int pageSize);
    public JsonObject searchBooks(String searchStr, int pageNumber, int pageSize);
    public JsonObject getBook(String id);
    public void saveBook(final Book book);
    public JsonObject updateBook(final String id, final Book book);
}
