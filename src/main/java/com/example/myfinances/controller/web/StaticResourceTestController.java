package com.example.myfinances.controller.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;

@Controller
public class StaticResourceTestController {

    @GetMapping("/test-css/{filename}")
    public ResponseEntity<String> testCss(@PathVariable String filename) throws IOException {
        Resource resource = new ClassPathResource("static/css/" + filename);
        if (resource.exists()) {
            String content = Files.readString(resource.getFile().toPath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("text/css"));
            return ResponseEntity.ok().headers(headers).body(content);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/test-js/{filename}")
    public ResponseEntity<String> testJs(@PathVariable String filename) throws IOException {
        Resource resource = new ClassPathResource("static/js/" + filename);
        if (resource.exists()) {
            String content = Files.readString(resource.getFile().toPath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/javascript"));
            return ResponseEntity.ok().headers(headers).body(content);
        }
        return ResponseEntity.notFound().build();
    }
}