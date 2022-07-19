package ru.gb.javafxchat.server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final List<ClientHandler> clients; // тут хранятся те клиенты, кот. подключились к серверу и прошли аутентификацию (передали логин и пароль)

    public ChatServer() {
        this.clients = new ArrayList<>();
    }

    // в методе run() мы создаем серверный сокет и ждем подключение клиента
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new InMemoryAuthService()) {
            while (true) {
                System.out.println("Ожидаю подключения...");
                Socket socket = serverSocket.accept();
                // как только клиент подключился создаем экземпляр ClientHandler
                // каждый экземпляр отвечает за соединение с одним клиентом
                // для каждого клиента создается отдельный socket
                // передаем в него socket и ссылку на наш чат сервер ChatServer через this
                new ClientHandler(socket, this, authService);
                System.out.println("Клиент подключен");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) { // в цикле, для каждого клиента вызываем sendMessage()
            client.sendMessage(message);
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client); // берем список клиентов и добавляем туда, того, кто только что залогинился
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) { // пробегаемся по всем клиентам, если найден клиент с таким же ником возвращаем true, в противном случае false
            if (nick.equals(client.getNick())) {
                return true;
            }
        }
        return false;
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client); // если пользователь вышел, убираем его из списка клиентов
    }

    public void privateMessage(ClientHandler from, String nickTo, String message) {
        for (ClientHandler client : clients){
            if (client.getNick().equals(nickTo)) {
                client.sendMessage("Сообщение от " + from.getNick() + ": " + message);
                break;
            }
        }
        from.sendMessage("Сообщение для " + nickTo + ": " + message);
    }
}