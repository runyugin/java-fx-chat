package ru.gb.javafxchat.client;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;

public class ChatController {
    @FXML
    private HBox authBox;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passField;
    @FXML
    private VBox messageBox;
    @FXML
    private TextArea messageArea;
    @FXML
    private TextField messageField;

    private final ChatClient client;

    public ChatController() {
        // создается экземпляр ChatClient с ссылкой this на самого себя
        this.client = new ChatClient(this);
        while (true) {
            try {
                client.openConnection(); // подключаемся
                break; // если соединение прошло успешно, то break
            } catch (IOException e) {
                showNotification(); // if не успешно и было исключение, то методом showNotification сообщаем об этом
            }
        }
    }

    private void showNotification() {
        Alert alert = new Alert(Alert.AlertType.ERROR,
                "Не могу подключиться к серверу.\n" +
                        "Проверьте, что сервер запущен и доступен",
                new ButtonType("Попоробовать снова", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        alert.setTitle("Ошибка подключения!"); // прописываем заголовок
        Optional<ButtonType> answer = alert.showAndWait(); // показываем пользователю
        // чтобы сообщение получить, вызывается map(), в кот. передается лямбда
        // та кнопка, на кот. пользователь нажал будет лежать в select
        // если нажата кнопка отиены (isCancelButton()), то true, в противном случае (.orElse) - false
        // т.е если пользователь хочет выйти - true, если нет - false
        Boolean isExit = answer.map(select -> select.getButtonData().isCancelButton()).orElse(false);
        if(isExit){ // если пользователь хочет выйти
            System.exit(0); // выходим из системы
        }
    }

    // кнопка для отправки сообщений
    public void clickSendButton() {
        String message = messageField.getText(); // getText() возвращает тот текст, который будет введен
        if (message.isBlank()) { // isBlank() когда ответ пустой и когда вместо ответа пробелы
            return; // return тут нужен, чтобы досрочно выйти из метода
        }

        client.sendMessage(message);
        messageField.clear(); // очищаем поле userAnswer
        messageField.requestFocus(); // устанавливаем на него фокус, чтобы был курсор
    }

    // метод addMessage добавляет сообщения пользователей в форму
    public void addMessage(String message) {
        messageArea.appendText(message + "\n"); // метод .appendText() передает текст
    }

    // метод, в кот. передаем признак успешной аутентификации
    // мы создали метод и положили в него true, поэтому пока false - отображается поле с паролем
    // после регистрации - поле с чатом
    public void setAuth(boolean success){
        authBox.setVisible(!success); // до аутентификации, бокс с паолем и логином
        messageBox.setVisible(success); // после аутентификации показываем бокс для сообщений
    }

    public void signinBtnClick() { // когда пользователь нажимает на кнопку
        // отправляется сообщение на утентификацию
        client.sendMessage("/auth " + loginField.getText() + " " + passField.getText());
    }
}