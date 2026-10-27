package org.example.salary.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Rates {
private LocalDate hireDate;
private BigDecimal seniorityBonus1Year;  //бонусы за стаж
private BigDecimal seniorityBonus2Years;
private BigDecimal seniorityBonus3Years;

private BigDecimal paymentForOrder;      // еДоставка и еМолл
private BigDecimal paymentForMail;          // почта
private BigDecimal paymentForGrandOrder;      // КБТ
private BigDecimal paymentForHours;        // часы
private BigDecimal paymentForOneDag;        // сумки
private BigDecimal paymentForCancelled;     // отмена
private BigDecimal regionMinLegalBonus;
private BigDecimal regionMinimumDaily;// минимум для юр.лица реги

private BigDecimal cityMorningRate;
private BigDecimal cityDayRate;
private BigDecimal cityEveningRate;
private BigDecimal cityZoneBRate;
private BigDecimal cityAfter19Surcharge;
private String uiTheme;

public Rates() {
}

public static Rates defaults() {
    Rates r = new Rates();
    r.uiTheme = AppTheme.BUSINESS.name();
    r.regionMinimumDaily = new BigDecimal("126.00");
    r.paymentForOrder = new BigDecimal("5.00");
    r.paymentForMail = new BigDecimal("3.00");
    r.paymentForGrandOrder = new BigDecimal("10.00");
    r.paymentForHours = new BigDecimal("2.20");
    r.paymentForOneDag = new BigDecimal("0.50");
    r.paymentForCancelled = new BigDecimal("3.50");
    r.regionMinLegalBonus = new BigDecimal("5.00");
    // ============================================================
    r.cityMorningRate = new BigDecimal("4.50");
    r.cityDayRate = new BigDecimal("3.50");
    r.cityEveningRate = new BigDecimal("4.50");
    r.cityZoneBRate = new BigDecimal("4.50");
    r.cityAfter19Surcharge = new BigDecimal("1.00");
    //=============================================================
    r.hireDate = LocalDate.now();
    r.seniorityBonus1Year = new BigDecimal("7.00");
    r.seniorityBonus2Years = new BigDecimal("9.00");
    r.seniorityBonus3Years = new BigDecimal("12.00");
    return r;
}

public BigDecimal getRegionMinimumDaily() {
    return regionMinimumDaily;
}

public void setRegionMinimumDaily(BigDecimal v) {
    this.regionMinimumDaily = v;
}

public BigDecimal getPaymentForOrder() {
    return paymentForOrder;
}

public void setPaymentForOrder(BigDecimal v) {
    this.paymentForOrder = v;
}

public BigDecimal getPaymentForMail() {
    return paymentForMail;
}

public void setPaymentForMail(BigDecimal v) {
    this.paymentForMail = v;
}

public BigDecimal getPaymentForGrandOrder() {
    return paymentForGrandOrder;
}

public void setPaymentForGrandOrder(BigDecimal v) {
    this.paymentForGrandOrder = v;
}

public BigDecimal getPaymentForHours() {
    return paymentForHours;
}

public void setPaymentForHours(BigDecimal v) {
    this.paymentForHours = v;
}

public BigDecimal getPaymentForOneDag() {
    return paymentForOneDag;
}

public void setPaymentForOneDag(BigDecimal v) {
    this.paymentForOneDag = v;
}

public BigDecimal getPaymentForCancelled() {
    return paymentForCancelled;
}

public void setPaymentForCancelled(BigDecimal v) {
    this.paymentForCancelled = v;
}

public BigDecimal getRegionMinLegalBonus() {
    return regionMinLegalBonus;
}

public void setRegionMinLegalBonus(BigDecimal v) {
    this.regionMinLegalBonus = v;
}

public BigDecimal getCityMorningRate() {
    return cityMorningRate;
}

public void setCityMorningRate(BigDecimal v) {
    this.cityMorningRate = v;
}

public BigDecimal getCityDayRate() {
    return cityDayRate;
}

public void setCityDayRate(BigDecimal v) {
    this.cityDayRate = v;
}

public BigDecimal getCityEveningRate() {
    return cityEveningRate;
}

public void setCityEveningRate(BigDecimal v) {
    this.cityEveningRate = v;
}

public BigDecimal getCityZoneBRate() {
    return cityZoneBRate;
}

public void setCityZoneBRate(BigDecimal v) {
    this.cityZoneBRate = v;
}

public BigDecimal getCityAfter19Surcharge() {
    return cityAfter19Surcharge;
}

public void setCityAfter19Surcharge(BigDecimal v) {
    this.cityAfter19Surcharge = v;
}

public String getUiTheme() {
    return uiTheme;
}

public void setUiTheme(String uiTheme) {
    this.uiTheme = uiTheme;
}

public LocalDate getHireDate() {
    return hireDate;
}

public void setHireDate(LocalDate hireDate) {
    this.hireDate = hireDate;
}

public BigDecimal getSeniorityBonus1Year() {
    return seniorityBonus1Year;
}

public void setSeniorityBonus1Year(BigDecimal seniorityBonus1Year) {
    this.seniorityBonus1Year = seniorityBonus1Year;
}

public BigDecimal getSeniorityBonus2Years() {
    return seniorityBonus2Years;
}

public void setSeniorityBonus2Years(BigDecimal seniorityBonus2Years) {
    this.seniorityBonus2Years = seniorityBonus2Years;
}

public BigDecimal getSeniorityBonus3Years() {
    return seniorityBonus3Years;
}

public void setSeniorityBonus3Years(BigDecimal seniorityBonus3Years) {
    this.seniorityBonus3Years = seniorityBonus3Years;
}
}