package ru.gb.javafxchat.server;

import java.io.Closeable;

// простой сервис аутентификации, который в памяти хранит заданные логины и пароли
public interface AuthService extends Closeable {
    String getNickByLoginAndPassword(String login, String password);
}