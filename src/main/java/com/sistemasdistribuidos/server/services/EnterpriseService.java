package com.sistemasdistribuidos.server.services;

import com.sistemasdistribuidos.server.models.Enterprise;
import com.sistemasdistribuidos.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.regex.Pattern;

public class EnterpriseService {
    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    public boolean isEmailAlreadyExists(String email) {
        try (Session session = sessionFactory.openSession()) {
            Query<Enterprise> query = session.createQuery("FROM Enterprise WHERE email = :email", Enterprise.class);
            query.setParameter("email", email);
            return query.uniqueResult() != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isCnpjAlreadyExists(String cnpj) {
        try (Session session = sessionFactory.openSession()) {
            Query<Enterprise> query = session.createQuery("FROM Enterprise WHERE cnpj = :cnpj", Enterprise.class);
            query.setParameter("cnpj", cnpj);
            return query.uniqueResult() != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isValidEmail(String email) {
        String emailRegex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches() && email.length() >= 7 && email.length() <= 50;
    }

    public boolean isValidPassword(String password) {
        return password.matches("\\d+") && password.length() >= 3 && password.length() <= 8;
    }

    public boolean isValidRazaoSocial(String razaoSocial) {
        return razaoSocial.length() >= 6 && razaoSocial.length() <= 100;
    }

    // public boolean isValidCnpj(String cnpj) {
    //     Integer formattedCnpj = Integer.parseInt(cnpj);

    //     return formattedCnpj.matches("\\d{14}") && isCnpjAlreadyExists(cnpj);
    // }

    public boolean isValidCnpj(String cnpj) {
        // Verifica se o CNPJ tem exatamente 14 caracteres numéricos
        if (cnpj == null || !cnpj.matches("\\d{14}")) {
            return false;
        }

        return true;
    
        // Verifica se o CNPJ já existe (supondo que esta função já está implementada)
        // return isCnpjAlreadyExists(cnpj);
    }

    public boolean isValidRamo(String ramo) {
        return ramo.length() >= 3 && ramo.length() <= 50;
    }

    public int saveEnterprise(String razaoSocial, String email, String cnpj, String descricao, String ramo, String senha) {
        System.out.println(descricao);
        Enterprise enterprise = new Enterprise(razaoSocial, email, cnpj, descricao, ramo, senha);
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(enterprise);
            transaction.commit();
            return 201;
        } catch (Exception e) {
            e.printStackTrace();
            return 500;
        }
    }

    public Enterprise getEnterpriseByEmailAndPassword(String email, String senha) {
        try (Session session = sessionFactory.openSession()) {
            Query<Enterprise> query = session.createQuery("FROM Enterprise WHERE email = :email AND senha = :senha", Enterprise.class);
            query.setParameter("email", email);
            query.setParameter("senha", senha);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateEnterprise(Enterprise enterprise, String razaoSocial, String descricao, String ramo) {
        enterprise.setRazaoSocial(razaoSocial);
        enterprise.setDescricao(descricao);
        enterprise.setRamo(ramo);
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.update(enterprise);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Enterprise getEnterpriseByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Query<Enterprise> query = session.createQuery("FROM Enterprise WHERE email = :email", Enterprise.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteEnterprise(Enterprise enterprise) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.delete(enterprise);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}