package org.example.salary.repository;

import org.example.salary.model.DailyRecord;
import org.example.salary.model.WorkDirection;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class InMemorySalaryRepository implements SalaryRepository {

private final Map<String, DailyRecord> storage = new HashMap<>();

@Override
public void save(DailyRecord record) {
    Objects.requireNonNull(record, "record не может быть пустым");
    Objects.requireNonNull(record.getDate(), "date не может быть пустым");
    Objects.requireNonNull(record.getPlace(), "place не может быть пустым");
    storage.put(key(record.getDate(), record.getPlace()), record);
}

@Override
public Optional<DailyRecord> findByDate(LocalDate date, WorkDirection place) {
    return Optional.ofNullable(storage.get(key(date, place)));
}

@Override
public List<DailyRecord> findByMonth(YearMonth month) {
    return storage.values().stream()
            .filter(r -> YearMonth.from(r.getDate()).equals(month))
            .sorted(byDateAndPlace())
            .toList();
}

@Override
public List<DailyRecord> findByMonth(YearMonth month, WorkDirection place) {
    return storage.values().stream()
            .filter(r -> YearMonth.from(r.getDate()).equals(month))
            .filter(r -> r.getPlace() == place)
            .sorted(Comparator.comparing(DailyRecord::getDate))
            .toList();
}

@Override
public boolean deleteByDate(LocalDate date, WorkDirection place) {
    return storage.remove(key(date, place)) != null;
}

@Override
public List<DailyRecord> findAll() {
    return storage.values().stream()
            .sorted(byDateAndPlace())
            .toList();
}

private String key(LocalDate date, WorkDirection place) {
    return date + "|" + place;
}

private Comparator<DailyRecord> byDateAndPlace() {
    return Comparator.comparing(DailyRecord::getDate)
            .thenComparing(DailyRecord::getPlace);
}
}