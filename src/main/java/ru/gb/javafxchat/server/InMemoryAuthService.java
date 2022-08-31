package ru.gb.javafxchat.server;


import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class InMemoryAuthService implements AuthService {

    private static class UserData {

        private String nick;
        private String login;
        private String password;

        public UserData(String nick, String login, String password) {
            this.nick = nick;
            this.login = login;
            this.password = password;
        }

        public String getNick() {
            return nick;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    }


    private static Connection connection;
    private static Statement createStatement() throws SQLException {
        return connection.createStatement();
    }
    private List<UserData> users;

    public InMemoryAuthService() {
        users = new ArrayList<>();
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/ru/gb/javafxchat/database.db");
            Statement statement = createStatement();
            ResultSet resultSet = statement.executeQuery("select login, password, username from auth");
            while (resultSet.next()){
                String login = resultSet.getString("login");
                String password = resultSet.getString("password");
                String username = resultSet.getString("username");
                users.add(new UserData(username, login, password));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        return users.stream()
                .filter(user -> login.equals(user.getLogin())
                        && password.equals(user.getPassword()))
                .findFirst()
                .map(UserData::getNick)
                .orElse(null);
    }

    @Override
    public String regNickByLoginAndPassword(String login, String password) {

        for (int i = 0; i < users.size(); i++) {
            if(users.get(i).getNick().equals(login)){
                return null;
            }
        }

        try {
            Statement statement = createStatement();

            String insertQueryTamplate = """
                    insert into auth(login, password, username)
                    values('%s', '%s', '%s')
                    """;

            String insertQuery = String.format(insertQueryTamplate, login, password, login);
            statement.executeUpdate(insertQuery);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        users.add(new UserData(login, login, password));

        return login;
    }

    @Override
    public void close() throws IOException {
        System.out.println("Сервис аутентификации остановлен");
    }
}