package com.example.SmartShield.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test(){
        System.out.println("Controller Hit!");
        return "API Works";
    }

    @GetMapping("/login")
    public String login(){
        System.out.println("Login hit!");
        return "Login Page";
    }
}
