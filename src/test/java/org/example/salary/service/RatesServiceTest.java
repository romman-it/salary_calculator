package org.example.salary.service;

import org.example.salary.model.Rates;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RatesService")
class RatesServiceTest {

@TempDir
Path tempDir;

@Test
@DisplayName("если файла нет — создаются значения по умолчанию")
void createsDefaultsWhenFileMissing() {
    var service = new RatesService(tempDir.resolve("rates.json"));
    assertEquals(new BigDecimal("5.00"), service.getRates().getPaymentForOrder());
    assertTrue(tempDir.resolve("rates.json").toFile().exists(),
               "Файл должен быть создан");
}

@Test
@DisplayName("updateRates сохраняет и загружается в новом экземпляре")
void persistsBetweenInstances() {
    Path file = tempDir.resolve("rates.json");
    var service = new RatesService(file);

    Rates updated = Rates.defaults();
    updated.setPaymentForOrder(new BigDecimal("7.77"));
    service.updateRates(updated);

    var second = new RatesService(file);
    assertEquals(new BigDecimal("7.77"), second.getRates().getPaymentForOrder());
}

@Test
@DisplayName("resetToDefaults возвращает исходные значения")
void resetToDefaults() {
    Path file = tempDir.resolve("rates.json");
    var service = new RatesService(file);

    Rates custom = Rates.defaults();
    custom.setPaymentForOrder(new BigDecimal("99.99"));
    service.updateRates(custom);

    service.resetToDefaults();

    assertEquals(new BigDecimal("5.00"), service.getRates().getPaymentForOrder());
}
}