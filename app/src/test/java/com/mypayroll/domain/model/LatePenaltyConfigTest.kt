package com.mypayroll.domain.model

import org.junit.Test

class LatePenaltyConfigTest {

    @Test
    fun `exactly Rp1000 for every tier is valid`() {
        LatePenaltyConfig(1_000L, 1_000L, 1_000L).validate()
    }

    @Test
    fun `above Rp1000 is valid`() {
        LatePenaltyConfig(5_000L, 10_000L, 20_000L).validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `tier 1 below Rp1000 is rejected`() {
        LatePenaltyConfig(tier1AmountMin1000 = 999L).validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `tier 2 below Rp1000 is rejected`() {
        LatePenaltyConfig(tier2AmountMin1000 = 500L).validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `tier 3 below Rp1000 is rejected`() {
        LatePenaltyConfig(tier3AmountMin1000 = 0L).validate()
    }
}
