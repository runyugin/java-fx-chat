package ru.gb.javafxchat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

// класс ClientHandler для работы с socket клиентов, у каждого коиента он свой
// socket это подключение к серверу
public class ClientHandler {
    private Socket socket;
    private ChatServer server; // знает все о клиентах
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    private AuthService authService;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            // в конструкторе инициализируем поля
            this.authService = authService;
            this.socket = socket; // сервер
            this.server = server; // сокет
            this.in = new DataInputStream(socket.getInputStream()); // поток для чтения сообщений
            this.out = new DataOutputStream(socket.getOutputStream()); // поток для записи сообщений
            new Thread(() -> { // отдельный тред для чтения сообщений
                try {
                    authenticate(); // перед тем как читать сообщение от пользователя, его надо аутентифицировать по логину и паролю
                    readMessages(); // для чтения сообщений от клиента
                } finally {
                    closeConnection(); // в конце, закрываем коннект
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // в этом метоже мы читаем сообщения и ждем сообщение аутентификации
    // по уиочанию сообщение аутентификации: /auth login password
    private void authenticate() {
        while (true) {
            try {
                String message = in.readUTF(); // в цикле читаем сообщения
                if (message.startsWith("/auth")) { // начинается сообщение аутентификации с команды "/auth"
                    // метод split() делит сообщение на массив из трех слов
                    String[] split = message.split("\\p{Blank}+");
                    String login = split[1];
                    String password = split[2];
                    // сравниваем через метод getNickByLoginAndPassword() логин и пароль  и получаем ник
                    String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) { // проверка на пользователя с созданным ником
                            sendMessage("Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage("/authok " + nick);
                        this.nick = nick;
                        server.broadcast("Пользователь " + nick + " зашел в чат");
                        server.subscribe(this);
                        break; // если пользователь успешно авторизовался, то мы выходим из бесконечного цикла через break
                    } else {
                        sendMessage("Неверный логин и пароль");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() {
        sendMessage("/end"); // отправка сообщение клиенту перед закрытием соединения
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            server.unsubscribe(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() {
        while (true) { // в бесконечном цике крутимся пока клиент не пришлет "/end"
            try {
                String message = in.readUTF(); // читаем сообщения от клиента
                if ("/end".equals(message)) {
                    break; // если присылает "/end" делаем break
                }
                if(message.startsWith("/w")){
                    String nickTo = message.split(" ")[1];
                    String mess = message.split(" ")[2];
                    server.privateMessage(this, nickTo, mess);
                    continue;
                }
                // для всех остальных сообщений вызываем метод broadcast, где рассылаем сообщения всем клиентам
                server.broadcast(nick + ": " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getNick() {
        return nick;
    }
}