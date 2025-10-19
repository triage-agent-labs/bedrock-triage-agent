package com.triageagent.bedrocktriageagent.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.triageagent.bedrocktriageagent.model.TriageResult;
import com.triageagent.bedrocktriageagent.service.TriageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

record TriageRequest(String text) {}

@RestController
@RequestMapping("/api")
public class ApiController {

    private final TriageService triage;

    public ApiController(TriageService triage) {
        this.triage = triage;
    }

    @PostMapping("/triage")
    public TriageResult triage(@RequestBody TriageRequest req) throws JsonProcessingException {
        return triage.triage(req.text());
    }
}
