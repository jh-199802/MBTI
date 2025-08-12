package com.example.mbtitest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    
    @GetMapping("/testest")
    public String testPage() {
        return "redirect:/awesome-test.html";
    }
}