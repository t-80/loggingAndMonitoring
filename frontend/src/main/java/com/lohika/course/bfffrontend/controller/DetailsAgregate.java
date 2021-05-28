package com.lohika.course.bfffrontend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1/details")
public class DetailsAgregate {

    private final static Logger logger = LoggerFactory.getLogger(DetailsAgregate.class);

    @Value("${books.url}")
    private String booksUrl;

    @Value("${authors.url}")
    private String authorsUrl;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public Map<String, String> getBooksAndAuthors() {
        logger.info("Get details");

        ResponseEntity<String> authors = restTemplate.getForEntity(
                authorsUrl, String.class );

        ResponseEntity<String> books = restTemplate.getForEntity(
                booksUrl, String.class );

        Map<String, String> result = new HashMap<>();
        result.put("authors", authors.getBody());
        result.put("books", books.getBody());

        return result;
    }
}
