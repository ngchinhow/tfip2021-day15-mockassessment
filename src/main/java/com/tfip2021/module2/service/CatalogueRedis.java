package com.tfip2021.module2.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Set;

import com.tfip2021.module2.model.Book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

import static com.tfip2021.module2.model.Constants.*;

@Service
public class CatalogueRedis implements CatalogueRepo {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Override
    public JsonObject listBooks(int pageNumber, int pageSize) {
        Set<String> titles = redisTemplate.opsForZSet().range(
            TITLE_SORTED_SET,
            (pageNumber - 1) * pageSize,
            pageNumber * pageSize
        );
        System.out.println(titles);

        // Initialize JSON objects needed
        JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
        JsonArrayBuilder booksBuilder = Json.createArrayBuilder();        
        JsonObjectBuilder paginationBuilder = Json.createObjectBuilder();

        // Loop through to add books to JSON
        for (String title: titles) {
            String bookStr = (String) redisTemplate.opsForHash().get(
                BOOKS_HASH,
                title.split(":")[3]
            );
            try (InputStream is = new ByteArrayInputStream(bookStr.getBytes())) {
                JsonReader reader = Json.createReader(is);
                JsonObject book = reader.readObject();
                booksBuilder.add(book);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Create pagination object
        paginationBuilder.add("pageNumber", pageNumber);
        paginationBuilder.add("size", titles.size());
        paginationBuilder.add("pageSize", pageSize);
        
        // Collate response object
        responseBuilder.add("books", booksBuilder);
        responseBuilder.add("pagination", paginationBuilder);
        return responseBuilder.build();
    }

    @Override
    public JsonObject searchBooks(String searchStr, int pageNumber, int pageSize) {
        // searchStr should be a string literal
        String matchStr = "*:*" + searchStr.toLowerCase() + "*:*";
        System.out.println(matchStr);
        ScanOptions scanOptions = ScanOptions.scanOptions().
            match(matchStr.toLowerCase()).count(pageSize).build();
        // Search through titles
        Cursor<TypedTuple<String>> cTitle = redisTemplate.opsForZSet().
            scan(TITLE_SORTED_SET, scanOptions);
        
        // Initialize JSON objects needed
        JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
        JsonArrayBuilder booksBuilder = Json.createArrayBuilder();        
        JsonObjectBuilder paginationBuilder = Json.createObjectBuilder();

        // Loop through to add books to JSON
        int counter = 0;
        while (cTitle.hasNext() && counter < pageSize) {
            if (cTitle.getPosition() >= (pageNumber - 1) * pageSize) {
                String bookStr = (String) redisTemplate.opsForHash().get(
                    BOOKS_HASH,
                    cTitle.next().getValue().split(":")[3]
                );
                try (InputStream is = new ByteArrayInputStream(bookStr.getBytes())) {
                    JsonReader reader = Json.createReader(is);
                    JsonObject book = reader.readObject();
                    booksBuilder.add(book);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                counter++;
            }
        }
        cTitle.close();

        // Create pagination object
        paginationBuilder.add("pageNumber", pageNumber);
        paginationBuilder.add("size", counter);
        paginationBuilder.add("pageSize", pageSize);
        
        // Collate response object
        responseBuilder.add("books", booksBuilder);
        responseBuilder.add("pagination", paginationBuilder);
        return responseBuilder.build();
    }

    @Override
    public JsonObject getBook(String id) {
        final String bookStr = (String) redisTemplate.opsForHash().get(
            BOOKS_HASH,
            id
        );
        JsonReader reader = Json.createReader(new StringReader(bookStr));
        return reader.readObject();
    }

    @Override
    public void saveBook(final Book book) {
        redisTemplate.opsForZSet().add(
            TITLE_SORTED_SET,
            ":" + book.getTitle().toLowerCase() + ":" +
                book.getAuthor().toLowerCase() + ":" +
                book.getId(),
            1
        );
        JsonObjectBuilder bookBuilder = Json.createObjectBuilder();
        bookBuilder.add("id", book.getId()).
                    add("title", book.getTitle()).
                    add("author", book.getAuthor());
        redisTemplate.opsForHash().put(
            BOOKS_HASH,
            book.getId(),
            bookBuilder.build().toString()
        );
    }

    @Override
    public JsonObject updateBook(final String id, final Book newBook) {
        String oldBookStr = (String) redisTemplate.opsForHash().get(
            BOOKS_HASH,
            id
        );
        JsonReader oldBookReader = Json.createReader(new StringReader(oldBookStr));
        JsonObject oldBookJSON = oldBookReader.readObject();
        // remove old book sorting
        redisTemplate.opsForZSet().remove(
            TITLE_SORTED_SET,
            ":" +  oldBookJSON.getString("title") + ":" +
                   oldBookJSON.getString("author") + ":" +
                   oldBookJSON.getString("id")
        );

        // add new book sorting
        redisTemplate.opsForZSet().add(
            TITLE_SORTED_SET,
            ":" + newBook.getTitle().toLowerCase() + ":" +
                  newBook.getAuthor().toLowerCase() + ":" +
                  id,
            1
        );
        JsonObjectBuilder bookBuilder = Json.createObjectBuilder();
        JsonObject response = bookBuilder.add("id", id).
            add("title", newBook.getTitle()).
            add("author", newBook.getAuthor()).build();
        redisTemplate.opsForHash().put(
            BOOKS_HASH,
            id,
            response.toString()
        );
        return response;
    }
}