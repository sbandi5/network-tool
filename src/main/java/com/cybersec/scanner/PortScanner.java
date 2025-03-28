package com.cybersec.scanner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PortScanner {

    private static final int TIMEOUT = 200; // Timeout in milliseconds

    public static Map<String, String> tcpConnectScan(String target, int port) {
        Map<String, String> result = new HashMap<>();
        result.put("port", String.valueOf(port));

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(target, port), TIMEOUT);
            result.put("status", "open");
            Map<String, String> bannerResult = grabBanner(target, port);
            result.put("service", bannerResult.getOrDefault("service", "unknown"));
        } catch (Exception e) {
            result.put("status", "closed");
            result.put("service", "unknown");
        }

        return result;
    }

    public static Map<String, String> synScan(String target, int port) {
        Map<String, String> result = new HashMap<>();
        result.put("port", String.valueOf(port));

        try {
            InetAddress address = InetAddress.getByName(target);
            byte[] synPacket = new byte[40]; 
            DatagramPacket packet = new DatagramPacket(synPacket, synPacket.length, address, port);
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.send(packet);
            }
            result.put("status", "syn_sent");
        } catch (Exception e) {
            result.put("status", "error");
        }

        return result;
    }

    public static Map<String, String> udpScan(String target, int port) {
        Map<String, String> result = new HashMap<>();
        result.put("port", String.valueOf(port));

        try {
            InetAddress address = InetAddress.getByName(target);
            byte[] buffer = new byte[10];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.send(packet);
            }
            result.put("status", "udp_sent");
        } catch (Exception e) {
            result.put("status", "error");
        }

        return result;
    }
    // Run a full scan (all methods)
    public static void fullScan(String target, int startPort, int endPort) {
        System.out.println("Starting Full Scan on " + target + " (Ports " + startPort + "-" + endPort + ")");

        for (int port = startPort; port <= endPort; port++) {
            System.out.println(tcpConnectScan(target, port));
        }

        // Map<String, String> detectedOS = DetectOs.detectOs(target);
        // System.out.println("Detected OS: " + detectedOS.get("os"));
        // System.out.println("Full scan completed.");
    }

    public static Map<String, String> grabBanner(String ipAddress, int port) {
        Map<String, String> result = new HashMap<>();
        result.put("port", String.valueOf(port));

        try (Socket socket = new Socket(ipAddress, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             OutputStream writer = socket.getOutputStream()) {

            writer.write("HEAD / HTTP/1.0\r\n\r\n".getBytes());
            writer.flush();

            String line = reader.readLine();
            result.put("service", (line != null) ? line : "unknown");
        } catch (Exception e) {
            result.put("service", "unknown");
        }

        return result;
    }
}