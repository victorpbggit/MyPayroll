package com.mypayroll.domain.model

import org.junit.Test

class OvertimeRuleConfigTest {

    @Test
    fun `100 percent for rule 1 and 2 is valid`() {
        OvertimeRuleConfig(rule1PercentMin100 = 100, rule2PercentMin100 = 100, holidayPercent = 0).validate()
    }

    @Test
    fun `above 100 percent is valid`() {
        OvertimeRuleConfig(rule1PercentMin100 = 150, rule2PercentMin100 = 200, holidayPercent = 50).validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rule 1 below 100 is rejected`() {
        OvertimeRuleConfig(rule1PercentMin100 = 99).validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rule 2 below 100 is rejected`() {
        OvertimeRuleConfig(rule2PercentMin100 = 99).validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative holiday percent is rejected`() {
        OvertimeRuleConfig(holidayPercent = -1).validate()
    }
}
