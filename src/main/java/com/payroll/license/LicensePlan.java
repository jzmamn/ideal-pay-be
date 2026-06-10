package com.payroll.license;

public enum LicensePlan {
    STANDARD(25), PROFESSIONAL(50), PREMIUM(100), ELITE(250), ULTIMATE(1000);

    private final int employeeLimit;

    LicensePlan(int employeeLimit) { this.employeeLimit = employeeLimit; }
    public int getEmployeeLimit() { return employeeLimit; }
}
