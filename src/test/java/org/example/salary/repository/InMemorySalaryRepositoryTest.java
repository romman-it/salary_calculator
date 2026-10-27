package org.example.salary.repository;

import org.example.salary.model.DailyRecord;
import org.example.salary.model.WorkDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemorySalaryRepository")
class InMemorySalaryRepositoryTest {

private InMemorySalaryRepository repository;

private static final LocalDate DAY_1 = LocalDate.of(2026, 10, 26);
private static final LocalDate DAY_2 = LocalDate.of(2026, 10, 27);
private static final LocalDate NEXT_MONTH = LocalDate.of(2026, 11, 1);

@BeforeEach
void setUp() {
    repository = new InMemorySalaryRepository();
}

private DailyRecord record(LocalDate date, WorkDirection place, String salary) {
    DailyRecord r = new DailyRecord();
    r.setDate(date);
    r.setPlace(place);
    r.setTotalSalary(new BigDecimal(salary));
    r.setEdostavkaCount(10);
    r.setEmallCount(5);
    r.setMailCount(3);
    r.setBagsCount(8);
    r.setLargeOrdersCount(2);
    r.setHoursWorked(8);
    r.setLegalPayments(new BigDecimal("15.00"));
    return r;
}

@Test
@DisplayName("save сохраняет запись, findByDate её находит")
void saveAndFindByDate() {
    repository.save(record(DAY_1, WorkDirection.REGION, "125.50"));

    var found = repository.findByDate(DAY_1, WorkDirection.REGION);
    assertTrue(found.isPresent());
    assertEquals(new BigDecimal("125.50"), found.get().getTotalSalary());
}

@Test
@DisplayName("findByDate возвращает пустой Optional, если записи нет")
void findByDateReturnsEmptyWhenMissing() {
    assertTrue(repository.findByDate(DAY_1, WorkDirection.REGION).isEmpty());
}

@Test
@DisplayName("повторный save за ту же дату и место заменяет запись")
void saveOverwritesSameDateAndPlace() {
    repository.save(record(DAY_1, WorkDirection.REGION, "100.00"));
    repository.save(record(DAY_1, WorkDirection.REGION, "200.00"));

    assertEquals(1, repository.findAll().size());
    assertEquals(new BigDecimal("200.00"),
                 repository.findByDate(DAY_1, WorkDirection.REGION).orElseThrow().getTotalSalary());
}

@Test
@DisplayName("город и регион за один день — две отдельные записи")
void cityAndRegionSameDayAreSeparate() {
    repository.save(record(DAY_1, WorkDirection.REGION, "100.00"));
    repository.save(record(DAY_1, WorkDirection.CITY, "50.00"));

    assertEquals(2, repository.findAll().size());
    assertEquals(new BigDecimal("100.00"),
                 repository.findByDate(DAY_1, WorkDirection.REGION).orElseThrow().getTotalSalary());
    assertEquals(new BigDecimal("50.00"),
                 repository.findByDate(DAY_1, WorkDirection.CITY).orElseThrow().getTotalSalary());
}

@Test
@DisplayName("findByMonth возвращает записи месяца, отсортированные по дате и месту")
void findByMonthFiltersAndSorts() {
    repository.save(record(DAY_2, WorkDirection.REGION, "200.00"));
    repository.save(record(DAY_1, WorkDirection.CITY, "50.00"));
    repository.save(record(DAY_1, WorkDirection.REGION, "100.00"));
    repository.save(record(NEXT_MONTH, WorkDirection.REGION, "300.00"));

    List<DailyRecord> october = repository.findByMonth(YearMonth.of(2026, 10));

    assertEquals(3, october.size());
    assertEquals(DAY_1, october.get(0).getDate());
    assertEquals(DAY_1, october.get(1).getDate());
    assertEquals(DAY_2, october.get(2).getDate());
}

@Test
@DisplayName("findByMonth с фильтром по месту возвращает только записи этого места")
void findByMonthFiltersByPlace() {
    repository.save(record(DAY_1, WorkDirection.REGION, "100.00"));
    repository.save(record(DAY_1, WorkDirection.CITY, "50.00"));
    repository.save(record(DAY_2, WorkDirection.REGION, "200.00"));

    List<DailyRecord> region = repository.findByMonth(YearMonth.of(2026, 10), WorkDirection.REGION);

    assertEquals(2, region.size());
    assertTrue(region.stream().allMatch(r -> r.getPlace() == WorkDirection.REGION));
}

@Test
@DisplayName("deleteByDate удаляет запись за конкретное место")
void deleteByDateRemovesByPlace() {
    repository.save(record(DAY_1, WorkDirection.REGION, "100.00"));
    repository.save(record(DAY_1, WorkDirection.CITY, "50.00"));

    assertTrue(repository.deleteByDate(DAY_1, WorkDirection.REGION));
    assertTrue(repository.findByDate(DAY_1, WorkDirection.REGION).isEmpty());

    // город остался
    assertTrue(repository.findByDate(DAY_1, WorkDirection.CITY).isPresent());
}

@Test
@DisplayName("deleteByDate возвращает false, если записи не было")
void deleteByDateReturnsFalseWhenMissing() {
    assertFalse(repository.deleteByDate(DAY_1, WorkDirection.REGION));
}

@Test
@DisplayName("save(null) бросает NullPointerException")
void saveNullThrows() {
    assertThrows(NullPointerException.class, () -> repository.save(null));
}
}