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
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0); //cria um servidor HTTP
        System.out.println("Servidor rodando em http://localhost:8081");

        // Rota principal
        server.createContext("/", Main::handleRequest); //rota / chama o metodo handlerequest
        server.start(); //inicia o servidor
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {//metodo que lida com as requisições http
        String path = exchange.getRequestURI().getPath(); //Pega o caminho da URL

        try {
            if ("/api/pets".equals(path)) { //ve qual rota o usuario acessou
                switch (exchange.getRequestMethod()) {
                    case "GET" -> listarPets(exchange);
                    case "POST" -> cadastrarPet(exchange);
                    case "DELETE" -> excluirPet(exchange);
                }
            } else {
                enviarArquivo(exchange, path.equals("/") ? "/index.html" : path); // se a tora nao for aquela, ele manda um arquivo estatico
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0); //envia "Erro interno no servidor"
        } finally {
            exchange.close(); //fecha o exchange(a conexao com o cliente
        }
    }

    // Listar
    private static void listarPets(HttpExchange exchange) throws SQLException, IOException {
        List<Pet> pets = PetDAO.listar(); //chama o metodo listar da classe petDAO
        StringBuilder json = new StringBuilder("["); // cria uma string JSON que começa com [
        for (int i = 0; i < pets.size(); i++) {
            Pet p = pets.get(i);
            json.append(String.format(
                    "{\"id\":%d,\"nome\":\"%s\",\"especie\":\"%s\",\"idade\":%d,\"tutor\":\"%s\"}",
                    p.getId(), p.getNome(), p.getEspecie(), p.getIdade(), p.getTutor()
            ));
            if (i < pets.size() - 1) json.append(","); //adiciona , depois de cada objeto menos depois do ultimo
        }
        json.append("]");
        enviarJson(exchange, json.toString()); // escreve o JSON no corpo da resposta MQTT
    }

    //Cadastrar
    private static void cadastrarPet(HttpExchange exchange) throws IOException, SQLException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); //codigo le o body da requisição
        System.out.println("JSON recebido: " + body);

        if (body == null || body.isEmpty()) {
            System.out.println("Corpo vazio recebido no POST!");
            exchange.sendResponseHeaders(400, 0);
            return;
        }

        // Remove chaves e aspas
        String json = body.replace("{", "").replace("}", "").replace("\"", "");//tira as aspas e chaves pra pegar os pares chave:valor
        String[] pares = json.split(","); //separa por virgulas

        String nome = "", especie = "", tutor = ""; //cria variaveis pra guardar os campos que vao ser lidas do JSON
        int idade = 0;

        for (String par : pares) { //percorre cada par
            String[] kv = par.split(":");
            if (kv.length == 2) {
                String chave = kv[0].trim(); //limpa os espaços com trim
                String valor = kv[1].trim();

                switch (chave) {
                    case "nome" -> nome = valor; //guarda os valores nas variaveis
                    case "especie" -> especie = valor;
                    case "idade" -> idade = Integer.parseInt(valor);
                    case "tutor" -> tutor = valor;
                }
            }
        }

        try {
            PetDAO.inserir(new Pet(nome, especie, idade, tutor)); //insere o pet no banco
            System.out.println("Pet inserido com sucesso!");
            exchange.sendResponseHeaders(200, 0);
        } catch (SQLException e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
        }
    }

    // Excluir
    private static void excluirPet(HttpExchange exchange) throws SQLException, IOException {
        String query = exchange.getRequestURI().getQuery(); //obtem os parametros de consulta. Exemplo: se a URL for http://localhost:8081/api/pets?id=5,
        //então getQuery() retorna a string "id=5".
        int id = Integer.parseInt(query.split("=")[1]); //peda o numero 5(exemplo) depois do id=5
        PetDAO.excluir(id);
        System.out.println("Pet excluído com ID " + id);
        exchange.sendResponseHeaders(200, 0);
    }

    //Enviar JSON
    private static void enviarJson(HttpExchange exchange, String json) throws IOException {
        byte[] resp = json.getBytes(StandardCharsets.UTF_8); //converte o JSON para bytes
        exchange.getResponseHeaders().set("Content-Type", "application/json"); //manda um cabeçalho  pro front saber como interpretar a resposta
        exchange.sendResponseHeaders(200, resp.length);  //status ok
        exchange.getResponseBody().write(resp); //envia o corpo com o JSON
    }

    //Enviar arquivos html (Esse é responsavel por enviar arquivos estaticos)
    private static void enviarArquivo(HttpExchange exchange, String path) throws IOException {
        File file = new File("web" + path); //representa o arquivo que o usuario quer acessaar
        if (!file.exists()) { //se não existir, dá not found
            exchange.sendResponseHeaders(404, 0);
            exchange.getResponseBody().write("Página não encontrada".getBytes());
            return;
        }
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath()); //le o conteudo do arquivo como bytes(porque a resposta HTTP envia bytes)
        exchange.sendResponseHeaders(200, bytes.length); //manda um OK e o tamanho do corpo
        exchange.getResponseBody().write(bytes); //envia o conteudo no corpo da resposta HTTP
    }
}
