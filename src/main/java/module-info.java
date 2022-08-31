module ru.gb.javafxchat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    exports ru.gb.javafxchat.client;
    opens ru.gb.javafxchat.client to javafx.fxml;
}