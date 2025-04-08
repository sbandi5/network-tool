package com.cybersec.sniffer;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cybersec.websocket.PacketWebSocketHandler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

@Service
public class RawSocketSniffer {

    private volatile boolean running = false;
    private volatile boolean sniffing = false;
    private final List<Map<String, String>> packets = new ArrayList<>();
    private Process pythonProcess;
    private Thread snifferThread;

    // ---------- Mode 1: All interfaces via Python ----------
    public void startPythonSniffing() {
        if (running)
            return;
        running = true;

        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("sudo", "python3", "./sniffer.py");
                pb.redirectErrorStream(true);
                pythonProcess = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
                String line;

                while (running && (line = reader.readLine()) != null) {
                    try {
                        PacketWebSocketHandler.broadcast(line);
                        Map<String, Object> json = new ObjectMapper().readValue(line, Map.class);

                        Map<String, String> data = new HashMap<>();
                        data.put("timestamp", new Date().toString());
                        data.put("sourceIP", (String) json.get("source_ip"));
                        data.put("destIP", (String) json.get("dest_ip"));
                        data.put("protocol", (String) json.get("protocol"));
                        data.put("length", String.valueOf(json.get("length")));

                        synchronized (packets) {
                            packets.add(data);
                            if (packets.size() > 1000)
                                packets.remove(0);
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid JSON: " + line);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ---------- Mode 2: Java DatagramSocket on specific port ----------
    public void startJavaSniffing(int listenPort) {
        if (sniffing)
            return;

        sniffing = true;
        snifferThread = new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(listenPort);
                byte[] buffer = new byte[65535];
                System.out.println("Java socket sniffer started on port " + listenPort);

                while (sniffing) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    Map<String, String> pkt = new HashMap<>();
                    pkt.put("sourceIP", packet.getAddress().getHostAddress());
                    pkt.put("length", String.valueOf(packet.getLength()));
                    pkt.put("data", new String(packet.getData(), 0, packet.getLength()));

                    synchronized (packets) {
                        packets.add(pkt);
                        if (packets.size() > 1000) {
                            packets.remove(0);
                        }
                    }

                    Thread.sleep(10);
                }

                socket.close();

            } catch (Exception e) {
                System.out.println("Sniffer error: " + e.getMessage());
            }
        });

        snifferThread.start();
    }

    public void stopSniffing() {
        // Stop Python
        running = false;
        if (pythonProcess != null)
            pythonProcess.destroy();

        // Stop Java
        sniffing = false;
        if (snifferThread != null)
            snifferThread.interrupt();
    }

    public List<Map<String, String>> getPackets() {
        synchronized (packets) {
            return new ArrayList<>(packets);
        }
    }
}
