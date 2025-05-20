package com.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.javaBeans.Patient;
import com.javaBeans.User;

public class UserDAO implements UserService {

    private DbConfigDAO dbInstance;
    private Connection connection;

    public UserDAO() {
        dbInstance = DbConfigDAO.getInstance();
        try {
            // 🔧 CORRIGÉ : stocker la connexion dans l'attribut
            connection = dbInstance.getConnection();  
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // For login:
    @Override
    public User checkLogin(String email, String password) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ? and password = ?";
        PreparedStatement preStat = connection.prepareStatement(query);
        preStat.setString(1, email);
        preStat.setString(2, password);
        ResultSet result = preStat.executeQuery();

        User user = null;

        if (result.next()) {
            int id = result.getInt("id_user");
            String firstName = result.getString("firstName");
            String lastName = result.getString("lastName");
            String phone = result.getString("phone");
            String cin = result.getString("cin");
            String accountType = result.getString("accountType");

            user = new User(id, cin, firstName, lastName, phone, email, password);
            user.setAccountType(accountType);
        }

        result.close();
        preStat.close();

        return user;
    }

    // For register:
    @Override
    public boolean isExist(String email, String cin) throws SQLException {
        boolean exist = false;
        String query = "SELECT id_user FROM user WHERE email = ? and cin = ?";
        PreparedStatement preStat = connection.prepareStatement(query);
        preStat.setString(1, email);
        preStat.setString(2, cin);
        ResultSet resultSet = preStat.executeQuery();

        exist = resultSet.next();
        resultSet.close();
        preStat.close();

        return exist;
    }

    @Override
    public int register(Patient patient) throws SQLException {
        // Insert into user table
        String userQuery = "INSERT INTO user (firstName, lastName, phone, email, password, cin) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement preStatOfUser = connection.prepareStatement(userQuery);

        preStatOfUser.setString(1, patient.getFirstName());
        preStatOfUser.setString(2, patient.getLastName());
        preStatOfUser.setString(3, patient.getPhone());
        preStatOfUser.setString(4, patient.getEmail());
        preStatOfUser.setString(5, patient.getPassword());
        preStatOfUser.setString(6, patient.getCin());

        preStatOfUser.executeUpdate();

        // Get last inserted user ID
        String maxQuery = "SELECT MAX(id_user) AS MID FROM user";
        PreparedStatement ms = connection.prepareStatement(maxQuery);
        ResultSet resultSet = ms.executeQuery();
        if (resultSet.next()) {
            patient.setId_user(resultSet.getInt("MID"));
        }

        resultSet.close();
        ms.close();

        // Insert into patient table
        String patientQuery = "INSERT INTO patient (id_patient, BirthDate, sex) VALUES (?, ?, ?)";
        PreparedStatement preStatOfPatient = connection.prepareStatement(patientQuery);
        preStatOfPatient.setInt(1, patient.getId_user());
        preStatOfPatient.setString(2, patient.getBirthDate());
        preStatOfPatient.setString(3, patient.getSex());

        preStatOfPatient.executeUpdate();

        preStatOfPatient.close();
        preStatOfUser.close();

        return 0;
    }
}
