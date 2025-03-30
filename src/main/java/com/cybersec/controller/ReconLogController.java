package com.cybersec.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recon/logs")
public class ReconLogController {

    @GetMapping
    public List<String> listLogs() {
        File dir = new File("logs");
        if (!dir.exists()) return List.of();

        return Arrays.stream(dir.listFiles())
                     .filter(f -> f.getName().endsWith(".txt"))
                     .map(File::getName)
                     .collect(Collectors.toList());
    }

    @GetMapping("/{filename}")
    public Resource download(@PathVariable String filename) throws MalformedURLException {
        File file = new File("logs", filename);
        return new UrlResource(file.toURI());
    }
}
