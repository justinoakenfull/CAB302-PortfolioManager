package com.javarepowizards.portfoliomanager.services.utility;


import org.json.JSONArray;
import org.json.JSONObject;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Service for communicating with a local Ollama API instance.
 * Detects available models, sends prompts, and retrieves generated responses.
 */
public class OllamaService {
    // OllamaService is a service that communicates with the Ollama API to generate responses based on a given prompt.
    private static final String TAGS_URL     = "http://localhost:11434/api/tags";
    private static final String GENERATE_URL = "http://localhost:11434/api/generate";
    private String modelName;

    /**
     * Constructs an OllamaService instance and detects the default model.
     * If no model is detected, it sets the modelName to null.
     */
    public OllamaService() {
        refreshModelName();
    }

    /**
     * Attempts to detect the default model from the Ollama API
     * and stores it in {@code modelName}.  If detection fails,
     * {@code modelName} will be set to null.
     */
    public void refreshModelName() {
        try {
            this.modelName = detectDefaultModel();
        } catch (IllegalStateException e) {
            // Ollama not reachable or no models installed
            this.modelName = null;
        }
    }

    /**
     * Detects the default model by sending a GET request to the Ollama API.
     * If no model is found, it throws an IllegalStateException.
     *
     * @return The name of the detected model.
     * @throws IllegalStateException if no model is found or if an error occurs during detection.
     */
    public String detectDefaultModel() {
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
     * Generates a response from the Ollama model based on the provided prompt.
     * If no model is detected, it throws an IllegalStateException.
     *
     * @param prompt The input prompt for the model.
     * @return The generated response as a String.
     * @throws IOException if an error occurs during the HTTP request or response handling.
     */
    public String generateResponse(String prompt) throws IOException {
        refreshModelName();

        if (modelName == null) {
            throw new IllegalStateException("No Ollama model detected");

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

    /**
     * Checks if the Ollama service is available by sending a GET request to /api/tags.
     * If the modelName is null, it returns false.
     *
     * @return true if the service is available, false otherwise.
     */
    public Boolean isServiceAvailable(){

        refreshModelName();

        if (modelName == null) {
            return false;
        }

        try
        {
            HttpURLConnection conn = (HttpURLConnection) new URL(TAGS_URL).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(500);
            conn.setReadTimeout(500);
            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }
}
