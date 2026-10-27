package org.example.salary.model;

import java.time.LocalTime;
import java.util.List;

public record CityInput(
        LocalTime openTime,
        LocalTime closeTime,
        int cityOrders,
        int after19Orders,
        int zoneBOrders,
        int cancelledOrders,
        int bagsCount,
        List<LegalOrder> legalOrders
) {
}