package com.triageagent.bedrocktriageagent.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.triageagent.bedrocktriageagent.model.TriageResult;
import com.triageagent.bedrocktriageagent.service.TriageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    private final TriageService triage;

    public HomeController(TriageService triage) {
        this.triage = triage;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/triage")
    public String submit(@RequestParam("text") String text, Model model) throws JsonProcessingException {
        TriageResult result = triage.triage(text);
        model.addAttribute("result", result);
        return "index";
    }
}
