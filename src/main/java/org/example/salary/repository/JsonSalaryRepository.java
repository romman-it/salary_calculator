package org.example.salary.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.salary.model.DailyRecord;
import org.example.salary.model.WorkDirection;
import org.example.salary.util.LocalDateAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class JsonSalaryRepository implements SalaryRepository {

private static final Logger logger = LoggerFactory.getLogger(JsonSalaryRepository.class);
private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
private static final Type LIST_TYPE = new TypeToken<List<DailyRecord>>() {}.getType();

private final Path dataDir;
private final Gson gson;

public JsonSalaryRepository() {
    this(Path.of("data"));
}

public JsonSalaryRepository(Path dataDir) {
    this.dataDir = dataDir;
    this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    try {
        Files.createDirectories(dataDir);
    } catch (IOException e) {
        throw new RuntimeException("Не удалось создать папку данных: " + dataDir, e);
    }
}

@Override
public void save(DailyRecord record) {
    Objects.requireNonNull(record, "record не может быть null");
    Objects.requireNonNull(record.getDate(), "date не может быть null");
    Objects.requireNonNull(record.getPlace(), "place не может быть null");

    YearMonth month = YearMonth.from(record.getDate());
    List<DailyRecord> records = readMonth(month);

    records.removeIf(r -> r.getDate().equals(record.getDate())
            && r.getPlace() == record.getPlace());
    records.add(record);
    records.sort(byDateAndPlace());

    writeMonth(month, records);
    logger.debug("Сохранена запись за {} ({})", record.getDate(), record.getPlace());
}

@Override
public Optional<DailyRecord> findByDate(LocalDate date, WorkDirection place) {
    return readMonth(YearMonth.from(date)).stream()
            .filter(r -> r.getDate().equals(date) && r.getPlace() == place)
            .findFirst();
}

@Override
public List<DailyRecord> findByMonth(YearMonth month) {
    return readMonth(month);
}

@Override
public List<DailyRecord> findByMonth(YearMonth month, WorkDirection place) {
    return readMonth(month).stream()
            .filter(r -> r.getPlace() == place)
            .toList();
}

@Override
public boolean deleteByDate(LocalDate date, WorkDirection place) {
    YearMonth month = YearMonth.from(date);
    List<DailyRecord> records = readMonth(month);
    boolean removed = records.removeIf(r ->
                                               r.getDate().equals(date) && r.getPlace() == place);
    if (removed) {
        writeMonth(month, records);
        logger.debug("Удалена запись за {} ({})", date, place);
    }
    return removed;
}

@Override
public List<DailyRecord> findAll() {
    if (!Files.isDirectory(dataDir)) {
        return List.of();
    }
    List<DailyRecord> all = new ArrayList<>();
    try (Stream<Path> files = Files.list(dataDir)) {
        files.filter(p -> p.getFileName().toString().matches("salary-\\d{4}-\\d{2}\\.json"))
                .forEach(p -> all.addAll(readFile(p)));
    } catch (IOException e) {
        logger.error("Ошибка чтения папки данных", e);
    }
    all.sort(byDateAndPlace());
    return all;
}

private List<DailyRecord> readMonth(YearMonth month) {
    return readFile(fileFor(month));
}

private List<DailyRecord> readFile(Path file) {
    if (!Files.exists(file)) {
        return new ArrayList<>();
    }
    try (Reader reader = Files.newBufferedReader(file)) {
        List<DailyRecord> records = gson.fromJson(reader, LIST_TYPE);
        return records != null ? new ArrayList<>(records) : new ArrayList<>();
    } catch (IOException e) {
        logger.error("Не удалось прочитать {}", file, e);
        return new ArrayList<>();
    }
}

private void writeMonth(YearMonth month, List<DailyRecord> records) {
    Path file = fileFor(month);
    try (Writer writer = Files.newBufferedWriter(file)) {
        gson.toJson(records, LIST_TYPE, writer);
    } catch (IOException e) {
        logger.error("Не удалось записать {}", file, e);
        throw new RuntimeException("Ошибка сохранения данных", e);
    }
}

private Path fileFor(YearMonth month) {
    return dataDir.resolve("salary-" + month.format(FILE_FORMAT) + ".json");
}

private Comparator<DailyRecord> byDateAndPlace() {
    return Comparator.comparing(DailyRecord::getDate)
            .thenComparing(DailyRecord::getPlace);
}
}