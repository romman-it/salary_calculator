package org.example.salary.repository;

import org.example.salary.model.DailyRecord;
import org.example.salary.model.WorkDirection;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface SalaryRepository {

void save(DailyRecord record);

Optional<DailyRecord> findByDate(LocalDate date, WorkDirection place);

List<DailyRecord> findByMonth(YearMonth month);

List<DailyRecord> findByMonth(YearMonth month, WorkDirection place);

boolean deleteByDate(LocalDate date, WorkDirection place);

List<DailyRecord> findAll();
}