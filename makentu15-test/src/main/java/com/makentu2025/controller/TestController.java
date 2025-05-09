package com.makentu2025.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.makentu2025.result.Result;
import com.makentu2025.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    @Autowired
    TestService testService;

    @GetMapping
    public Result test() {
        return Result.success("success");
    }
}
