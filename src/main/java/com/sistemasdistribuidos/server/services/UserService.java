package com.sistemasdistribuidos.server.services;

import com.sistemasdistribuidos.server.models.User;
import com.sistemasdistribuidos.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.regex.Pattern;

public class UserService {
    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    public boolean isEmailAlreadyExists(String email) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
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

    public boolean isValidName(String name) {
        return name.length() >= 6 && name.length() <= 30;
    }

    public int saveUser(String nome, String email, String senha) {
        User user = new User(nome, email, senha);
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
            return 201;
        } catch (Exception e) {
            e.printStackTrace();
            return 500;
        }
    }

    public User getUserByEmailAndPassword(String email, String senha) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email AND senha = :senha", User.class);
            query.setParameter("email", email);
            query.setParameter("senha", senha);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateUser(User user, String name) {
        user.setNome(name);
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.update(user);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getUserByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteUser(User user) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.delete(user);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
