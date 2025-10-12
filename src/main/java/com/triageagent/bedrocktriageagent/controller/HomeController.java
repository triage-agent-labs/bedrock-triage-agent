package com.triageagent.bedrocktriageagent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Welcome to Bedrock Triage Agent!");
        return "index"; // looks for src/main/resources/templates/index.html
    }
}
