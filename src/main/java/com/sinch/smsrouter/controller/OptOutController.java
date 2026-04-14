package com.sinch.smsrouter.controller;

import com.sinch.smsrouter.service.OptOutService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/optout")
public class OptOutController {

    private final OptOutService optOutService;

    public OptOutController(OptOutService optOutService) {
        this.optOutService = optOutService;
    }

    @PostMapping("/{phoneNumber}")
    public void optOut(@PathVariable String phoneNumber) {
        optOutService.optOut(phoneNumber);
    }
}
