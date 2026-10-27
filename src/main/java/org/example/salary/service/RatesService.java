package org.example.salary.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.salary.model.Rates;
import org.example.salary.util.LocalDateAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;

public class RatesService {

private static final Logger logger = LoggerFactory.getLogger(RatesService.class);

private final Path file;
private final Gson gson;
private Rates current;

public RatesService() {
    this(Path.of("data", "rates.json"));
}

public RatesService(Path file) {
    this.file = file;
    this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    this.current = load();
}

/**
 * Текущие тарифы. Всегда не null.
 */
public Rates getRates() {
    return current;
}

/**
 * Заменить тарифы и сохранить на диск.
 */
public void updateRates(Rates rates) {
    Objects.requireNonNull(rates, "rates не может быть null");
    this.current = rates;
    save(rates);
    logger.info("Тарифы обновлены: {}", rates);
}

/**
 * Сбросить к значениям по умолчанию и сохранить.
 */
public void resetToDefaults() {
    updateRates(Rates.defaults());
    logger.info("Тарифы сброшены к значениям по умолчанию");
}

private Rates load() {
    if (!Files.exists(file)) {
        logger.info("Файл тарифов не найден, используем значения по умолчанию: {}", file);
        Rates defaults = Rates.defaults();
        save(defaults);
        return defaults;
    }
    try (Reader reader = Files.newBufferedReader(file)) {
        Rates loaded = gson.fromJson(reader, Rates.class);
        if (loaded == null) {
            logger.warn("Файл тарифов пустой, используем значения по умолчанию");
            return Rates.defaults();
        }
        logger.info("Тарифы загружены из {}", file);
        Rates merged = mergeWithDefaults(loaded);
        save(merged);   // перезаписываем файл полной версией
        logger.info("Тарифы загружены из {}", file);
        return merged;
    }
    catch (IOException e) {
        logger.error("Не удалось прочитать файл тарифов {}, используем значения по умолчанию", file, e);
        return Rates.defaults();
    }
}

private Rates mergeWithDefaults(Rates loaded) {
    Rates d = Rates.defaults();
    if (loaded.getPaymentForOrder() == null)
        loaded.setPaymentForOrder(d.getPaymentForOrder());
    if (loaded.getPaymentForMail() == null)
        loaded.setPaymentForMail(d.getPaymentForMail());
    if (loaded.getPaymentForGrandOrder() == null)
        loaded.setPaymentForGrandOrder(d.getPaymentForGrandOrder());
    if (loaded.getPaymentForHours() == null)
        loaded.setPaymentForHours(d.getPaymentForHours());
    if (loaded.getPaymentForOneDag() == null)
        loaded.setPaymentForOneDag(d.getPaymentForOneDag());
    if (loaded.getPaymentForCancelled() == null)
        loaded.setPaymentForCancelled(d.getPaymentForCancelled());
    if (loaded.getRegionMinLegalBonus() == null)
        loaded.setRegionMinLegalBonus(d.getRegionMinLegalBonus());
    if (loaded.getCityMorningRate() == null)
        loaded.setCityMorningRate(d.getCityMorningRate());
    if (loaded.getCityDayRate() == null)
        loaded.setCityDayRate(d.getCityDayRate());
    if (loaded.getCityEveningRate() == null)
        loaded.setCityEveningRate(d.getCityEveningRate());
    if (loaded.getCityZoneBRate() == null)
        loaded.setCityZoneBRate(d.getCityZoneBRate());
    if (loaded.getCityAfter19Surcharge() == null)
        loaded.setCityAfter19Surcharge(d.getCityAfter19Surcharge());
    if (loaded.getRegionMinimumDaily() == null)
        loaded.setRegionMinimumDaily(d.getRegionMinimumDaily());
    if (loaded.getUiTheme() == null)
        loaded.setUiTheme(d.getUiTheme());
    if (loaded.getHireDate() == null)
        loaded.setHireDate(d.getHireDate());
    if (loaded.getSeniorityBonus1Year() == null)
        loaded.setSeniorityBonus1Year(d.getSeniorityBonus1Year());
    if (loaded.getSeniorityBonus2Years() == null)
        loaded.setSeniorityBonus2Years(d.getSeniorityBonus2Years());
    if (loaded.getSeniorityBonus3Years() == null)
        loaded.setSeniorityBonus3Years(d.getSeniorityBonus3Years());
    return loaded;
}

private void save(Rates rates) {
    try {
        Files.createDirectories(file.getParent());
        try (Writer writer = Files.newBufferedWriter(file)) {
            gson.toJson(rates, writer);
        }
    }
    catch (IOException e) {
        logger.error("Не удалось сохранить тарифы в {}", file, e);
        throw new RuntimeException("Ошибка сохранения тарифов", e);
    }
}
}