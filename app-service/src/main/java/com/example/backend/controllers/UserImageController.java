package com.example.backend.controllers;

import java.sql.SQLException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.backend.services.UserImageService;

@Controller
public class UserImageController {

    @Autowired
    private UserImageService userImageService;

    @GetMapping("/user/{id}/image")
    public ResponseEntity<Object> downloadUserImage(@PathVariable long id) throws SQLException {

        Optional<Resource> file = userImageService.getUserImage(id);

        if (file.isPresent()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(file.get());
        }

        return ResponseEntity.notFound().build();
    }
}