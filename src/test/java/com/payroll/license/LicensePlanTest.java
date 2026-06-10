package com.payroll.license;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LicensePlanTest {
    @Test void plansHaveExpectedLimits() {
        assertEquals(25, LicensePlan.STANDARD.getEmployeeLimit());
        assertEquals(50, LicensePlan.PROFESSIONAL.getEmployeeLimit());
        assertEquals(100, LicensePlan.PREMIUM.getEmployeeLimit());
        assertEquals(250, LicensePlan.ELITE.getEmployeeLimit());
        assertEquals(1000, LicensePlan.ULTIMATE.getEmployeeLimit());
    }
}
