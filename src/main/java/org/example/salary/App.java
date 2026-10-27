package org.example.salary;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.salary.model.AppTheme;
import org.example.salary.repository.JsonSalaryRepository;
import org.example.salary.repository.SalaryRepository;
import org.example.salary.service.RatesService;
import org.example.salary.service.SalaryService;
import org.example.salary.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends Application {

private static final Logger logger = LoggerFactory.getLogger(App.class);

private Stage stage;
private RatesService ratesService;
private SalaryService salaryService;
private SalaryRepository repository;

@Override
public void start(Stage stage) {
    this.stage = stage;
    this.ratesService = new RatesService();
    this.repository = new JsonSalaryRepository();
    this.salaryService = new SalaryService(ratesService);
    showMainScreen();
    logger.info("Приложение запущено");
}

private Scene createScene(Parent root, double w, double h) {
    Scene scene = new Scene(root, w, h);
    AppTheme theme = AppTheme.fromName(ratesService.getRates().getUiTheme());
    var css = getClass().getResource(theme.getCssPath());
    if (css != null) {
        scene.getStylesheets().add(css.toExternalForm());
    } else {
        logger.warn("CSS не найден: {}", theme.getCssPath());
    }
    return scene;
}

private void showMainScreen() {
    MainScreen main = new MainScreen(
            this::showCityScreen,
            this::showRegionScreen,
            this::showReportScreen,
            this::showSettingsScreen
    );
    stage.setScene(createScene(main, 550, 800));
    stage.setTitle("ма e - Babki");
    stage.show();
}

private void showReportScreen() {
    ReportScreen report = new ReportScreen(repository, this::showMainScreen);
    stage.setScene(createScene(report, 550, 800));
    stage.setTitle("ма e - Babki — Отчёт");
}

private void showCityScreen() {
    CityScreen city = new CityScreen(salaryService, repository, this::showMainScreen);
    stage.setScene(createScene(city, 550, 800));
    stage.setTitle("ма e - Babki — Город");
}

private void showRegionScreen() {
    RegionMenu region = new RegionMenu(salaryService, repository, this::showMainScreen);
    stage.setScene(createScene(region, 550, 800));
    stage.setTitle("ма e - Babki — Регион");
}

private void showSettingsScreen() {
    SettingsScreen s = new SettingsScreen(ratesService, this::showMainScreen);
    stage.setScene(createScene(s, 550, 800));
    stage.setTitle("ма e - Babki — Настройки");
}

public static void main(String[] args) {
    launch(args);
}
}