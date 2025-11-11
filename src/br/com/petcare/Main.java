package br.com.petcare;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        System.out.println("🚀 Servidor rodando em http://localhost:8081");

        // Rota principal
        server.createContext("/", Main::handleRequest);
        server.start();
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        try {
            if ("/api/pets".equals(path)) {
                switch (exchange.getRequestMethod()) {
                    case "GET" -> listarPets(exchange);
                    case "POST" -> cadastrarPet(exchange);
                    case "DELETE" -> excluirPet(exchange);
                }
            } else {
                enviarArquivo(exchange, path.equals("/") ? "/index.html" : path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
        } finally {
            exchange.close();
        }
    }

    // --------------------- LISTAR ---------------------
    private static void listarPets(HttpExchange exchange) throws SQLException, IOException {
        List<Pet> pets = PetDAO.listar();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < pets.size(); i++) {
            Pet p = pets.get(i);
            json.append(String.format(
                    "{\"id\":%d,\"nome\":\"%s\",\"especie\":\"%s\",\"idade\":%d,\"tutor\":\"%s\"}",
                    p.getId(), p.getNome(), p.getEspecie(), p.getIdade(), p.getTutor()
            ));
            if (i < pets.size() - 1) json.append(",");
        }
        json.append("]");
        enviarJson(exchange, json.toString());
    }

    // --------------------- CADASTRAR ---------------------
    private static void cadastrarPet(HttpExchange exchange) throws IOException, SQLException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("📩 JSON recebido: " + body);

        if (body == null || body.isEmpty()) {
            System.out.println("⚠️ Corpo vazio recebido no POST!");
            exchange.sendResponseHeaders(400, 0);
            return;
        }

        // Remove chaves e aspas
        String json = body.replace("{", "").replace("}", "").replace("\"", "");
        String[] pares = json.split(",");

        String nome = "", especie = "", tutor = "";
        int idade = 0;

        for (String par : pares) {
            String[] kv = par.split(":");
            if (kv.length == 2) {
                String chave = kv[0].trim();
                String valor = kv[1].trim();

                switch (chave) {
                    case "nome" -> nome = valor;
                    case "especie" -> especie = valor;
                    case "idade" -> idade = Integer.parseInt(valor);
                    case "tutor" -> tutor = valor;
                }
            }
        }

        try {
            PetDAO.inserir(new Pet(nome, especie, idade, tutor));
            System.out.println("✅ Pet inserido com sucesso!");
            exchange.sendResponseHeaders(200, 0);
        } catch (SQLException e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
        }
    }

    // --------------------- EXCLUIR ---------------------
    private static void excluirPet(HttpExchange exchange) throws SQLException, IOException {
        String query = exchange.getRequestURI().getQuery();
        int id = Integer.parseInt(query.split("=")[1]);
        PetDAO.excluir(id);
        System.out.println("🗑️ Pet excluído com ID " + id);
        exchange.sendResponseHeaders(200, 0);
    }

    // --------------------- ENVIAR JSON ---------------------
    private static void enviarJson(HttpExchange exchange, String json) throws IOException {
        byte[] resp = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
    }

    // --------------------- ENVIAR ARQUIVOS HTML ---------------------
    private static void enviarArquivo(HttpExchange exchange, String path) throws IOException {
        File file = new File("web" + path);
        if (!file.exists()) {
            exchange.sendResponseHeaders(404, 0);
            exchange.getResponseBody().write("Página não encontrada".getBytes());
            return;
        }
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
