package org.example.salary.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.salary.model.LegalOrder;
import org.example.salary.service.SalaryService;

import java.math.BigDecimal;
import java.util.List;

public class LegalOrdersPanel extends VBox {

private final SalaryService salaryService;
private final BigDecimal minBonus;
private final ObservableList<LegalOrder> orders = FXCollections.observableArrayList();
private final Label totalLabel = new Label("Итого юр.лиц: 0.00 руб.");

public LegalOrdersPanel(SalaryService salaryService, BigDecimal minBonus) {
    this.salaryService = salaryService;
    this.minBonus = minBonus;

    setSpacing(8);

    Label title = new Label("Юр.лица за день");
    title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

    ListView<LegalOrder> listView = new ListView<>(orders);
    listView.setPrefHeight(120);
    listView.setCellFactory(lv -> new ListCell<>() {
        @Override
        protected void updateItem(LegalOrder item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                Label text = new Label(item.toString());
                Button remove = new Button("✖");
                remove.setOnAction(e -> {
                    orders.remove(item);
                    updateTotal();
                });
                HBox.setHgrow(text, Priority.ALWAYS);
                HBox cell = new HBox(8, text, remove);
                cell.setPadding(new Insets(2, 4, 2, 4));
                setGraphic(cell);
            }
        }
    });

    Button addButton = new Button("+ Добавить юр.лицо");
    addButton.setOnAction(e -> {
        addLegalOrder dialog = new addLegalOrder(salaryService, minBonus);
        dialog.showAndReturn().ifPresent(order -> {
            orders.add(order);
            updateTotal();
        });
    });

    totalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f1c40f;");

    getChildren().addAll(title, listView, addButton, totalLabel);
}

public void updateMinBonus(BigDecimal newMinBonus) {
}

public List<LegalOrder> getOrders() {
    return List.copyOf(orders);
}

private void updateTotal() {
    BigDecimal total = orders.stream()
            .map(LegalOrder::getBonus)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    totalLabel.setText(String.format("Итого юр.лиц: %.2f руб.", total));
}
}