package com.cybersec.controller;

import com.cybersec.scanner.PortScanner;
import com.cybersec.scanner.DetectOs;
import org.springframework.web.bind.annotation.*;
import java.util.Map;



@RestController
@RequestMapping("/api/scanner")
public class PortScannerController {

    @GetMapping("/tcp")
    public Map<String, String> scanTCP(@RequestParam String target, @RequestParam int port) {
        return PortScanner.tcpConnectScan(target, port);
    }

    @GetMapping("/syn")
    public Map<String, String> scanSYN(@RequestParam String target, @RequestParam int port) {
        return PortScanner.synScan(target, port);
    }

    @GetMapping("/udp")
    public Map<String, String> scanUDP(@RequestParam String target, @RequestParam int port) {
        return PortScanner.udpScan(target, port);
    }

    @GetMapping("/os")
    public Map<String, String> detectOS(@RequestParam String target) {
        return DetectOs.detectOs(target);
    }
    @GetMapping("/full")
    public Map<String, Object> getMethodName(@RequestParam String target, @RequestParam int startPort, @RequestParam int endPort) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = PortScanner.fullScan(target, startPort, endPort);
        long endTime = System.currentTimeMillis();
        System.out.println("timeTaken "+ (endTime - startTime)/1000 + "s");
        return result;
    }
    
}
