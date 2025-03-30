package com.cybersec.controller;

import com.cybersec.smart.SmartReconService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/smart")
public class SmartReconController {

    private final SmartReconService smartService;

    public SmartReconController(SmartReconService smartService) {
        this.smartService = smartService;
    }

    @GetMapping("/recon")
    public String runSmartRecon(
        @RequestParam String target,
        @RequestParam(defaultValue = "20") int startPort,
        @RequestParam(defaultValue = "100") int endPort
    ) {
        return smartService.run(target, startPort, endPort);
    }
}
