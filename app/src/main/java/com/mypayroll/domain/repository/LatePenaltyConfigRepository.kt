package com.mypayroll.domain.repository

import com.mypayroll.domain.model.LatePenaltyConfig
import kotlinx.coroutines.flow.Flow

interface LatePenaltyConfigRepository {
    fun observeConfig(): Flow<LatePenaltyConfig>
    suspend fun getConfig(): LatePenaltyConfig

    /** Menyimpan [config]. Melempar [IllegalArgumentException] jika ada tier < Rp1.000. */
    suspend fun saveConfig(config: LatePenaltyConfig)
}
