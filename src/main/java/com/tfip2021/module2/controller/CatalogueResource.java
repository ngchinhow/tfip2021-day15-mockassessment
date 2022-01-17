package com.tfip2021.module2.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tfip2021.module2.model.Book;
import com.tfip2021.module2.service.CatalogueRedis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static com.tfip2021.module2.model.Constants.*;

@RestController
@RequestMapping(
    path = { "/" },
    produces = { "application/json" }
)
public class CatalogueResource {

    @Autowired
    private CatalogueRedis service;

    @GetMapping("books")
    public ResponseEntity<String> listBooks(
        @RequestParam MultiValueMap<String, String> queryParams
    ) throws JsonProcessingException {
        int pageNumber = isPosInt(queryParams.getFirst("pageNumber")) ? 
            Integer.parseInt(queryParams.getFirst("pageNumber")) :
            DEFAULT_PAGE_NUMBER;
        int pageSize = isPosInt(queryParams.getFirst("pageSize")) ? 
            Integer.parseInt(queryParams.getFirst("pageSize")) :
            DEFAULT_PAGE_SIZE;
        return new ResponseEntity<String> (
            service.listBooks(pageNumber, pageSize).toString(), HttpStatus.OK
        );
    }

    @GetMapping("books/{id}")
    public ResponseEntity<String> getBook(
        @PathVariable(name="id", required=true) String id
    ) {
        return new ResponseEntity<String>(
            service.getBook(id).toString(),
            HttpStatus.OK
        );
    }

    @GetMapping("search")
    public ResponseEntity<String> searchBook(
        @RequestParam(name="q", required=true) String searchStr,
        @RequestParam MultiValueMap<String, String> queryParams
    ) {
        int pageNumber = isPosInt(queryParams.getFirst("pageNumber")) ? 
            Integer.parseInt(queryParams.getFirst("pageNumber")) :
            DEFAULT_PAGE_NUMBER;
        int pageSize = isPosInt(queryParams.getFirst("pageSize")) ? 
            Integer.parseInt(queryParams.getFirst("pageSize")) :
            DEFAULT_PAGE_SIZE;
        if (searchStr == null) {
            return new ResponseEntity<String> (
                service.listBooks(pageNumber, pageSize).toString(),
                HttpStatus.OK
            );
        }
        return new ResponseEntity<String> (
            service.searchBooks(searchStr, pageNumber, pageSize).toString(),
            HttpStatus.OK
        );
    }

    @PostMapping(
        path = { "book" },
        consumes = { "application/json" }
    )
    @ResponseBody
    public ResponseEntity<String> createBook(@RequestBody Book book) {
        service.saveBook(book);
        return new ResponseEntity<String> (
            "created", HttpStatus.CREATED
        );
    }

    @PutMapping(
        path = { "book/{id}" },
        consumes = { "application/json" }
    )
    @ResponseBody
    public ResponseEntity<String> updateBook(
        @PathVariable(name="id", required=true) String id,
        @RequestBody Book book
    ) {
        return new ResponseEntity<>(
            service.updateBook(id, book).toString(),
            HttpStatus.OK
        );
    }

    private static boolean isPosInt(String str) {
        return str != null && str.matches("^[0-9]+");
    }
}
