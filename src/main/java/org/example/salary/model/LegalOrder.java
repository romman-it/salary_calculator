package org.example.salary.model;

import java.math.BigDecimal;
import java.util.Objects;

public class LegalOrder {

private BigDecimal orderCost;
private BigDecimal bonus;

public LegalOrder() {
}

public LegalOrder(BigDecimal orderCost, BigDecimal bonus) {
    this.orderCost = orderCost;
    this.bonus = bonus;
}

public BigDecimal getOrderCost() { return orderCost; }
public void setOrderCost(BigDecimal orderCost) { this.orderCost = orderCost; }

public BigDecimal getBonus() { return bonus; }
public void setBonus(BigDecimal bonus) { this.bonus = bonus; }

@Override
public String toString() {
    return String.format("Заказ %.2f → %.2f руб.", orderCost, bonus);
}

@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LegalOrder that = (LegalOrder) o;
    return Objects.equals(orderCost, that.orderCost) && Objects.equals(bonus, that.bonus);
}

@Override
public int hashCode() {
    return Objects.hash(orderCost, bonus);
}
}