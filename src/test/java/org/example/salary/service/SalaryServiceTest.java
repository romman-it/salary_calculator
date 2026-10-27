package org.example.salary.service;
import org.example.salary.model.Rates;
import org.example.salary.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SalaryService")
class SalaryServiceTest {

@TempDir
Path tempDir;

private SalaryService service;

@BeforeEach
void setUp() {
    RatesService ratesService = new RatesService(tempDir.resolve("rates.json"));
    Rates rates = Rates.defaults();
    // фиксируем ставки, чтобы тесты не зависели от дефолтов в Rates.defaults()
    rates.setPaymentForOrder(new BigDecimal("5.00"));
    rates.setPaymentForMail(new BigDecimal("2.50"));
    rates.setPaymentForGrandOrder(new BigDecimal("10.00"));
    rates.setPaymentForHours(new BigDecimal("2.20"));
    rates.setPaymentForOneDag(new BigDecimal("0.50"));
    rates.setPaymentForCancelled(new BigDecimal("3.50"));
    rates.setRegionMinLegalBonus(new BigDecimal("5.00"));
    rates.setRegionMinimumDaily(new BigDecimal("126.00"));
    rates.setCityMorningRate(new BigDecimal("4.50"));
    rates.setCityDayRate(new BigDecimal("3.50"));
    rates.setCityEveningRate(new BigDecimal("4.50"));
    rates.setCityZoneBRate(new BigDecimal("4.50"));
    rates.setCityAfter19Surcharge(new BigDecimal("1.00"));
    ratesService.updateRates(rates);
    rates.setHireDate(java.time.LocalDate.now());
    rates.setSeniorityBonus1Year(new BigDecimal("7.00"));
    rates.setSeniorityBonus2Years(new BigDecimal("9.00"));
    rates.setSeniorityBonus3Years(new BigDecimal("12.00"));

    service = new SalaryService(ratesService);
}

private RegionInput regionInput(int eD, int eM, int mail, int kbt,
                                int cancelled, int bags, LocalTime open, LocalTime close,
                                List<LegalOrder> legal) {
    return new RegionInput(open, close, eD, eM, mail, kbt, cancelled, bags, legal);
}

private CityInput cityInput(int cityOrders, int after19, int zoneB,
                            int cancelled, int bags, LocalTime open, LocalTime close,
                            List<LegalOrder> legal) {
    return new CityInput(open, close, cityOrders, after19, zoneB, cancelled, bags, legal);
}

private CityInput cityInput(int cityOrders, int after19, int zoneB, int cancelled, int bags) {
    return cityInput(cityOrders, after19, zoneB, cancelled, bags,
                     LocalTime.of(9, 0), LocalTime.of(18, 0), List.of());
}

private CityInput cityInput(int cityOrders, int after19, int zoneB, int cancelled, int bags,
                            LocalTime open, LocalTime close) {
    return cityInput(cityOrders, after19, zoneB, cancelled, bags, open, close, List.of());
}

// ============ РЕГИОН ============

@Test
@DisplayName("регион: базовая ставка 5.00, почта 2.50, часы 2.20")
void regionBasicRates() {
    // еД 10*5=50 + еМ 5*5=25 + почта 3*2.50=7.50 + КБТ 2*10=20
    // часы 8*2.20=17.60, сумки 8-2*(10+5)<0 = 0
    BigDecimal result = service.calculateRegionRaw(
            regionInput(10, 5, 3, 2, 0, 8,
                        LocalTime.of(9, 0), LocalTime.of(18, 0), List.of())
    );
    assertEquals(new BigDecimal("122.30"), result);}

@Test
@DisplayName("регион: сумки считаются от общего числа заказов (еД+еМ)")
void regionBagsOnlyFromEDandEM() {
    // бесплатных = 1 × 10 = 10; платных = 25 − 10 = 15 × 0.5 = 7.5
    // 5*5 + 5*5 + 3*2.5 + 7.5 + 9*2.2 = 25+25+7.5+7.5+19.8 = 84.80
    BigDecimal result = service.calculateRegionRaw(
            regionInput(5, 5, 3, 0, 0, 25,
                        LocalTime.of(9, 0), LocalTime.of(18, 0), List.of())
    );
    assertEquals(new BigDecimal("84.80"), result);
}

@Test
@DisplayName("регион: отменённый заказ × 3.50")
void regionCancelledOrders() {
    BigDecimal result = service.calculateRegionRaw(
            regionInput(0, 0, 0, 0, 4, 0,
                        LocalTime.of(9, 0), LocalTime.of(18, 0), List.of())
    );
    // 4*3.50 + 8*2.2 = 14 + 17.6 = 31.60
    assertEquals(new BigDecimal("33.80"), result);}

@Test
@DisplayName("регион: юр.лицо по шкале, но не меньше 5.00")
void regionLegalWithMin() {
    // заказ 1.00 → по шкале 1.60, но минимум 5.00 → 5.00
    LegalOrder small = new LegalOrder(new BigDecimal("1.00"), null);
    BigDecimal result = service.calculateRegionRaw(
            regionInput(0, 0, 0, 0, 0, 0,
                        LocalTime.of(9, 0), LocalTime.of(18, 0), List.of(small))
    );
    // 5.00 (юр) + 8*2.2 = 22.60
    assertEquals(new BigDecimal("24.80"), result);}

@Test
@DisplayName("регион: отрицательное количество → исключение")
void regionNegativeThrows() {
    assertThrows(IllegalArgumentException.class,
                 () -> service.calculateRegionRaw(
                         regionInput(-1, 0, 0, 0, 0, 0,
                                     LocalTime.of(9, 0), LocalTime.of(18, 0), List.of())));
}

// ============ ГОРОД ============

@Test
@DisplayName("город утро (открытие 07:00): ставка 4.50")
void cityMorningRate() {
    // 10*4.5 = 45 + 8*2.2 = 17.6 → 62.60
    BigDecimal result = service.calculateCity(
            cityInput(10, 0, 0, 0, 0,
                      LocalTime.of(7, 0), LocalTime.of(18, 0)));
    assertEquals(new BigDecimal("69.20"), result);}

@Test
@DisplayName("город день (открытие 10:00): ставка 3.50")
void cityDayRate() {
    // 10*3.5 = 35 + 17.6 = 52.60
    BigDecimal result = service.calculateCity(
            cityInput(10, 0, 0, 0, 0,
                      LocalTime.of(10, 0), LocalTime.of(18, 0)));
    assertEquals(new BigDecimal("52.60"), result);
}

@Test
@DisplayName("город вечер (открытие 16:00): ставка 4.50")
void cityEveningRate() {
    BigDecimal result = service.calculateCity(
            cityInput(10, 0, 0, 0, 0,
                      LocalTime.of(16, 0), LocalTime.of(22, 0)));
    // 10*4.5 + 6*2.2 = 45 + 13.2 = 58.20
    assertEquals(new BigDecimal("58.20"), result);
}

@Test
@DisplayName("город: заказы после 19:00 — ставка +1")
void cityAfter19Surcharge() {
    // 7 до 19-00 × 4.5 + 3 после × 5.5 = 31.5 + 16.5 = 48
    BigDecimal result = service.calculateCity(
            cityInput(10, 3, 0, 0, 0,
                      LocalTime.of(9, 0), LocalTime.of(18, 0)));
    // день: 3.50 + 1 = 4.50 после 19; 7*3.5 + 3*4.5 = 38 + 9 ч × 2.20 = 57.80
    assertEquals(new BigDecimal("57.80"), result);
}

@Test
@DisplayName("город: зона Б × 4.50, без надбавки после 19")
void cityZoneB() {
    BigDecimal result = service.calculateCity(
            cityInput(0, 0, 5, 0, 0,
                      LocalTime.of(10, 0), LocalTime.of(18, 0)));
    // 5*4.5 = 22.5 + 8*2.2 = 22.5+17.6 = 40.10
    assertEquals(new BigDecimal("40.10"), result);
}

@Test
@DisplayName("город: сумки от суммы город+зонаБ")
void cityBagsFromAllOrders() {
    // бесплатных = 1 × 8 = 8; платных = 20 − 8 = 12 × 0.5 = 6
    // 5*3.5 + 3*4.5 + 8*2.2 + 6 = 17.5+13.5+17.6+6 = 54.60
    BigDecimal result = service.calculateCity(
            cityInput(5, 0, 3, 0, 20,
                      LocalTime.of(10, 0), LocalTime.of(18, 0)));
    assertEquals(new BigDecimal("54.60"), result);
}

@Test
@DisplayName("город: after19 > cityOrders → исключение")
void cityAfter19ExceedsOrders() {
    assertThrows(IllegalArgumentException.class,
                 () -> service.calculateCity(
                         cityInput(5, 10, 0, 0, 0,
                                   LocalTime.of(9, 0), LocalTime.of(18, 0))));
}

// ============ ШКАЛА ЮР.ЛИЦ ============

@Test
@DisplayName("юр.лицо < 700 → ×1.60")
void legalUnder700() {
    assertEquals(new BigDecimal("160.00"),
                 service.calculateLegalBonus(new BigDecimal("100")));
}

@Test
@DisplayName("юр.лицо 700–1000 → ×1.44")
void legal700To1000() {
    assertEquals(new BigDecimal("1080.00"),
                 service.calculateLegalBonus(new BigDecimal("750")));
}

@Test
@DisplayName("юр.лицо 1500–2500 → ×1.04")
void legal1500To2500() {
    assertEquals(new BigDecimal("1664.00"),
                 service.calculateLegalBonus(new BigDecimal("1600")));
}

@Test
@DisplayName("юр.лицо ≥2500 → 30.00")
void legalOver2500() {
    assertEquals(new BigDecimal("30.00"),
                 service.calculateLegalBonus(new BigDecimal("5000")));
}
@Test
@DisplayName("регион: 9 часов и меньше — минимум НЕ применяется")
void regionNoMinimumUnder9Hours() {
    BigDecimal result = service.calculateRegion(
            regionInput(0, 0, 0, 0, 0, 0,
                        LocalTime.of(9, 0), LocalTime.of(18, 0), List.of())
    );
    // 9 ч × 2.20 = 19.80 < 126, но часов не > 9 → минимум не применяется
    assertEquals(new BigDecimal("19.80"), result);
}

@Test
@DisplayName("регион: > 9 часов и меньше 126 → 126")
void regionAppliesMinimumOver9Hours() {
    BigDecimal result = service.calculateRegion(
            regionInput(0, 0, 0, 0, 0, 0,
                        LocalTime.of(9, 0), LocalTime.of(19, 0), List.of())
    );
    // 10 ч × 2.20 = 22.00 < 126, часов > 9 → 126
    assertEquals(new BigDecimal("126.00"), result);
}

@Test
@DisplayName("регион: сумма выше 126 → платим расчётную")
void regionAboveMinimumKeepsCalculated() {
    BigDecimal result = service.calculateRegion(
            regionInput(20, 10, 5, 5, 0, 0,
                        LocalTime.of(9, 0), LocalTime.of(18, 0), List.of())
    );
    // 20*5+10*5+5*2.5+5*10+9*2.2 = 100+50+12.5+50+19.8 = 232.30
    assertEquals(new BigDecimal("232.30"), result);
}
private SalaryService serviceWithHireDate(int years) {
    RatesService rs = new RatesService(tempDir.resolve("rates-hire-" + years + ".json"));
    Rates r = Rates.defaults();
    r.setPaymentForOrder(new BigDecimal("5.00"));
    r.setPaymentForMail(new BigDecimal("2.50"));
    r.setPaymentForGrandOrder(new BigDecimal("10.00"));
    r.setPaymentForHours(new BigDecimal("2.20"));
    r.setPaymentForOneDag(new BigDecimal("0.50"));
    r.setPaymentForCancelled(new BigDecimal("3.50"));
    r.setRegionMinLegalBonus(new BigDecimal("5.00"));
    r.setRegionMinimumDaily(new BigDecimal("126.00"));
    r.setSeniorityBonus1Year(new BigDecimal("7.00"));
    r.setSeniorityBonus2Years(new BigDecimal("9.00"));
    r.setSeniorityBonus3Years(new BigDecimal("12.00"));
    r.setHireDate(java.time.LocalDate.now().minusYears(years));
    rs.updateRates(r);
    return new SalaryService(rs);
}

@Test
@DisplayName("стаж < 1 года → без бонуса")
void noSeniorityBonusUnderYear() {
    SalaryService s = serviceWithHireDate(0);
    BigDecimal result = s.calculateRegion(
            regionInput(20, 10, 5, 5, 0, 0,
                        LocalTime.of(9, 0), LocalTime.of(18, 0), List.of())
    );
    // raw = 20*5+10*5+5*2.5+5*10+9*2.2 = 237.30, 9 ч → минимум не применяется, стаж 0 → 237.30
    assertEquals(new BigDecimal("232.30"), result);}

@Test
@DisplayName("стаж 1+ год → +7%")
void seniorityBonus1Year() {
    SalaryService s = serviceWithHireDate(1);
    BigDecimal result = s.calculateRegion(
            regionInput(20, 10, 5, 5, 0, 0,
                        LocalTime.of(9, 0), LocalTime.of(18, 0), List.of())
    );
// 232.30 × 1.07 = 248.561 → 248.56
    assertEquals(new BigDecimal("248.56"), result);}

@Test
@DisplayName("стаж 2+ года → +9%")
void seniorityBonus2Years() {
    SalaryService s = serviceWithHireDate(2);
    BigDecimal result = s.calculateRegion(
            regionInput(20, 10, 5, 5, 0, 0,
                        LocalTime.of(9, 0), LocalTime.of(18, 0), List.of())
    );
    // 232.30 × 1.09 = 253.207 → 253.21
    assertEquals(new BigDecimal("253.21"), result);
}

@Test
@DisplayName("стаж 3+ года → +12%")
void seniorityBonus3Years() {
    SalaryService s = serviceWithHireDate(3);
    BigDecimal result = s.calculateRegion(
            regionInput(20, 10, 5, 5, 0, 0,
                        LocalTime.of(9, 0), LocalTime.of(18, 0), List.of())
    );
    // 232.30 × 1.12 = 260.176 → 260.18
    assertEquals(new BigDecimal("260.18"), result);
}
}