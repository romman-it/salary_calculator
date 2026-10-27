package org.example.salary.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.example.salary.model.DailyRecord;
import org.example.salary.model.LegalOrder;
import org.example.salary.model.RegionInput;
import org.example.salary.model.WorkDirection;
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

public class RegionMenu extends VBox {

private static final Logger logger = LoggerFactory.getLogger(RegionMenu.class);
private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

private final SalaryService salaryService;
private final SalaryRepository repository;
private final Runnable onBack;

private final TextField openField = new TextField("07:00");
private final TextField closeField = new TextField("20:00");
private final TextField edostavkaField = new TextField();
private final TextField emallField = new TextField();
private final TextField mailField = new TextField();
private final TextField largeOrdersField = new TextField();
private final TextField cancelledField = new TextField();
private final TextField bagsField = new TextField();

private final Label resultLabel = new Label("Результат: —");
private final LegalOrdersPanel legalPanel;

public RegionMenu(SalaryService salaryService,
                  SalaryRepository repository,
                  Runnable onBack) {
    this.salaryService = salaryService;
    this.repository = repository;
    this.onBack = onBack;

    setSpacing(14);
    setPadding(new Insets(20));
    setAlignment(Pos.TOP_CENTER);

    Label title = new Label("Регион — расчёт за " + LocalDate.now());
    title.getStyleClass().add("title");

    GridPane form = buildForm();

    BigDecimal minBonus = salaryService.getRates().getRegionMinLegalBonus();
    legalPanel = new LegalOrdersPanel(salaryService, minBonus);

    Button calcAndSave = new Button("Рассчитать и сохранить");
    calcAndSave.getStyleClass().add("button-primary");
    calcAndSave.setMaxWidth(Double.MAX_VALUE);
    calcAndSave.setPrefHeight(42);
    calcAndSave.setOnAction(e -> onCalculateAndSave());

    resultLabel.getStyleClass().add("result-success");

    Button back = new Button("< Назад");
    back.getStyleClass().add("button-ghost");
    back.setOnAction(e -> onBack.run());

    getChildren().addAll(title, form, legalPanel, calcAndSave, resultLabel, back);
}

private GridPane buildForm() {
    GridPane grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(10);

    int row = 0;
    row = addRow(grid, row, "Время открытия путевого (HH:mm):", openField);
    row = addRow(grid, row, "Время прибытия из рейса (HH:mm):", closeField);
    row = addRow(grid, row, "еДоставка, шт:", edostavkaField);
    row = addRow(grid, row, "еМолл, шт:", emallField);
    row = addRow(grid, row, "Почта, шт:", mailField);
    row = addRow(grid, row, "КБТ / большие, шт:", largeOrdersField);
    row = addRow(grid, row, "Отменённые заказы, шт:", cancelledField);
    addRow(grid, row, "Сумки (всего):", bagsField);

    return grid;
}

private int addRow(GridPane grid, int row, String label, TextField field) {
    grid.add(new Label(label), 0, row);
    field.setPrefWidth(120);
    grid.add(field, 1, row);
    return row + 1;
}

private void onCalculateAndSave() {
    try {
        LocalTime open = parseTime(openField);
        LocalTime close = parseTime(closeField);

        RegionInput input = new RegionInput(
                open, close,
                parseInt(edostavkaField),
                parseInt(emallField),
                parseInt(mailField),
                parseInt(largeOrdersField),
                parseInt(cancelledField),
                parseInt(bagsField),
                legalPanel.getOrders()
        );

        BigDecimal total = salaryService.calculateRegion(input);

        if (total.signum() == 0) {
            showError("Сумма равна 0 — нечего сохранять");
            return;
        }

        LocalDate today = LocalDate.now();
        var existing = repository.findByDate(today, WorkDirection.REGION);
        if (existing.isPresent()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Запись за сегодня (регион) уже есть");
            confirm.setContentText("Текущая сумма: " + existing.get().getTotalSalary()
                                           + " руб. Заменить?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        DailyRecord record = new DailyRecord();
        record.setPlace(WorkDirection.REGION);
        record.setDate(today);
        record.setTotalSalary(total);
        record.setEdostavkaCount(input.edostavkaCount());
        record.setEmallCount(input.emallCount());
        record.setMailCount(input.mailCount());
        record.setLargeOrdersCount(input.largeOrdersCount());
        record.setCancelledOrdersCount(input.cancelledOrders());
        record.setBagsCount(input.bagsCount());
        record.setHoursWorked(hoursBetween(open, close));
        record.setLegalPayments(sumBonuses(input.legalOrders()));
        record.setLegalOrders(input.legalOrders());

        repository.save(record);

        resultLabel.setText(String.format("Маладэц!! : %.2f руб. за %s", total, today));
        logger.info("Регион за {}: {} руб.", today, total);

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

private LocalTime parseTime(TextField field) {
    return LocalTime.parse(field.getText().trim().replace('.', ':'), TIME_FMT);
}

private int parseInt(TextField field) {
    String text = field.getText().trim();
    if (text.isEmpty()) return 0;
    return Integer.parseInt(text);
}

private void showError(String msg) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setHeaderText("Ошибка");
    alert.setContentText(msg);
    alert.showAndWait();
}
}