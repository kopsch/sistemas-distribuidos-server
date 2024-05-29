package com.sistemasdistribuidos.server.controllers;

import com.sistemasdistribuidos.server.models.User;
import com.sistemasdistribuidos.server.services.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;

public class UserController {
    private final UserService userService;
    private final Map<String, User> userSessionMap;
    private final Map<String, User> sessionUserMap;

    public UserController(UserService userService, Map<String, User> userSessionMap, Map<String, User> sessionUserMap) {
        this.userService = userService;
        this.userSessionMap = userSessionMap;
        this.sessionUserMap = sessionUserMap;
    }

    public void handleCadastrarCandidato(JsonNode jsonNode, ObjectNode responseNode, PrintWriter out) {
        String nome = jsonNode.get("nome").asText();
        String email = jsonNode.get("email").asText();
        String senha = jsonNode.get("senha").asText();

        if (userService.isEmailAlreadyExists(email)) {
            responseNode.put("status", 400);
            responseNode.put("operacao", "cadastrarCandidato");
            responseNode.put("mensagem", "E-mail já cadastrado");
            out.println(responseNode.toString());
            return;
        }

        if (!userService.isValidEmail(email)) {
            responseNode.put("status", 400);
            responseNode.put("operacao", "cadastrarCandidato");
            responseNode.put("mensagem", "Formato de e-mail inválido");
            out.println(responseNode.toString());
            return;
        }

        if (!userService.isValidPassword(senha)) {
            responseNode.put("status", 400);
            responseNode.put("operacao", "cadastrarCandidato");
            responseNode.put("mensagem", "Senha inválida. Deve conter apenas caracteres numéricos e ter entre 3 e 8 caracteres");
            out.println(responseNode.toString());
            return;
        }

        if (!userService.isValidName(nome)) {
            responseNode.put("status", 400);
            responseNode.put("operacao", "cadastrarCandidato");
            responseNode.put("mensagem", "Nome inválido. Deve ter entre 6 e 30 caracteres");
            out.println(responseNode.toString());
            return;
        }

        int status = userService.saveUser(nome, email, senha);
        if (status == 201) {
            String token = UUID.randomUUID().toString();
            User user = userService.getUserByEmail(email);
            userSessionMap.put(token, user);
            sessionUserMap.put(email, user);
            responseNode.put("token", token);
        }

        responseNode.put("status", status);
        responseNode.put("operacao", "cadastrarCandidato");
        out.println(responseNode.toString());
    }

    public void handleLoginCandidato(JsonNode jsonNode, ObjectNode responseNode, PrintWriter out) {
        String email = jsonNode.get("email").asText();
        String senha = jsonNode.get("senha").asText();

        User user = userService.getUserByEmailAndPassword(email, senha);
        if (user != null) {
            String token = UUID.randomUUID().toString();
            userSessionMap.put(token, user);
            sessionUserMap.put(email, user);
            responseNode.put("status", 200);
            responseNode.put("token", token);
            responseNode.put("operacao", "loginCandidato");
        } else {
            responseNode.put("status", 401);
            responseNode.put("mensagem", "E-mail ou senha incorretos");
            responseNode.put("operacao", "loginCandidato");
        }

        out.println(responseNode.toString());
    }

    public void handleAtualizarCandidato(JsonNode jsonNode, ObjectNode responseNode, PrintWriter out) {
        String email = jsonNode.get("email").asText();
        String senha = jsonNode.get("senha").asText();
        String nome = jsonNode.get("nome").asText();

        User user = userService.getUserByEmailAndPassword(email, senha);
        if (user != null) {
            if (!userService.isValidName(nome)) {
                responseNode.put("status", 400);
                responseNode.put("operacao", "atualizarCandidato");
                responseNode.put("mensagem", "Nome inválido. Deve ter entre 6 e 30 caracteres");
                out.println(responseNode.toString());
                return;
            }

            userService.updateUser(user, nome);
            responseNode.put("status", 201);
            responseNode.put("operacao", "atualizarCandidato");
        } else {
            responseNode.put("status", 401);
            responseNode.put("mensagem", "E-mail ou senha incorretos");
            responseNode.put("operacao", "atualizarCandidato");
        }

        out.println(responseNode.toString());
    }

    public void handleVisualizarCandidato(JsonNode jsonNode, ObjectNode responseNode, PrintWriter out) {
        String email = jsonNode.get("email").asText();

        User user = sessionUserMap.get(email);
        if (user != null) {
            User userProfile = userService.getUserByEmail(user.getEmail());
            responseNode.put("operacao", "visualizarCandidato");
            responseNode.put("status", 201);
            responseNode.put("nome", userProfile.getNome());
            responseNode.put("email", userProfile.getEmail());
            responseNode.put("senha", userProfile.getSenha());
        } else {
            responseNode.put("status", 404);
            responseNode.put("mensagem", "E-mail não encontrado");
            responseNode.put("operacao", "visualizarCandidato");
        }

        out.println(responseNode.toString());
    }

    public void handleApagarCandidato(JsonNode jsonNode, ObjectNode responseNode, PrintWriter out) {
        String email = jsonNode.get("email").asText();

        User user = sessionUserMap.get(email);
        if (user != null) {
            User userProfile = userService.getUserByEmail(user.getEmail());
            userService.deleteUser(userProfile);
            responseNode.put("operacao", "apagarCandidato");
            responseNode.put("status", 201);
        } else {
            responseNode.put("status", 404);
            responseNode.put("mensagem", "E-mail não encontrado");
            responseNode.put("operacao", "apagarCandidato");
        }

        out.println(responseNode.toString());
    }

    public void handleLogout(JsonNode jsonNode, ObjectNode responseNode, PrintWriter out) {

        responseNode.put("status", 200);
        responseNode.put("mensagem", "Logout bem-sucedido");
        responseNode.put("operacao", "logout");
        out.println(responseNode.toString());
    }
}
