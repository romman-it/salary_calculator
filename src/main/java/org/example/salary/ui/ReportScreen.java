package org.example.salary.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.salary.model.DailyRecord;
import org.example.salary.model.WorkDirection;
import org.example.salary.repository.SalaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportScreen extends VBox {

private static final Logger logger = LoggerFactory.getLogger(ReportScreen.class);
private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MM.yyyy");

private final SalaryRepository repository;
private final Runnable onBack;

private final TableView<DailyRecord> table = new TableView<>();
private final Label monthLabel = new Label();
private final Label totalLabel = new Label();
private final Label cityLabel = new Label();
private final Label regionLabel = new Label();
private final Label daysLabel = new Label();

private YearMonth currentMonth = YearMonth.now();

public ReportScreen(SalaryRepository repository, Runnable onBack) {
    this.repository = repository;
    this.onBack = onBack;

    setSpacing(12);
    setPadding(new Insets(20));
    setAlignment(Pos.TOP_CENTER);

    Label title = new Label("з/п за месяц");
    title.getStyleClass().add("title");

    Button prev = new Button("◀");
    Button next = new Button("▶");
    prev.setOnAction(e -> changeMonth(-1));
    next.setOnAction(e -> changeMonth(1));

    monthLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

    HBox monthRow = new HBox(10, prev, monthLabel, next);
    monthRow.setAlignment(Pos.CENTER);

    buildTable();

    totalLabel.getStyleClass().add("result-total");
    cityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ecc71;");
    regionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3498db;");
    daysLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #bdc3c7;");

    Button back = new Button("◀ Назад");
    back.getStyleClass().add("button-ghost");
    back.setOnAction(e -> onBack.run());

    getChildren().addAll(title, monthRow, table,
                         totalLabel, cityLabel, regionLabel, daysLabel, back);

    refresh();
}

private void buildTable() {
    table.setPrefHeight(320);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    TableColumn<DailyRecord, String> dateCol = new TableColumn<>("Дата");
    dateCol.setCellValueFactory(c ->
                                        new SimpleStringProperty(c.getValue().getDate().format(DAY_FMT)));

    TableColumn<DailyRecord, String> placeCol = new TableColumn<>("Направление");
    placeCol.setCellValueFactory(c ->
                                         new SimpleStringProperty(c.getValue().getPlace() != null
                                                                          ? c.getValue().getPlace().getDisplayName() : "—"));

    TableColumn<DailyRecord, BigDecimal> salaryCol = new TableColumn<>("Зарплата");
    salaryCol.setCellValueFactory(c ->
                                          new SimpleObjectProperty<>(c.getValue().getTotalSalary()));

    TableColumn<DailyRecord, Integer> hoursCol = new TableColumn<>("Часы");
    hoursCol.setCellValueFactory(c ->
                                         new SimpleObjectProperty<>(c.getValue().getHoursWorked()));

    TableColumn<DailyRecord, Integer> legalCol = new TableColumn<>("Юр.лица");
    legalCol.setCellValueFactory(c ->
                                         new SimpleObjectProperty<>(
                                                 c.getValue().getLegalOrders() != null
                                                         ? c.getValue().getLegalOrders().size() : 0));

    table.getColumns().addAll(dateCol, placeCol, salaryCol, hoursCol, legalCol);
}

private void changeMonth(int delta) {
    currentMonth = currentMonth.plusMonths(delta);
    refresh();
}

private void refresh() {
    monthLabel.setText("Месяц: " + currentMonth.format(MONTH_FMT));

    List<DailyRecord> records = repository.findByMonth(currentMonth);
    table.getItems().setAll(records);

    BigDecimal total = sum(records);
    BigDecimal citySum = sum(records.stream()
                                     .filter(r -> r.getPlace() == WorkDirection.CITY).toList());
    BigDecimal regionSum = sum(records.stream()
                                       .filter(r -> r.getPlace() == WorkDirection.REGION).toList());

    long days = records.stream()
            .map(DailyRecord::getDate)
            .distinct()
            .count();

    BigDecimal avg = days == 0
            ? BigDecimal.ZERO
            : total.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);

    totalLabel.setText(String.format("Итого за месяц: %.2f руб.", total));
    cityLabel.setText(String.format("Город: %.2f руб.", citySum));
    regionLabel.setText(String.format("Регион: %.2f руб.", regionSum));
    daysLabel.setText(String.format("Рабочих дней: %d | Средняя за день: %.2f руб.",
                                    days, avg));

    logger.debug("Отчёт за {}: {} записей, итого {}", currentMonth, records.size(), total);
}

private BigDecimal sum(List<DailyRecord> records) {
    return records.stream()
            .map(DailyRecord::getTotalSalary)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}
}