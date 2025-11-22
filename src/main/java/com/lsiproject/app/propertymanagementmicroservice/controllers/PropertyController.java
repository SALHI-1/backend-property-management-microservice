package com.lsiproject.app.propertymanagementmicroservice.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/public")
public class PropertyController {

    @GetMapping
    public ResponseEntity<String> testApi() {
        return ResponseEntity.ok("hello");
    }
}
