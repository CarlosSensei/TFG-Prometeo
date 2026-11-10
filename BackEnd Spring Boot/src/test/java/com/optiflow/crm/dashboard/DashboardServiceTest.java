package com.optiflow.crm.dashboard;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardServiceTest {
    @Test
    void zeroResolvedQuotesReturnsZeroConversion() {
        assertEquals(new BigDecimal("0.0"), DashboardService.conversionRate(0, 0));
    }

    @Test
    void conversionIsCalculatedWithOneDecimal() {
        assertEquals(new BigDecimal("33.3"), DashboardService.conversionRate(1, 3));
    }

    @Test
    void conversionCanReachOneHundredPercent() {
        assertEquals(new BigDecimal("100.0"), DashboardService.conversionRate(7, 7));
    }
}
