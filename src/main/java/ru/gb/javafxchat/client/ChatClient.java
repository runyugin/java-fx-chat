package ru.gb.javafxchat.client;

import static ru.gb.javafxchat.Command.AUTHOK;
import static ru.gb.javafxchat.Command.CLIENTS;
import static ru.gb.javafxchat.Command.END;
import static ru.gb.javafxchat.Command.ERROR;
import static ru.gb.javafxchat.Command.MESSAGE;
import static ru.gb.javafxchat.Command.STOP;
import static ru.gb.javafxchat.Command.getCommand;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.application.Platform;
import ru.gb.javafxchat.Command;

public class ChatClient {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final ChatController controller;

    private Path file = Path.of("src/main/resources/ru/gb/javafxchat/history/text.txt");

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                if (waitAuth()) {
                    readMessages();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }).start();

    }

    private boolean waitAuth() throws IOException {
        while (true) {
            final String message = in.readUTF();
            final Command command = getCommand(message);
            final String[] params = command.parse(message);
            if (command == AUTHOK) { // /authok nick1
                final String nick = params[0];
                controller.setAuth(true);
                controller.addMessage(readTextForFile(file));
                controller.addMessage("Успешная авторизация под ником " + nick);
                return true;
            }
            if (command == ERROR) {
                Platform.runLater(() -> controller.showError(params[0]));
                continue;
            }
            if (command == STOP) {
                Platform.runLater(() -> controller.showError("Истекло время на авторизацию, перезапустите приложение"));
                try {
                    Thread.sleep(5000); // Без sleep пользователь не увидит сообщение об ошибке. Хочется более изящного решения, но лень его искать
                    sendMessage(END);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
    }


    private static String readTextForFile(Path path) {
        try {
            String result = "";
            List<String> strings = Files.readAllLines(path);


            List<String> stringsSub = new ArrayList<>();

            if(strings.size()<100){
                stringsSub = strings;
            } else {
                stringsSub = strings.subList(strings.size()-100, strings.size());
            }

            for (String line:stringsSub) {
                result+=line+"\n";
            }

            return result;


        } catch (IOException e) {
            throw new RuntimeException(e);
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
        System.exit(0);
    }

    private void readMessages() throws IOException {
        while (true) {
            final String message = in.readUTF();
            final Command command = getCommand(message);
            if (END == command) {
                controller.setAuth(false);
                break;
            }
            final String[] params = command.parse(message);
            if (ERROR == command) {
                String messageError = params[0];
                Platform.runLater(() -> controller.showError(messageError));
                continue;
            }
            if (MESSAGE == command) {
                Platform.runLater(() -> controller.addMessage(params[0]));
            }
            if (CLIENTS == command) {
                Platform.runLater(() -> controller.updateClientsList(params));
            }
        }
    }

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }
}
