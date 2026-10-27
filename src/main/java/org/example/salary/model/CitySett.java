package org.example.salary.model;

import java.time.LocalTime;

public enum CitySett {
    MORNING("Утро"),
    DAY("День"),
    EVENING("Вечер");

private final String displayName;

CitySett(String displayName) {
    this.displayName = displayName;
}

public String getDisplayName() {return displayName;}

/**
 * Определяет смену по времени открытия:
 * до 09:00 — утро, до 15:00 — день, после 15:00 — вечер.
 */
public static CitySett fromOpenTime(LocalTime openTime) {
    if (openTime.isBefore(LocalTime.of(9, 0))) {
        return MORNING;
    }
    if (openTime.isBefore(LocalTime.of(14, 30))) {
        return DAY;
    }
    return EVENING;
}

@Override
public String toString() {return displayName;}
}