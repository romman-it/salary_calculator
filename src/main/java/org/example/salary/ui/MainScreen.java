package org.example.salary.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainScreen extends VBox {

private static final Logger logger = LoggerFactory.getLogger(MainScreen.class);

public MainScreen(Runnable onCity,
                  Runnable onRegion,
                  Runnable onReport,
                  Runnable onSettings) {

    setSpacing(14);
    setPadding(new Insets(30));
    setAlignment(Pos.CENTER);

    Label title = new Label("ма e - Babki");
    title.getStyleClass().add("title");

    Label subtitle = new Label("куда сегодня?");
    subtitle.getStyleClass().add("subtitle");

    Button cityButton = makeButton("ГОРОД", onCity);
    cityButton.getStyleClass().add("button-primary");

    Button regionButton = makeButton("РЕГИОН", onRegion);
    regionButton.getStyleClass().add("button-primary");

    Button reportButton = makeButton("Отчёты за месяц", onReport);

    Button settingsButton = makeButton("Настройки", onSettings);

    Button exitButton = makeButton("Выход", () -> {
        logger.info("Выход из приложения");
        Platform.exit();
    });
    exitButton.getStyleClass().add("button-danger");
    String mess = "";
    Label messageLabel = new Label(mess);
    messageLabel.getStyleClass().add("subtitle");


    getChildren().addAll(title, subtitle,
                         cityButton, regionButton, reportButton, settingsButton, exitButton, messageLabel);
}

private Button makeButton(String text, Runnable action) {
    Button b = new Button(text);
    b.setMaxWidth(Double.MAX_VALUE);
    b.setPrefHeight(42);
    b.setOnAction(e -> action.run());
    return b;
}
}