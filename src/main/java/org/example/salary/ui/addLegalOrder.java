package org.example.salary.ui;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.salary.model.LegalOrder;
import org.example.salary.service.SalaryService;

import java.math.BigDecimal;
import java.util.Optional;

public class addLegalOrder extends Dialog<LegalOrder> {

public addLegalOrder(SalaryService salaryService, BigDecimal minCost) {
    setTitle("Добавить юр.лицо");
    setHeaderText("Введите стоимость заказа. Минимальный бонус: " + minCost + " руб.");

    ButtonType addButton = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
    getDialogPane().getButtonTypes().addAll(addButton, cancelButton);

    TextField costField = new TextField();
    costField.setPromptText("Стоимость заказа, руб.");

    Label bonusLabel = new Label("Оплата: —");
    bonusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71;");

    // пересчёт при вводе
    costField.textProperty().addListener((obs, oldVal, newVal) -> {
        try {
            BigDecimal cost = parseMoney(newVal);
            if (cost != null && cost.signum() >= 0) {
                BigDecimal bonus = salaryService.calculateLegalBonusWithMin(cost, minCost);
                bonusLabel.setText("Оплата: " + bonus + " руб.");
            } else {
                bonusLabel.setText("Оплата: —");
            }
        } catch (Exception e) {
            bonusLabel.setText("Оплата: —");
        }
    });

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20));
    grid.add(new Label("Стоимость:"), 0, 0);
    grid.add(costField, 1, 0);
    grid.add(bonusLabel, 0, 1, 2, 1);

    getDialogPane().setContent(grid);

    Node addNode = getDialogPane().lookupButton(addButton);
    addNode.setDisable(true);
    costField.textProperty().addListener((obs, oldVal, newVal) -> {
        BigDecimal cost = parseMoney(newVal);
        addNode.setDisable(cost == null || cost.signum() < 0);
    });

    setResultConverter(button -> {
        if (button == addButton) {
            BigDecimal cost = parseMoney(costField.getText());
            BigDecimal bonus = salaryService.calculateLegalBonusWithMin(cost, minCost);
            return new LegalOrder(cost, bonus);
        }
        return null;
    });
}

public Optional<LegalOrder> showAndReturn() {
    return showAndWait();
}

private BigDecimal parseMoney(String text) {
    if (text == null) return null;
    String clean = text.trim().replace(',', '.');
    if (clean.isEmpty()) return null;
    try {
        return new BigDecimal(clean).setScale(2, java.math.RoundingMode.HALF_UP);
    } catch (NumberFormatException e) {
        return null;
    }
}
}