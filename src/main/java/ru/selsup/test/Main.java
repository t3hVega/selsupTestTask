package ru.selsup.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {
        Logger logger = Logger.getLogger(Main.class.getName());
        ObjectMapper objectMapper = new ObjectMapper();
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        httpServer.createContext("/", new httpHandler());
        httpServer.setExecutor(null);
        httpServer.start();
        HttpClient httpClient = HttpClient.newHttpClient();
        CrptApi crptApi = new CrptApi(httpClient, TimeUnit.SECONDS, 5, 5);
        String documentContents = objectMapper.writeValueAsString(crptApi.getDocument());

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080"))
                .header("Content-Type", "application/json")
                .GET()
                .POST(HttpRequest.BodyPublishers.ofString(documentContents))
                .build();
        logger.info(httpRequest.toString());
        Thread thread = new Thread(() -> {
            try {
                crptApi.processRequest(httpRequest);
                crptApi.processRequest(httpRequest);
                crptApi.processRequest(httpRequest);
                crptApi.processRequest(httpRequest);
                crptApi.processRequest(httpRequest);
                crptApi.processRequest(httpRequest);
                crptApi.processRequest(httpRequest);
                crptApi.processRequest(httpRequest);
                crptApi.processRequest(httpRequest);
                crptApi.processRequest(httpRequest);
                crptApi.processRequest(httpRequest);
                crptApi.processRequest(httpRequest);
                crptApi.processRequest(httpRequest);
                Thread.sleep(5500);
                crptApi.processRequest(httpRequest);
                crptApi.processRequest(httpRequest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }

    static class httpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (t.getRequestMethod().equals("POST")) {
                InputStream inputStream = t.getRequestBody();
                String input = new BufferedReader(new InputStreamReader(inputStream))
                        .lines().parallel().collect(Collectors.joining("\n"));
                t.sendResponseHeaders(200, input.length());
                OutputStream outputStream = t.getResponseBody();
                outputStream.write(input.getBytes());
                outputStream.close();
            }
        }
    }
}