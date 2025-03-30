package com.cybersec.smart;

import com.cybersec.scanner.PortScanner;
import com.cybersec.exploit.BruteForceSimulator;
import com.cybersec.sniffer.RawSocketSniffer;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SmartReconService {

    private final RawSocketSniffer sniffer;

    public SmartReconService(RawSocketSniffer sniffer) {
        this.sniffer = sniffer;
    }

    public String run(String target, int startPort, int endPort) {
        StringBuilder log = new StringBuilder();
        log.append("Starting Smart Reconnaissance\n");
        log.append("Target: ").append(target).append("\n");
        log.append("Scanning ports ").append(startPort).append(" to ").append(endPort).append("\n\n");

        List<Map<String, String>> openPorts = new ArrayList<>();

        for (int port = startPort; port <= endPort; port++) {
            Map<String, String> result = PortScanner.tcpConnectScan(target, port);
            if ("open".equals(result.get("status"))) {
                openPorts.add(result);
                log.append("Port ").append(port)
                        .append(" is OPEN, service: ").append(result.get("service")).append("\n");
            }
        }

        if (openPorts.isEmpty()) {
            log.append("\nNo open ports found.\n");
            return log.toString();
        }

        log.append("\nAttempting Banner Grabbing...\n");

        for (Map<String, String> portInfo : openPorts) {
            int port = Integer.parseInt(portInfo.get("port"));
            Map<String, String> bannerMap = PortScanner.grabBanner(target, port);
            String banner = bannerMap.getOrDefault("service", "Unknown");
            log.append("Port ").append(port).append(": ").append(banner).append("\n");

            Optional<String> match = matchVulnerability(banner);
            match.ifPresent(vuln -> log.append("VULNERABLE: ").append(vuln).append("\n"));

            if (port == 21 || port == 22) {
                log.append("Running brute force simulation on port ").append(port).append("...\n");
                BruteForceSimulator simulator = new BruteForceSimulator();
                Map<String, String> bruteResult = simulator.bruteForce(target, port);

                log.append("Brute Force Result:\n");
                log.append("Status: ").append(bruteResult.get("status")).append("\n");

                if ("success".equals(bruteResult.get("status"))) {
                    log.append("Username: ").append(bruteResult.get("username")).append("\n");
                    log.append("Password: ").append(bruteResult.get("password")).append("\n");
                }
            }
        }

        try {
            log.append("\nStarting packet sniffer...\n");
            sniffer.startPythonSniffing(); // sniff on all ports
            Thread.sleep(10000);
            sniffer.stopSniffing();
            log.append("Stopped packet sniffer\n");

            List<Map<String, String>> packets = sniffer.getPackets();
            log.append("\nCaptured Packets:\n");
            for (Map<String, String> pkt : packets) {
                log.append(pkt.toString()).append("\n");
            }
        } catch (Exception e) {
            log.append("Sniffer Error: ").append(e.getMessage()).append("\n");
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String filename = "recon-" + target.replaceAll("\\.", "_") + "-" + timestamp + ".txt";
        String path = "logs/" + filename;

        try {
            java.io.File dir = new java.io.File("logs");
            if (!dir.exists() && !dir.mkdir()) {
                throw new RuntimeException("Failed to create logs directory");
            }

            Path filePath = Paths.get(path);
            Files.write(filePath, log.toString().getBytes());
            log.append("\nSaved recon log to file: ").append(filename).append("\n");
        } catch (Exception e) {
            log.append("\nError saving recon log: ").append(e.getMessage()).append("\n");
        }

        log.append("\nSmart Recon complete.");
        return log.toString();
    }

    private Optional<String> matchVulnerability(String banner) {
        Map<String, String> vulnMap = Map.of(
                "vsFTPd 2.3.4", "Backdoor vulnerability in vsFTPd 2.3.4",
                "OpenSSH 7.2p2", "Credential leak in OpenSSH 7.2p2",
                "Apache/2.2.8", "Remote code execution in Apache 2.2.8",
                "MySQL 5.5", "Old MySQL vulnerable to auth bypass");

        return vulnMap.entrySet().stream()
                .filter(entry -> banner.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
    }

}
