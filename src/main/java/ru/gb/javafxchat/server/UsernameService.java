package ru.gb.javafxchat.server;

import java.io.Closeable;

public interface UsernameService extends Closeable {

    void updateUsername(String login, String newUsername);

}
