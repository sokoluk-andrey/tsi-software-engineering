package com.example.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hello")
public class HelloController {

    @GetMapping
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Spring Boot работает!");
        response.put("status", "success");
        return response;
    }
}


