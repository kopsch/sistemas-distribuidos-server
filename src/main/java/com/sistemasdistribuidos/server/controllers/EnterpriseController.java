package com.sistemasdistribuidos.server.controllers;

import com.sistemasdistribuidos.server.models.Enterprise;
import com.sistemasdistribuidos.server.services.EnterpriseService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;

public class EnterpriseController {
    private final EnterpriseService enterpriseService;
    private final Map<String, Enterprise> enterpriseSessionMap;
    private final Map<String, Enterprise> sessionEnterpriseMap;

    public EnterpriseController(EnterpriseService enterpriseService, Map<String, Enterprise> enterpriseSessionMap, Map<String, Enterprise> sessionEnterpriseMap) {
        this.enterpriseService = enterpriseService;
        this.enterpriseSessionMap = enterpriseSessionMap;
        this.sessionEnterpriseMap = sessionEnterpriseMap;
    }

    public void handleCadastrarEmpresa(JsonNode jsonNode, ObjectNode responseNode, PrintWriter out) {
        String razaoSocial = jsonNode.get("razaoSocial").asText();
        String email = jsonNode.get("email").asText();
        String cnpj = jsonNode.get("cnpj").asText();
        String descricao = jsonNode.get("descricao").asText();
        String ramo = jsonNode.get("ramo").asText();
        String senha = jsonNode.get("senha").asText();

        if (enterpriseService.isEmailAlreadyExists(email)) {
            responseNode.put("status", 400);
            responseNode.put("operacao", "cadastrarEmpresa");
            responseNode.put("mensagem", "E-mail já cadastrado");
            out.println(responseNode.toString());
            return;
        }

        if (!enterpriseService.isValidEmail(email)) {
            responseNode.put("status", 400);
            responseNode.put("operacao", "cadastrarEmpresa");
            responseNode.put("mensagem", "Formato de e-mail inválido");
            out.println(responseNode.toString());
            return;
        }

        if (!enterpriseService.isValidPassword(senha)) {
            responseNode.put("status", 400);
            responseNode.put("operacao", "cadastrarEmpresa");
            responseNode.put("mensagem", "Senha inválida. Deve conter apenas caracteres numéricos e ter entre 3 e 8 caracteres");
            out.println(responseNode.toString());
            return;
        }

        if (!enterpriseService.isValidCnpj(cnpj)) {
            responseNode.put("status", 400);
            responseNode.put("operacao", "cadastrarEmpresa");
            responseNode.put("mensagem", "CNPJ inválido. Deve ter 14 caracteres numéricos");
            out.println(responseNode.toString());
            return;
        }

        int status = enterpriseService.saveEnterprise(razaoSocial, email, cnpj, descricao, ramo, senha);
        if (status == 201) {
            String token = UUID.randomUUID().toString();
            Enterprise enterprise = enterpriseService.getEnterpriseByEmail(email);
            enterpriseSessionMap.put(token, enterprise);
            sessionEnterpriseMap.put(email, enterprise);
            responseNode.put("token", token);
        }

        responseNode.put("status", status);
        responseNode.put("operacao", "cadastrarEmpresa");
        out.println(responseNode.toString());
    }

    public void handleLoginEmpresa(JsonNode jsonNode, ObjectNode responseNode, PrintWriter out) {
        String email = jsonNode.get("email").asText();
        String senha = jsonNode.get("senha").asText();

        Enterprise enterprise = enterpriseService.getEnterpriseByEmailAndPassword(email, senha);
        if (enterprise != null) {
            String token = UUID.randomUUID().toString();
            enterpriseSessionMap.put(token, enterprise);
            sessionEnterpriseMap.put(email, enterprise);
            responseNode.put("status", 200);
            responseNode.put("token", token);
            responseNode.put("operacao", "loginEmpresa");
        } else {
            responseNode.put("status", 401);
            responseNode.put("mensagem", "E-mail ou senha incorretos");
            responseNode.put("operacao", "loginEmpresa");
        }

        out.println(responseNode.toString());
    }

    public void handleAtualizarEmpresa(JsonNode jsonNode, ObjectNode responseNode, PrintWriter out) {
        String email = jsonNode.get("email").asText();
        String senha = jsonNode.get("senha").asText();
        String razaoSocial = jsonNode.get("razaoSocial").asText();
        String descricao = jsonNode.get("descricao").asText();
        String ramo = jsonNode.get("ramo").asText();

        Enterprise enterprise = enterpriseService.getEnterpriseByEmailAndPassword(email, senha);
        if (enterprise != null) {
            enterpriseService.updateEnterprise(enterprise, razaoSocial, descricao, ramo);
            responseNode.put("status", 201);
            responseNode.put("operacao", "atualizarEmpresa");
        } else {
            responseNode.put("status", 401);
            responseNode.put("mensagem", "E-mail ou senha incorretos");
            responseNode.put("operacao", "atualizarEmpresa");
        }

        out.println(responseNode.toString());
    }

    public void handleVisualizarEmpresa(JsonNode jsonNode, ObjectNode responseNode, PrintWriter out) {
        String email = jsonNode.get("email").asText();

        Enterprise enterprise = sessionEnterpriseMap.get(email);
        if (enterprise != null) {
            Enterprise enterpriseProfile = enterpriseService.getEnterpriseByEmail(enterprise.getEmail());
            responseNode.put("operacao", "visualizarEmpresa");
            responseNode.put("status", 201);
            responseNode.put("razaoSocial", enterpriseProfile.getRazaoSocial());
            responseNode.put("email", enterpriseProfile.getEmail());
            responseNode.put("cnpj", enterpriseProfile.getCnpj());
            responseNode.put("descricao", enterpriseProfile.getDescricao());
            responseNode.put("ramo", enterpriseProfile.getRamo());
            responseNode.put("senha", enterpriseProfile.getSenha());
        } else {
            responseNode.put("status", 404);
            responseNode.put("mensagem", "E-mail não encontrado");
            responseNode.put("operacao", "visualizarEmpresa");
        }

        out.println(responseNode.toString());
    }

    public void handleApagarEmpresa(JsonNode jsonNode, ObjectNode responseNode, PrintWriter out) {
        String email = jsonNode.get("email").asText();

        Enterprise enterprise = sessionEnterpriseMap.get(email);
        if (enterprise != null) {
            Enterprise enterpriseProfile = enterpriseService.getEnterpriseByEmail(enterprise.getEmail());
            enterpriseService.deleteEnterprise(enterpriseProfile);
            responseNode.put("operacao", "apagarEmpresa");
            responseNode.put("status", 201);
        } else {
            responseNode.put("status", 404);
            responseNode.put("mensagem", "E-mail não encontrado");
            responseNode.put("operacao", "apagarEmpresa");
        }

        out.println(responseNode.toString());
    }

    public void handleLogout(JsonNode jsonNode, ObjectNode responseNode, PrintWriter out) {
        String token = jsonNode.get("token").asText();
        System.out.println(token);
        System.out.println(enterpriseSessionMap);

        if (enterpriseSessionMap.remove(token) != null) {
            sessionEnterpriseMap.remove(token);
            responseNode.put("status", 200);
            responseNode.put("mensagem", "Logout bem-sucedido");
        } else {
            responseNode.put("status", 400);
            responseNode.put("mensagem", "Token inválido");
        }

        responseNode.put("operacao", "logout");
        out.println(responseNode.toString());
    }
}
