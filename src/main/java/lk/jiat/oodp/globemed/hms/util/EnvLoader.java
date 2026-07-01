package lk.jiat.oodp.globemed.hms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to load configuration variables from a local .env file.
 */
public class EnvLoader {
    private static final Map<String, String> envMap = new HashMap<>();

    static {
        load();
        validate();
    }

    private static void load() {
        File envFile = new File(".env");
        if (envFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();
                        
                        // Strip surrounding quotes if present
                        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        }
                        
                        envMap.put(key, value);
                    }
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not read .env file: " + e.getMessage());
            }
        }
    }

    public static String get(String key) {
        String val = envMap.get(key);
        if (val == null) {
            val = System.getenv(key);
        }
        if (val == null) {
            val = System.getProperty(key);
        }
        return val;
    }

    private static void validate() {
        String dbUrl = get("DB_URL");
        String dbUser = get("DB_USER");
        String dbPassword = get("DB_PASSWORD");

        if (dbUrl == null || dbUser == null || dbPassword == null) {
            StringBuilder missing = new StringBuilder();
            if (dbUrl == null) missing.append("  - DB_URL\n");
            if (dbUser == null) missing.append("  - DB_USER\n");
            if (dbPassword == null) missing.append("  - DB_PASSWORD\n");

            String errorMessage = "\n========================================================================\n" +
                    "  [ERROR] MISSING DATABASE CONFIGURATION ENVIRONMENT VARIABLES\n" +
                    "========================================================================\n" +
                    "The following required configuration values are missing:\n" +
                    missing.toString() +
                    "Please make sure you have created a '.env' file in the root of your project\n" +
                    "and filled in the details. You can use '.env.example' as a template.\n" +
                    "========================================================================\n";
            System.err.println(errorMessage);
            throw new IllegalStateException("Database configuration variables are missing. Please setup your .env file.");
        }
    }
}
