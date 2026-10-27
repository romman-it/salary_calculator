package org.example.salary.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DailyRecord {

private WorkDirection place;
private LocalDate date;
private BigDecimal totalSalary;
private int edostavkaCount;
private int emallCount;
private int mailCount;
private int bagsCount;
private int largeOrdersCount;
private int cancelledOrdersCount;
private int hoursWorked;
private BigDecimal legalPayments;
private List<LegalOrder> legalOrders = new ArrayList<>();

public DailyRecord() {
}

public WorkDirection getPlace() { return place; }
public void setPlace(WorkDirection place) { this.place = place; }

public LocalDate getDate() { return date; }
public void setDate(LocalDate date) { this.date = date; }

public BigDecimal getTotalSalary() { return totalSalary; }
public void setTotalSalary(BigDecimal totalSalary) { this.totalSalary = totalSalary; }

public int getEdostavkaCount() { return edostavkaCount; }
public void setEdostavkaCount(int edostavkaCount) { this.edostavkaCount = edostavkaCount; }

public int getEmallCount() { return emallCount; }
public void setEmallCount(int emallCount) { this.emallCount = emallCount; }

public int getMailCount() { return mailCount; }
public void setMailCount(int mailCount) { this.mailCount = mailCount; }

public int getBagsCount() { return bagsCount; }
public void setBagsCount(int bagsCount) { this.bagsCount = bagsCount; }

public int getLargeOrdersCount() { return largeOrdersCount; }
public void setLargeOrdersCount(int largeOrdersCount) { this.largeOrdersCount = largeOrdersCount; }

public int getCancelledOrdersCount() { return cancelledOrdersCount; }
public void setCancelledOrdersCount(int cancelledOrdersCount) { this.cancelledOrdersCount = cancelledOrdersCount; }

public int getHoursWorked() { return hoursWorked; }
public void setHoursWorked(int hoursWorked) { this.hoursWorked = hoursWorked; }

public BigDecimal getLegalPayments() { return legalPayments; }
public void setLegalPayments(BigDecimal legalPayments) { this.legalPayments = legalPayments; }

public List<LegalOrder> getLegalOrders() { return legalOrders; }
public void setLegalOrders(List<LegalOrder> legalOrders) {
    this.legalOrders = legalOrders != null ? legalOrders : new ArrayList<>();
}

@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DailyRecord that = (DailyRecord) o;
    return Objects.equals(date, that.date) && place == that.place;
}

@Override
public int hashCode() {
    return Objects.hash(date, place);
}

@Override
public String toString() {
    return String.format("[%s] %s | %.2f руб. | часов: %d | отменено: %d | юр.лиц: %d",
                         place, date, totalSalary, hoursWorked, cancelledOrdersCount,
                         legalOrders != null ? legalOrders.size() : 0);
}
}