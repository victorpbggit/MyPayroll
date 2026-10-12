package com.mypayroll.domain.repository

import com.mypayroll.domain.model.PayrollSetupConfig
import kotlinx.coroutines.flow.Flow

interface PayrollSetupRepository {
    fun observeConfig(): Flow<PayrollSetupConfig>
    suspend fun getConfig(): PayrollSetupConfig
    suspend fun saveConfig(config: PayrollSetupConfig)
}
