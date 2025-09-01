package com.hsbc.transactionmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/swagger-ui.html";
    }

    @GetMapping("/index")
    public String index() {
        return "redirect:/swagger-ui.html";
    }

    @GetMapping("/home")
    public String homePage() {
        return "redirect:/swagger-ui.html";
    }
}