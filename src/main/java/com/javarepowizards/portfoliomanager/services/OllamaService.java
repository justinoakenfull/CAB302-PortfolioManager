package com.javarepowizards.portfoliomanager.services;


import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OllamaService {

    private final String apiURL;
    private final String modelName;


    public OllamaService() {
        this("http://localhost:11434/api/generate", "llama3.2:latest");
    }

    public OllamaService(String apiURL, String modelName) {
        this.apiURL = apiURL;
        this.modelName = modelName;
    }


    public String generateResponse(String prompt) throws IOException{

        URL url = new URL(apiURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // create request JSON
        JSONObject requestJson = new JSONObject();
        requestJson.put("model", modelName);
        requestJson.put("prompt", prompt);
        requestJson.put("stream", false);


        // send request ong
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestJson.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // read full response

        StringBuilder raw = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = br.readLine()) != null) {
                raw.append(line);
            }
        }

        // Parse and return
        JSONObject resp = new JSONObject(raw.toString());
        return resp.getString("response");
    }

}
