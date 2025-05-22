package com.javarepowizards.portfoliomanager.services;


import org.json.JSONArray;
import org.json.JSONObject;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OllamaService {
    private static final String TAGS_URL     = "http://localhost:11434/api/tags";
    private static final String GENERATE_URL = "http://localhost:11434/api/generate";

    private final String modelName;

    public OllamaService() {
        String detected = null;
        try {
            detected = detectDefaultModel();
        } catch (IllegalStateException e) {
            // log.warn("Ollama not reachable; AI features disabled", e);
        }
        this.modelName = detected;
    }


    private String detectDefaultModel() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(TAGS_URL).openConnection();

            //set timeout values (in ms?)
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(5000);

            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();
            InputStream in = status < 400
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            if (status >= 400) {
                throw new IOException("HTTP " + status + ": " + sb);
            }

            JSONArray models = new JSONObject(sb.toString()).getJSONArray("models");
            if (models.isEmpty()) {
                throw new IllegalStateException("No Ollama models installed locally");
            }

            return models.getJSONObject(0).getString("name");

        } catch (IOException e) {
            throw new IllegalStateException("Failed to detect Ollama model", e);
        }
    }

    /**
     * Sends the given prompt to /api/generate using the detected model.
     */
    public String generateResponse(String prompt) throws IOException {
        if (modelName == null) {
            // This shouldnt be hit cause we shouldnt call it if there is no model
            // but we want to make sure we don't crash if no ollama model is running.
            return "";
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(GENERATE_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject req = new JSONObject()
                .put("model", modelName)
                .put("prompt", prompt)
                .put("stream", false);

        try (var os = conn.getOutputStream()) {
            os.write(req.toString().getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream in = status < 400
                ? conn.getInputStream()
                : conn.getErrorStream();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }

        if (status >= 400) {
            throw new IOException("HTTP " + status + ": " + sb);
        }

        return new JSONObject(sb.toString()).getString("response");
    }
}
