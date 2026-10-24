package com.mypayroll.domain.payroll

import com.mypayroll.domain.model.LatePenaltyConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class LatePenaltyCalculatorTest {

    private val config = LatePenaltyConfig(
        tier1AmountMin1000 = 1_000L,
        tier2AmountMin1000 = 1_000L,
        tier3AmountMin1000 = 1_000L
    )

    @Test
    fun `below 5 minutes has no penalty`() {
        assertEquals(0L, LatePenaltyCalculator.calculate(4, config))
    }

    @Test
    fun `exactly 5 minutes applies tier 1 only`() {
        assertEquals(1_000L, LatePenaltyCalculator.calculate(5, config))
    }

    @Test
    fun `14 minutes still tier 1 only`() {
        assertEquals(1_000L, LatePenaltyCalculator.calculate(14, config))
    }

    @Test
    fun `exactly 15 minutes applies tier 1 plus tier 2`() {
        assertEquals(2_000L, LatePenaltyCalculator.calculate(15, config))
    }

    @Test
    fun `29 minutes still tier 1 plus tier 2`() {
        assertEquals(2_000L, LatePenaltyCalculator.calculate(29, config))
    }

    @Test
    fun `exactly 30 minutes applies all three tiers`() {
        assertEquals(3_000L, LatePenaltyCalculator.calculate(30, config))
    }

    @Test
    fun `60 minutes still applies all three tiers (cumulative, not re-triggered)`() {
        assertEquals(3_000L, LatePenaltyCalculator.calculate(60, config))
    }

    @Test
    fun `different tier amounts accumulate correctly`() {
        val custom = LatePenaltyConfig(tier1AmountMin1000 = 2_000L, tier2AmountMin1000 = 3_000L, tier3AmountMin1000 = 5_000L)
        assertEquals(0L, LatePenaltyCalculator.calculate(0, custom))
        assertEquals(2_000L, LatePenaltyCalculator.calculate(5, custom))
        assertEquals(5_000L, LatePenaltyCalculator.calculate(15, custom))
        assertEquals(10_000L, LatePenaltyCalculator.calculate(30, custom))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative late minutes is rejected`() {
        LatePenaltyCalculator.calculate(-1, config)
    }
}
