package com.fortis.api;
//mini backend web server

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class RestAPIServer {
    private HttpServer server;
    
    public RestAPIServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        setupRoutes();
    }
    //routes menu
    private void setupRoutes() {
        server.createContext("/api/test", new TestHandler());
        server.createContext("/api/accounts", new AccountsHandler());
        server.createContext("/api/stats", new StatsHandler());
    }
    //8080 port
    //start server
    //display server info

    public void start() {
        server.setExecutor(null);
        server.start();
        System.out.println("\n========================================");
        System.out.println("  FORTIS BANKING - SERVERS RUNNING");
        System.out.println("========================================");
        System.out.println("Backend API: http://localhost:8080");
        System.out.println("React UI:    http://localhost:3000");
        System.out.println("========================================\n");
    }
    //react is on 3000 and backend is on 8080
    private void enableCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
    
    //web lang is json and it format is {key:value} 
    private void sendJSON(HttpExchange exchange, int statusCode, String json) throws IOException {
        enableCORS(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json");//browser knows it's receiving data, not a plain text file.
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }
    
    class TestHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            sendJSON(exchange, 200, "{\"message\":\"API is running!\"}");
        }
    }
    
    class AccountsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            String json = "[" +
                "{\"account_id\":1,\"account_holder\":\"Alice Smith\",\"balance\":5000.00,\"account_type\":\"SAVINGS\"}," +
                "{\"account_id\":2,\"account_holder\":\"Bob Johnson\",\"balance\":3500.50,\"account_type\":\"CHECKING\"}," +
                "{\"account_id\":3,\"account_holder\":\"Charlie Brown\",\"balance\":10000.00,\"account_type\":\"SAVINGS\"}" +
                "]";
            sendJSON(exchange, 200, json);
        }
    }
    
    class StatsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                enableCORS(exchange);
                exchange.sendResponseHeaders(204, -1);//204 means no content and -1 means no body 
                return;
            }
            
            String json = "{\"accounts\":3,\"transactions\":15,\"volume\":25000.00}";
            sendJSON(exchange, 200, json);
        }
    }
    
    public static void main(String[] args) {
        try {
            RestAPIServer server = new RestAPIServer(8080);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();//it is used to print the stack trace of the exception
        }
    }
}
