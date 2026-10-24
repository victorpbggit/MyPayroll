package com.mypayroll.domain.payroll

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class PayrollCalendarRulesTest {

    @Test
    fun `Monday through Saturday default to WORKDAY`() {
        // 2026-01-05 is a Monday.
        val monday = LocalDate.of(2026, 1, 5)
        for (offset in 0..5) {
            val date = monday.plusDays(offset.toLong())
            assertEquals("$date (${date.dayOfWeek})", PayrollDayStatus.WORKDAY, defaultPayrollDayStatus(date))
        }
    }

    @Test
    fun `Sunday defaults to HOLIDAY`() {
        val sunday = LocalDate.of(2026, 1, 11)
        assertEquals(PayrollDayStatus.HOLIDAY, defaultPayrollDayStatus(sunday))
    }
}
