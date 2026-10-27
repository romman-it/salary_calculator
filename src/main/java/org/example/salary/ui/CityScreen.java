package org.example.salary.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.example.salary.model.*;
import org.example.salary.repository.SalaryRepository;
import org.example.salary.service.SalaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class CityScreen extends VBox {

private static final Logger logger = LoggerFactory.getLogger(CityScreen.class);
private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("ЧЧ:мм");

private final SalaryService salaryService;
private final SalaryRepository repository;
private final Runnable onBack;

private final TextField openField = new TextField("09:00");
private final TextField closeField = new TextField("18:00");
private final TextField cityOrdersField = new TextField();
private final TextField after19Field = new TextField();
private final TextField zoneBField = new TextField();
private final TextField cancelledField = new TextField();
private final TextField bagsField = new TextField();

private final Label shiftLabel = new Label("Смена: —");
private final Label resultLabel = new Label("Результат: —");
private final LegalOrdersPanel legalPanel;

public CityScreen(SalaryService salaryService,
                  SalaryRepository repository,
                  Runnable onBack) {
    this.salaryService = salaryService;
    this.repository = repository;
    this.onBack = onBack;

    setSpacing(14);
    setPadding(new Insets(20));
    setAlignment(Pos.TOP_CENTER);

    Label title = new Label("Город — расчёт за " + LocalDate.now());
    title.getStyleClass().add("title");

    shiftLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

    openField.textProperty().addListener((obs, o, n) -> updateShift());

    GridPane form = buildForm();

    BigDecimal minBonus = salaryService.getRates().getCityDayRate();
    legalPanel = new LegalOrdersPanel(salaryService, minBonus);

    Button calcAndSave = new Button("Рассчитать и сохранить");
    calcAndSave.getStyleClass().add("button-primary");
    calcAndSave.setMaxWidth(Double.MAX_VALUE);
    calcAndSave.setPrefHeight(42);
    calcAndSave.setOnAction(e -> onCalculateAndSave());

    resultLabel.getStyleClass().add("result-success");

    Button back = new Button("◀ Назад");
    back.getStyleClass().add("button-ghost");
    back.setOnAction(e -> onBack.run());

    getChildren().addAll(title, form, shiftLabel, legalPanel, calcAndSave, resultLabel, back);

    updateShift();
}

private GridPane buildForm() {
    GridPane grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(10);

    int row = 0;
    row = addRow(grid, row, "Открытие путевого (HH:mm):", openField);
    row = addRow(grid, row, "Прибытие из рейса (HH:mm):", closeField);
    row = addRow(grid, row, "Заказы по городу:", cityOrdersField);
    row = addRow(grid, row, "Из них после 19:00:", after19Field);
    row = addRow(grid, row, "Заказы в зоне Б:", zoneBField);
    row = addRow(grid, row, "Отменённые заказы:", cancelledField);
    addRow(grid, row, "Сумки (всего):", bagsField);
    return grid;
}

private int addRow(GridPane grid, int row, String label, TextField field) {
    grid.add(new Label(label), 0, row);
    field.setPrefWidth(120);
    grid.add(field, 1, row);
    return row + 1;
}

private void updateShift() {
    try {
        LocalTime open = LocalTime.parse(openField.getText().trim().replace('.', ':'), TIME_FMT);
        CitySett shift = CitySett.fromOpenTime(open);
        shiftLabel.setText("Смена: " + shift + " (ставка за заказ: "
                                   + getRateForShift(shift) + " руб.)");
    } catch (DateTimeParseException ex) {
        shiftLabel.setText("Смена: — (введите время в формате чч:мм)");
    }
}

private BigDecimal getRateForShift(CitySett shift) {
    Rates r = salaryService.getRates();
    return switch (shift) {
        case MORNING -> r.getCityMorningRate();
        case DAY -> r.getCityDayRate();
        case EVENING -> r.getCityEveningRate();
    };
}

private void onCalculateAndSave() {
    try {
        LocalTime open = LocalTime.parse(openField.getText().trim().replace('.', ':'), TIME_FMT);
        LocalTime close = LocalTime.parse(closeField.getText().trim().replace('.', ':'), TIME_FMT);

        CityInput input = new CityInput(
                open, close,
                parseInt(cityOrdersField),
                parseInt(after19Field),
                parseInt(zoneBField),
                parseInt(cancelledField),
                parseInt(bagsField),
                legalPanel.getOrders()
        );

        BigDecimal total = salaryService.calculateCity(input);

        if (total.signum() == 0) {
            showError("Сумма равна 0 — нечего сохранять");
            return;
        }

        LocalDate today = LocalDate.now();
        var existing = repository.findByDate(today, WorkDirection.CITY);
        if (existing.isPresent()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Запись за сегодня (город) уже есть");
            confirm.setContentText("Сумма: " + existing.get().getTotalSalary() + " руб. Заменить?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        DailyRecord record = new DailyRecord();
        record.setPlace(WorkDirection.CITY);
        record.setDate(today);
        record.setTotalSalary(total);
        record.setEdostavkaCount(input.cityOrders());
        record.setCancelledOrdersCount(input.cancelledOrders());
        record.setBagsCount(input.bagsCount());
        record.setHoursWorked(hoursBetween(open, close));
        record.setLegalPayments(sumBonuses(input.legalOrders()));
        record.setLegalOrders(input.legalOrders());

        repository.save(record);

        resultLabel.setText(String.format("Сохранено: %.2f руб. за %s", total, today));
        logger.info("Город за {}: {} руб.", today, total);

    } catch (NumberFormatException e) {
        showError("Введите корректные числа");
    } catch (DateTimeParseException e) {
        showError("Время в формате ЧЧ:мм (например, 09:00)");
    } catch (IllegalArgumentException e) {
        showError(e.getMessage());
    }
}

private BigDecimal sumBonuses(List<LegalOrder> orders) {
    return orders.stream()
            .map(LegalOrder::getBonus)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}

private int hoursBetween(LocalTime open, LocalTime close) {
    var d = java.time.Duration.between(open, close);
    if (d.isNegative()) d = d.plusDays(1);
    return (int) d.toHours();
}

private int parseInt(TextField f) {
    String t = f.getText().trim();
    if (t.isEmpty()) return 0;
    return Integer.parseInt(t);
}

private void showError(String msg) {
    Alert a = new Alert(Alert.AlertType.ERROR);
    a.setHeaderText("Ошибка");
    a.setContentText(msg);
    a.showAndWait();
}
}