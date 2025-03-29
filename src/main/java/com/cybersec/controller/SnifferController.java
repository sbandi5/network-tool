package com.cybersec.controller;

import com.cybersec.sniffer.RawSocketSniffer;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sniffer")
public class SnifferController {

    private final RawSocketSniffer sniffer;

    public SnifferController(RawSocketSniffer sniffer) {
        this.sniffer = sniffer;
    }

    @PostMapping("/start")
    public String startSniffing(@RequestParam(required = false) Integer port) {
        int listenPort = (port == null) ? -1 : port;

        if (listenPort == -1) {
            sniffer.startPythonSniffing();
            return "Sniffer started on all interfaces.";
        } else {
            sniffer.startJavaSniffing(listenPort);
            return "Sniffer started on port " + listenPort + ".";
        }
    }

    @PostMapping("/stop")
    public String stopSniffing() {
        sniffer.stopSniffing();
        return "Sniffing stopped.";
    }

    @GetMapping("/packets")
    public List<Map<String, String>> getPackets(@RequestParam(required = false) String protocol) {
        List<Map<String, String>> allPackets = sniffer.getPackets();
        if (protocol == null || protocol.isEmpty()) {
            return allPackets;
        }

        return allPackets.stream()
                .filter(p -> protocol.equalsIgnoreCase(p.getOrDefault("protocol", "")))
                .toList();
    }

}
