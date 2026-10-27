package org.example.salary.service;

import org.example.salary.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class SalaryService {

private static final Logger logger = LoggerFactory.getLogger(SalaryService.class);
private static final int SCALE = 2;
private static final int FREE_BAGS_PER_ORDER = 1;
private static final LocalTime AFTER_19 = LocalTime.of(19, 0);

private final RatesService ratesService;

public SalaryService(RatesService ratesService) {
    this.ratesService = Objects.requireNonNull(ratesService);
}
/**
 * Итоговая зарплата за день: сырой расчёт, но не ниже минимальной дневной
 */
public BigDecimal calculateRegion(RegionInput input) {
    BigDecimal raw = calculateRegionRaw(input);

    int hours = hoursBetween(input.openTime(), input.closeTime());

    BigDecimal base = raw;
    BigDecimal min = ratesService.getRates().getRegionMinimumDaily();
    if (hours > 9 && min != null && raw.compareTo(min) < 0) {
        logger.debug("Регион: {} < минимума {} (часов={} > 9) → применяем минимум", raw, min, hours);
        base = min;
    }

    BigDecimal withSeniority = base.multiply(seniorityMultiplier())
            .setScale(SCALE, RoundingMode.HALF_UP);
    logger.debug("Регион итог: base={} → {}", base, withSeniority);
    return withSeniority;
}

public BigDecimal calculateRegionRaw(RegionInput input) {
    validate(input);

    Rates rates = ratesService.getRates();
    int hours = hoursBetween(input.openTime(), input.closeTime());

    int regularOrders = input.edostavkaCount() + input.emallCount();

    BigDecimal total = BigDecimal.ZERO
            .add(money(input.edostavkaCount()).multiply(rates.getPaymentForOrder()))
            .add(money(input.emallCount()).multiply(rates.getPaymentForOrder()))
            .add(money(input.mailCount()).multiply(rates.getPaymentForMail()))
            .add(money(input.largeOrdersCount()).multiply(rates.getPaymentForGrandOrder()))
            .add(money(input.cancelledOrders()).multiply(rates.getPaymentForCancelled()))
            .add(money(hours).multiply(rates.getPaymentForHours()))
            .add(calculateBags(input.bagsCount(), regularOrders, rates.getPaymentForOneDag()))
            .add(calculateLegalTotal(input.legalOrders(), rates.getRegionMinLegalBonus()));

    BigDecimal rounded = total.setScale(SCALE, RoundingMode.HALF_UP);
    logger.debug("Регион (raw): еД={}, еМ={}, почта={}, КБТ={}, отмен={}, часы={}, сумки={}, итого={}",
                 input.edostavkaCount(), input.emallCount(), input.mailCount(),
                 input.largeOrdersCount(), input.cancelledOrders(), hours,
                 input.bagsCount(), rounded);
    return rounded;
}
// ================================================================ город ====================================
public BigDecimal calculateCity(CityInput input) {
    validate(input);

    Rates rates = ratesService.getRates();
    int hours = hoursBetween(input.openTime(), input.closeTime());
    CitySett shift = CitySett.fromOpenTime(input.openTime());

    BigDecimal shiftRate = switch (shift) {
        case MORNING -> rates.getCityMorningRate();
        case DAY -> rates.getCityDayRate();
        case EVENING -> rates.getCityEveningRate();
    };

    int regularAfter19 = input.after19Orders();
    int regularBefore19 = Math.max(0, input.cityOrders() - regularAfter19);

    BigDecimal orderPay =
            money(regularBefore19).multiply(shiftRate)
                    .add(money(regularAfter19)
                                 .multiply(shiftRate.add(rates.getCityAfter19Surcharge())))
                    .add(money(input.zoneBOrders()).multiply(rates.getCityZoneBRate()));

    int allCityOrders = input.cityOrders() + input.zoneBOrders();

    BigDecimal total = BigDecimal.ZERO
            .add(orderPay)
            .add(money(input.cancelledOrders()).multiply(rates.getPaymentForCancelled()))
            .add(money(hours).multiply(rates.getPaymentForHours()))
            .add(calculateBags(input.bagsCount(), allCityOrders, rates.getPaymentForOneDag()))
            .add(calculateLegalTotal(input.legalOrders(), shiftRate));

    BigDecimal rounded = total.setScale(SCALE, RoundingMode.HALF_UP);
    BigDecimal withSeniority = rounded.multiply(seniorityMultiplier())
            .setScale(SCALE, RoundingMode.HALF_UP);
    logger.debug("Город ({}): заказы={} (после19={}), зонаБ={}, отмен={}, часы={}, итог={}",
                 shift, input.cityOrders(), input.after19Orders(),
                 input.zoneBOrders(), input.cancelledOrders(), hours, withSeniority);
    return withSeniority;
}

// ======================================================================================================

public BigDecimal calculateLegalBonus(BigDecimal orderCost) {
    Objects.requireNonNull(orderCost, "orderCost не может быть null");
    if (orderCost.signum() < 0) {
        throw new IllegalArgumentException("Стоимость заказа не может быть отрицательной");
    }
    BigDecimal result;
    if (orderCost.compareTo(new BigDecimal("700")) < 0) {
        result = orderCost.multiply(new BigDecimal("1.60"));
    } else if (orderCost.compareTo(new BigDecimal("1000")) < 0) {
        result = orderCost.multiply(new BigDecimal("1.44"));
    } else if (orderCost.compareTo(new BigDecimal("1500")) < 0) {
        result = orderCost.multiply(new BigDecimal("1.30"));
    } else if (orderCost.compareTo(new BigDecimal("2500")) < 0) {
        result = orderCost.multiply(new BigDecimal("1.04"));
    } else {
        result = new BigDecimal("30.00");
    }
    return result.setScale(SCALE, RoundingMode.HALF_UP);
}

public BigDecimal calculateLegalBonusWithMin(BigDecimal orderCost, BigDecimal minBonus) {
    BigDecimal bonus = calculateLegalBonus(orderCost);
    return bonus.max(minBonus).setScale(SCALE, RoundingMode.HALF_UP);
}

private BigDecimal calculateLegalTotal(List<LegalOrder> orders, BigDecimal minBonus) {
    if (orders == null || orders.isEmpty()) return BigDecimal.ZERO;
    return orders.stream()
            .map(o -> o.getBonus() != null ? o.getBonus()
                    : calculateLegalBonusWithMin(o.getOrderCost(), minBonus))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}

private BigDecimal calculateBags(int totalBags, int totalOrders, BigDecimal bagRate) {
    int freeBags = FREE_BAGS_PER_ORDER * totalOrders;
    int paidBags = Math.max(0, totalBags - freeBags);
    return money(paidBags).multiply(bagRate);
}

private int hoursBetween(LocalTime open, LocalTime close) {
    Duration d = Duration.between(open, close);
    if (d.isNegative()) {
        d = d.plusDays(1); // смена через полночь
    }
    return (int) d.toHours();
}

private BigDecimal money(int value) {
    return BigDecimal.valueOf(value);
}

private void validate(RegionInput input) {
    Objects.requireNonNull(input, "input не может быть null");
    Objects.requireNonNull(input.openTime(), "openTime не может быть null");
    Objects.requireNonNull(input.closeTime(), "closeTime не может быть null");
    if (input.edostavkaCount() < 0 || input.emallCount() < 0
            || input.mailCount() < 0 || input.largeOrdersCount() < 0
            || input.cancelledOrders() < 0 || input.bagsCount() < 0) {
        throw new IllegalArgumentException("Количество не может быть отрицательным");
    }
}

private void validate(CityInput input) {
    Objects.requireNonNull(input, "input не может быть null");
    Objects.requireNonNull(input.openTime(), "openTime не может быть null");
    Objects.requireNonNull(input.closeTime(), "closeTime не может быть null");
    if (input.cityOrders() < 0 || input.after19Orders() < 0
            || input.zoneBOrders() < 0 || input.cancelledOrders() < 0
            || input.bagsCount() < 0) {
        throw new IllegalArgumentException("Количество не может быть отрицательным");
    }
    if (input.after19Orders() > input.cityOrders()) {
        throw new IllegalArgumentException("Заказов после 19:00 не может быть больше общего числа");
    }
}
private BigDecimal seniorityMultiplier() {
    Rates r = ratesService.getRates();
    LocalDate hireDate = r.getHireDate();
    if (hireDate == null) return BigDecimal.ONE;

    long months = java.time.temporal.ChronoUnit.MONTHS.between(hireDate, LocalDate.now());
    BigDecimal percent;
    if (months >= 36) {
        percent = r.getSeniorityBonus3Years();
    } else if (months >= 24) {
        percent = r.getSeniorityBonus2Years();
    } else if (months >= 12) {
        percent = r.getSeniorityBonus1Year();
    } else {
        return BigDecimal.ONE;
    }
    BigDecimal factor = BigDecimal.ONE.add(
            percent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
    logger.debug("Бонус за стаж: {} мес. → {}% (×{})", months, percent, factor);
    return factor;
}
public Rates getRates() {
    return ratesService.getRates();
}
}