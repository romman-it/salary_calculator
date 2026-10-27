# Salary Calculator
![Build](https://github.com/romman-it/salary_calculator/actions/workflows/build.yml/badge.svg)
Десктопное приложение на JavaFX для расчёта и учёта зарплаты экспедитора
по двум направлениям — **город Минск** и **регион** — с разными тарифными сетками.

## Возможности

- 🏙 **Город** — смены (утро/день/вечер), зона Б, надбавка после 19:00,
  отменённые заказы, сумки, часы
**Регион** — еДоставка, еМолл, почта, КБТ, отменённые заказы, сумки, часы
**Минимум 126 ₽ для региона** — если отработано > 9 часов и расчёт меньше минимума
**Бонус за стаж** — +7% (1+ год), +9% (2+ года), +12% (3+ года), для города и региона
**Юр.лица** — список за день, бонус по шкале с минимальным порогом
**Сумки** — первая на заказ бесплатна, далее 0.50
**5 тем интерфейса** — Классика(зелёный), Природа, Киберпанк, Деловой, Молодёжный
️ **Настройки тарифов** — редактируются через UI, сохраняются в JSON
**Отчёты за месяц** — таблица, итоги по городу и региону отдельно
**35 unit-тестов** — JUnit 5

## Технологии

Java 17 · JavaFX 17 · Maven · SLF4J + Log4j2 · Gson · JUnit 5 · BigDecimal

| Слой             | Стек           |
|------------------|----------------|
| Язык             | Java 17        |
| UI               | JavaFX 17      |
| Сборка           | Maven          |
| Логирование      | SLF4J + Log4j2 |
| Хранение         | JSON (Gson)    |
| Тесты            | JUnit 5        |
| Заработная плата | `BigDecimal`   |

## Архитектура
org.example.salary/
├── App.java — composition root, навигация
├── model/ — DailyRecord, Rates, LegalOrder,
│ WorkPlace, CityShift, CityInput, RegionInput
├── repository/ — SalaryRepository (интерфейс) + JSON/InMemory
├── service/ — SalaryService, RatesService
├── ui/ — MainScreen, CityScreen, RegionScreen,
│ ReportScreen, SettingsScreen,
│ LegalOrdersPanel, LegalOrderDialog
└── util/ — LocalDateAdapter (Gson)


**Ключевые решения:**
- `SalaryRepository` — интерфейс, чтобы заменить JSON на SQLite без изменений в UI и сервисах
- `BigDecimal` для всех денежных расчётов
- Конструкторная инъекция зависимостей
- Слои не знают друг о друге
- Темы UI — enum `AppTheme` + CSS в `resources`
- Собственные DTO (`CityInput`, `RegionInput`) для чистых сигнатур

## Запуск

mvn clean javafx:run
Тесты
mvn test
Покрыто: репозиторий (save/find/delete по дате и месту), тарифы (загрузка/сохранение/сброс),
расчёт города и региона (смены, надбавки, сумки, отмены, юр.лица, минимальный бонус).

Покрыто:
репозиторий: save/find/delete по (date, place), город и регион отдельно
тарифы: загрузка, сохранение, сброс
регион: формулы, минимум >9 часов, бонус за стаж
город: смены, надбавка после 19, зона Б, сумки
юр.лица: шкала ×1.60/×1.44/×1.30/×1.04/30, минимальная оплата



Структура данных

[
{
"place": "REGION",
"date": "2026-10-26",
"totalSalary": 232.30,
"edostavkaCount": 20,
"emallCount": 10,
"mailCount": 5,
"largeOrdersCount": 5,
"cancelledOrdersCount": 0,
"bagsCount": 0,
"hoursWorked": 9,
"legalPayments": 0,
"legalOrders": []
}
]

Автор

[ romman_pavlovich ]
[ Roman12S ] [ Roman Sh. Pavlovich ] ( [ https://github.com/romman-it/salary_calculator ] ) — GitHub
Скачать готовое приложение для Windows:
https://github.com/romman-it/salary_calculator/releases/tag/v3.1.1
Прямая ссылка для скачивания:
https://github.com/romman-it/salary_calculator/releases/download/v3.1.1/SalaryCalculator-win.zip
