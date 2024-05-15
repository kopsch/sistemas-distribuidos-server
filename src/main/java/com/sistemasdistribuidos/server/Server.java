package com.sistemasdistribuidos.server;

import com.sistemasdistribuidos.server.controllers.UserController;
import com.sistemasdistribuidos.server.models.User;
import com.sistemasdistribuidos.server.services.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT = 22222;
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private static final UserService userService = new UserService();
    private static final Map<String, User> userSessionMap = new HashMap<>();
    private static final Map<String, User> sessionUserMap = new HashMap<>();
    private static final UserController userController = new UserController(userService, userSessionMap, sessionUserMap);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started...");
            while (true) {
                pool.execute(new ClientHandler(serverSocket.accept()));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {

        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                String received;
                while ((received = in.readLine()) != null) {
                    System.out.println("Operação recebida do cliente: " + received);
                    processOperation(received, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void processOperation(String operation, PrintWriter out) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode responseNode = mapper.createObjectNode();

                JsonNode jsonNode = mapper.readTree(operation);
                String operationType = jsonNode.get("operacao").asText();

                switch (operationType) {
                    case "cadastrarCandidato":
                        userController.handleCadastrarCandidato(jsonNode, responseNode, out);
                        break;
                    case "loginCandidato":
                        userController.handleLoginCandidato(jsonNode, responseNode, out);
                        break;
                    case "atualizarCandidato":
                        userController.handleAtualizarCandidato(jsonNode, responseNode, out);
                        break;
                    case "visualizarCandidato":
                        userController.handleVisualizarCandidato(jsonNode, responseNode, out);
                        break;
                    case "apagarCandidato":
                        userController.handleApagarCandidato(jsonNode, responseNode, out);
                        break;
                    case "logout":
                        userController.handleLogout(jsonNode, responseNode, out);
                        break;
                    default:
                        responseNode.put("status", 400);
                        responseNode.put("mensagem", "Operação inválida");
                        out.println(responseNode.toString());
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                out.println("{\"status\": 500, \"mensagem\": \"Erro ao processar a operação\"}");
            }
        }
    }
}
