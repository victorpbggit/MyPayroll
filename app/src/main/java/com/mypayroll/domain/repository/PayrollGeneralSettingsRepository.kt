package com.mypayroll.domain.repository

import com.mypayroll.domain.model.PayrollGeneralSettings
import kotlinx.coroutines.flow.Flow

interface PayrollGeneralSettingsRepository {
    fun observeSettings(): Flow<PayrollGeneralSettings>
    suspend fun getSettings(): PayrollGeneralSettings

    /** Menyimpan [settings]. Melempar [IllegalArgumentException] jika ada nilai tidak valid. */
    suspend fun saveSettings(settings: PayrollGeneralSettings)
}
