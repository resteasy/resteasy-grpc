package dev.resteasy.grpc.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatGPTClient2 {

    public static void chatGPT(String msg1) {
        String url = "https://api.openai.com/v1/chat/completions";
        //      String url = "https://www.redhat.com/en/topics/middleware";
        String apiKey = "sk-proj-1Y4WT59SB5wdGwKNPGQRT3BlbkFJkncCzs5HAi6vhoO6gLZA";
        String model = "gpt-3.5-turbo";
        HttpURLConnection connection1 = null;
        HttpURLConnection connection2 = null;
        OutputStreamWriter writer1 = null;
        OutputStreamWriter writer2 = null;
        BufferedReader br1 = null;
        BufferedReader br2 = null;

        try {
            URL obj = new URL(url);
            connection1 = (HttpURLConnection) obj.openConnection();
            connection1.setRequestMethod("POST");
            connection1.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection1.setRequestProperty("Content-Type", "application/json");
            connection1.setDoOutput(true);
            //         connection1.setRequestProperty("Content-Length", "-1");
            connection1.setRequestProperty("Transfer-Encoding", "chunked");
            //         int respCode = connection1.getResponseCode();
            connection2 = (HttpURLConnection) obj.openConnection();
            connection2.setRequestMethod("POST");
            connection2.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection2.setRequestProperty("Content-Type", "application/json");
            connection2.setDoOutput(true);
            writer1 = new OutputStreamWriter(connection1.getOutputStream());
            writer2 = new OutputStreamWriter(connection2.getOutputStream());
            StringBuffer response1 = new StringBuffer();
            StringBuffer response2 = new StringBuffer();

            // Request body 1
            String body1 = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + msg1
                    + "\"}]}";

            for (int i = 0; i < 5; i++) {

                writer1.write(body1);
                writer1.flush();

                // Response from ChatGPT
                br1 = new BufferedReader(new InputStreamReader(connection1.getInputStream()));
                String line1;
                while ((line1 = br1.readLine()) != null) {
                    response1.append(line1);
                }
                msg1 = extractMessageFromJSONResponse(response1.toString());
                System.out.println("chatGPT1: " + msg1);
                String body2 = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + msg1
                        + "\"}]}";
                writer2.write(body2);
                writer2.flush();
                String line2;
                br2 = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
                while ((line2 = br2.readLine()) != null) {
                    response2.append(line2);
                }
                String msg2 = extractMessageFromJSONResponse(response2.toString());
                System.out.println("chatGPT2: " + msg2);
                body1 = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + msg2 + "\"}]}";
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (br1 != null) {
                    br1.close();
                }
                if (br2 != null) {
                    br2.close();
                }
                if (writer1 != null) {
                    writer1.close();
                }
                if (writer2 != null) {
                    writer2.close();
                }
                if (connection1 != null) {
                    connection1.disconnect();
                }
                if (connection2 != null) {
                    connection2.disconnect();
                }
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    public static String extractMessageFromJSONResponse(String response) {
        int start = response.indexOf("content") + 11;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }

    public static void main(String[] args) {

        chatGPT("Hi. I'm chatGPT. Let's talk.");
    }
}
