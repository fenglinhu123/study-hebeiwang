package com.mhyy.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController()
public class TestController {

    @Autowired
    private RestTemplate outerRestTemplate;

    @RequestMapping("/test")
    public String test() {
        return "Hello world!";
    }
}
