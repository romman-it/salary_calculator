package org.example.salary.ui;
import javafx.scene.control.DatePicker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.salary.model.AppTheme;
import org.example.salary.model.Rates;
import org.example.salary.service.RatesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.math.BigDecimal;
import java.math.RoundingMode;

public class SettingsScreen extends VBox {

private static final Logger logger = LoggerFactory.getLogger(SettingsScreen.class);
private final ComboBox<AppTheme> themeBox = new ComboBox<>();
private final RatesService ratesService;
private final Runnable onBack;

// =========================================================Регион=====================================
private final TextField regionMinimumField = new TextField();
private final TextField orderField = new TextField();
private final TextField mainField = new TextField();
private final TextField grandOrderField = new TextField();
private final TextField hoursField = new TextField();
private final TextField bagField = new TextField();
private final TextField cancelledField = new TextField();
private final TextField regionMinLegalField = new TextField();

//============================================================== Город========================================
private final TextField cityMorningField = new TextField();
private final TextField cityDayField = new TextField();
private final TextField cityEveningField = new TextField();
private final TextField cityZoneBField = new TextField();
private final TextField cityAfter19Field = new TextField();

private final DatePicker hireDatePicker = new DatePicker();
private final TextField seniority1Field = new TextField();
private final TextField seniority2Field = new TextField();
private final TextField seniority3Field = new TextField();

public SettingsScreen(RatesService ratesService, Runnable onBack) {
    this.ratesService = ratesService;
    this.onBack = onBack;

    setSpacing(12);
    setPadding(new Insets(20));
    setAlignment(Pos.TOP_CENTER);

    Label title = new Label("Настройки");
    title.getStyleClass().add("title");

    Label hint = new Label("Значения сохраняются в data/rates.json");
    hint.getStyleClass().add("subtitle");
    VBox content = new VBox(20);
    content.setPadding(new Insets(10));
    content.getChildren().addAll(buildThemeSection(), buildRegionSection(), buildCitySection());

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setPrefHeight(520);
    scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

    Button save = new Button("Сохранить");
    save.getStyleClass().add("button-primary");
    save.setOnAction(e -> onSave());

    Button reset = new Button("Сбросить");
    reset.setOnAction(e -> onReset());

    Button back = new Button("◀ Назад");
    back.getStyleClass().add("button-ghost");
    back.setOnAction(e -> onBack.run());

    HBox buttons = new HBox(10, save, reset);
    buttons.setAlignment(Pos.CENTER);

    getChildren().addAll(title, hint, scroll, buttons, back);
    themeBox.getItems().setAll(AppTheme.values());
    themeBox.setPrefWidth(200);
    fillFromRates(ratesService.getRates());
}

private VBox buildRegionSection() {
    Label header = new Label("Регион");
    header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3498db;");

    GridPane grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(8);
    int row = 0;
    row = addRow(grid, row, "еДоставка / еМолл (за заказ):", orderField);
    row = addRow(grid, row, "Почта:", mainField);
    row = addRow(grid, row, "КБТ / большой заказ:", grandOrderField);
    row = addRow(grid, row, "Час работы:", hoursField);
    row = addRow(grid, row, "Сумка:", bagField);
    row = addRow(grid, row, "Отменённый заказ:", cancelledField);
    row = addRow(grid, row, "Мин. бонус юр.лица:", regionMinLegalField);
    row = addRow(grid, row, "Минимальная зарплата за день:", regionMinimumField);
    grid.add(new Label("Дата приёма на работу:"), 0, row);
    hireDatePicker.setPrefWidth(150);
    grid.add(hireDatePicker, 1, row++);

    row = addRow(grid, row, "Бонус за 1+ год, %:", seniority1Field);
    row = addRow(grid, row, "Бонус за 2+ года, %:", seniority2Field);
    addRow(grid, row, "Бонус за 3+ года, %:", seniority3Field);
    return new VBox(8, header, grid);
}
private VBox buildThemeSection() {
    Label header = new Label("Тема");
    header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");
    return new VBox(8, header, themeBox);
}
private VBox buildCitySection() {
    Label header = new Label("Город");
    header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");

    GridPane grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(8);
    int row = 0;
    row = addRow(grid, row, "Утренняя смена (до 09:00):", cityMorningField);
    row = addRow(grid, row, "Дневная смена (09:00–15:00):", cityDayField);
    row = addRow(grid, row, "Вечерняя смена (после 15:00):", cityEveningField);
    row = addRow(grid, row, "Зона Б (за заказ):", cityZoneBField);
    addRow(grid, row, "Надбавка после 19:00:", cityAfter19Field);

    return new VBox(8, header, grid);
}

private int addRow(GridPane grid, int row, String label, TextField field) {
    grid.add(new Label(label), 0, row);
    field.setPrefWidth(120);
    grid.add(field, 1, row);
    return row + 1;
}

private void fillFromRates(Rates r) {
    orderField.setText(r.getPaymentForOrder().toPlainString());
    mainField.setText(r.getPaymentForMail().toPlainString());
    grandOrderField.setText(r.getPaymentForGrandOrder().toPlainString());
    hoursField.setText(r.getPaymentForHours().toPlainString());
    bagField.setText(r.getPaymentForOneDag().toPlainString());
    cancelledField.setText(r.getPaymentForCancelled().toPlainString());
    regionMinLegalField.setText(r.getRegionMinLegalBonus().toPlainString());
    regionMinimumField.setText(r.getRegionMinimumDaily().toPlainString());
    cityMorningField.setText(r.getCityMorningRate().toPlainString());
    cityDayField.setText(r.getCityDayRate().toPlainString());
    cityEveningField.setText(r.getCityEveningRate().toPlainString());
    cityZoneBField.setText(r.getCityZoneBRate().toPlainString());
    cityAfter19Field.setText(r.getCityAfter19Surcharge().toPlainString());
    hireDatePicker.setValue(r.getHireDate());
    seniority1Field.setText(r.getSeniorityBonus1Year().toPlainString());
    seniority2Field.setText(r.getSeniorityBonus2Years().toPlainString());
    seniority3Field.setText(r.getSeniorityBonus3Years().toPlainString());
    themeBox.setValue(AppTheme.fromName(r.getUiTheme()));
}

private void onSave() {
    try {
        Rates r = ratesService.getRates();
        r.setPaymentForOrder(parseMoney(orderField));
        r.setPaymentForMail(parseMoney(mainField));
        r.setPaymentForGrandOrder(parseMoney(grandOrderField));
        r.setPaymentForHours(parseMoney(hoursField));
        r.setPaymentForOneDag(parseMoney(bagField));
        r.setPaymentForCancelled(parseMoney(cancelledField));
        r.setRegionMinLegalBonus(parseMoney(regionMinLegalField));
        r.setRegionMinimumDaily(parseMoney(regionMinimumField));

        r.setCityMorningRate(parseMoney(cityMorningField));
        r.setCityDayRate(parseMoney(cityDayField));
        r.setCityEveningRate(parseMoney(cityEveningField));
        r.setCityZoneBRate(parseMoney(cityZoneBField));
        r.setCityAfter19Surcharge(parseMoney(cityAfter19Field));
        r.setHireDate(hireDatePicker.getValue());
        r.setSeniorityBonus1Year(parseMoney(seniority1Field));
        r.setSeniorityBonus2Years(parseMoney(seniority2Field));
        r.setSeniorityBonus3Years(parseMoney(seniority3Field));

        AppTheme selected = themeBox.getValue();
        if (selected != null) {
            r.setUiTheme(selected.name());
        }

        ratesService.updateRates(r);
        showInfo("Тарифы сохранены. Тема применена.");
        onBack.run();
        logger.info("Пользователь обновил тарифы");
    } catch (IllegalArgumentException e) {
        showError(e.getMessage());
    }
}

private void onReset() {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setHeaderText("Сбросить тарифы?");
    confirm.setContentText("Все текущие значения будут заменены значениями по умолчанию.");
    if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
    ratesService.resetToDefaults();
    fillFromRates(ratesService.getRates());
    showInfo("Сброшено");
}

private BigDecimal parseMoney(TextField f) {
    String t = f.getText().trim().replace(',', '.');
    if (t.isEmpty()) throw new IllegalArgumentException("Все поля должны быть заполнены");
    BigDecimal v;
    try { v = new BigDecimal(t); }
    catch (NumberFormatException e) { throw new IllegalArgumentException("Неверное число: " + t); }
    if (v.signum() < 0) throw new IllegalArgumentException("Тариф не может быть отрицательным");
    return v.setScale(2, RoundingMode.HALF_UP);
}

private void showInfo(String msg) {
    Alert a = new Alert(Alert.AlertType.INFORMATION);
    a.setHeaderText(null);
    a.setContentText(msg);
    a.showAndWait();
}

private void showError(String msg) {
    Alert a = new Alert(Alert.AlertType.ERROR);
    a.setHeaderText("Ошибка");
    a.setContentText(msg);
    a.showAndWait();
}
}