package org.example.salary.model;

import java.time.LocalTime;
import java.util.List;

public record RegionInput(
        LocalTime openTime,
        LocalTime closeTime,
        int edostavkaCount,
        int emallCount,
        int mailCount,
        int largeOrdersCount,
        int cancelledOrders,
        int bagsCount,
        List<LegalOrder> legalOrders
) {
}