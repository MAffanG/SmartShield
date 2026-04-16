package com.example.SmartShield.controller;

import com.example.SmartShield.RateLimited;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RateLimited(maxRequests = 5,windowMs = 60000)
    @GetMapping("/test")
    public String test(){
        System.out.println("Controller Hit!");
        return "API Works";
    }

    @RateLimited(maxRequests = 10,windowMs = 120000)
    @GetMapping("/login")
    public String login(){
        System.out.println("Login hit!");
        return "Login Page";
    }
}
