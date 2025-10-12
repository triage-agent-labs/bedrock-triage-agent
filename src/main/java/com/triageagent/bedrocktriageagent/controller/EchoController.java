package com.triageagent.bedrocktriageagent.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EchoController {

    public record Echo(String text) {}

    @PostMapping("/echo")
    public Echo echo(@RequestBody Echo body) {
        return body;
    }
}
