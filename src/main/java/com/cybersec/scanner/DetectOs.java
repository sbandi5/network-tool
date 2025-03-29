package com.cybersec.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class DetectOs {

    public static Map<String, String> detectOs(String ipAddress) {
        Map<String, String> result = new HashMap<>();
        result.put("target", ipAddress);

        try {
            String os = System.getProperty("os.name").toLowerCase();
            System.out.println("Operating System: " + os);
            String[] command;
            if (os.contains("win")) {
                command = new String[]{"ping", "-n", "1", ipAddress};
            } else {
                command = new String[]{"ping", "-c", "1", ipAddress};
            }
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("ttl")) {
                    int ttl = extractTTL(line);
                    result.put("os", guessOS(ttl));
                    break;
                }
            }
        } catch (IOException e) {
            result.put("os", "error");
        }

        return result;
    }

    private static int extractTTL(String line) {
        String[] getTTL = line.toLowerCase().split("ttl=");
        return Integer.parseInt(getTTL[1].split(" ")[0]);
    }

    private static String guessOS(int ttl) {
        if (ttl <= 64)
            return "Linux/Unix";
        else if (ttl <= 128) 
            return "Windows";
        else if (ttl <= 255) 
            return "MacOS";
        else
            return "Unknown";
    }
}
