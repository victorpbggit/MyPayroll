package com.mypayroll.domain.repository

import com.mypayroll.domain.model.OvertimeRuleConfig
import kotlinx.coroutines.flow.Flow

interface OvertimeRuleConfigRepository {
    fun observeConfig(): Flow<OvertimeRuleConfig>
    suspend fun getConfig(): OvertimeRuleConfig

    /** Menyimpan [config]. Melempar [IllegalArgumentException] jika rule1/rule2 < 100. */
    suspend fun saveConfig(config: OvertimeRuleConfig)
}
