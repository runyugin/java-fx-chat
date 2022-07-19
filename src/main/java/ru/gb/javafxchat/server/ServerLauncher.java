package ru.gb.javafxchat.server;

// класс ServerLauncher запускает сервер
// он создает ChatServer и вызывает метод run()
// в методе run() мы создаем серверный сокет и ждем подключение клиента
public class ServerLauncher {
    public static void main(String[] args) {
        new ChatServer().run();
    }
}