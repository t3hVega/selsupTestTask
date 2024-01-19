package ru.selsup.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        Logger logger = Logger.getLogger(Main.class.getName());
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        httpServer.createContext("/", new httpHandler());
        httpServer.setExecutor(null);
        httpServer.start();
        HttpClient httpClient = HttpClient.newHttpClient();
        CrptApi crptApi = new CrptApi(httpClient, TimeUnit.SECONDS, 3, 3);
        CrptApi.Document document = crptApi.getDocument();
        Thread thread = new Thread(() -> {
            try {
                crptApi.createDocument(document, "1");
                crptApi.createDocument(document, "2");
                crptApi.createDocument(document, "3");
                crptApi.createDocument(document, "1");
                crptApi.createDocument(document, "2");
                crptApi.createDocument(document, "3");
                crptApi.createDocument(document, "1");
                crptApi.createDocument(document, "2");
                Thread.sleep(9500);
                crptApi.createDocument(document, "3");
                crptApi.createDocument(document, "1");
                crptApi.createDocument(document, "2");
                crptApi.createDocument(document, "3");
                Thread.sleep(3500);
                crptApi.createDocument(document, "1");
                crptApi.createDocument(document, "2");
            } catch (IOException | InterruptedException e) {
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