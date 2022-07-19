package ru.gb.javafxchat.client;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

//класс ChatClient, в котором будет осуществляться взаимодействие с сервером
public class ChatClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final ChatController controller;

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    //метод openConnection через кот. открываем соединение
    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189); // инициализируем socket
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                waitAuth();
                readMessages();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }).start();
    }

    // метод ждет сообщение об супешной аутентификации: /authok nick
    private void waitAuth() throws IOException {
        while (true) {
            String message = in.readUTF(); // в бесконечном цикле читаем сообщения
            if (message.startsWith("/authok")) { // ждем сообщение "/authok", что аутентификация прошла успешно
                String[] split = message.split("\\p{Blank}+");
                String nick = split[1];
                controller.setAuth(true);
                controller.addMessage("Успешная авторизация под ником " + nick);
                break; // пока авторизация не успешная, крутимся в бесконечном цикле, как только прошли break
            }
        }
    }

    private void closeConnection() {
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
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readMessages() throws IOException {
        while (true) { // читаем сообщения в бесконечном цикле
            String message = in.readUTF();
            if ("/end".equals(message)) { // пока не получим "/end"
                controller.setAuth(false);
                break;
            }
            controller.addMessage(message); // отображаем прочтенные сообщения в контроллере (на форме) через метод addMessage
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}