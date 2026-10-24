package com.mypayroll.presentation.compensation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.EmployeeIncentiveEntity
import com.mypayroll.data.local.entity.EmployeeSalaryHistoryEntity
import com.mypayroll.data.local.entity.EmployeeVariableSalaryHistoryEntity
import com.mypayroll.data.local.entity.IncentiveTypeEntity
import com.mypayroll.data.local.entity.PayrollType
import com.mypayroll.data.local.entity.SalaryType
import com.mypayroll.domain.model.EmployeeProfile
import com.mypayroll.domain.model.PayrollGeneralSettings
import com.mypayroll.domain.payroll.gajiPokokPerHari
import com.mypayroll.domain.repository.EmployeeIncentiveRepository
import com.mypayroll.domain.repository.EmployeeRepository
import com.mypayroll.domain.repository.EmployeeSalaryHistoryRepository
import com.mypayroll.domain.repository.EmployeeVariableSalaryHistoryRepository
import com.mypayroll.domain.repository.IncentiveTypeRepository
import com.mypayroll.domain.repository.PayrollGeneralSettingsRepository
import com.mypayroll.domain.usecase.ObserveEmployeeProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EmployeeCompensationUiState(
    val profile: EmployeeProfile? = null,
    val salaryHistory: List<EmployeeSalaryHistoryEntity> = emptyList(),
    val variableSalaryHistory: List<EmployeeVariableSalaryHistoryEntity> = emptyList(),
    val incentives: List<EmployeeIncentiveEntity> = emptyList(),
    val incentiveTypes: List<IncentiveTypeEntity> = emptyList(),
    val generalSettings: PayrollGeneralSettings = PayrollGeneralSettings(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val infoMessage: String? = null
) {
    val latestSalary: EmployeeSalaryHistoryEntity?
        get() = salaryHistory.maxByOrNull { it.effectiveDate }

    val latestVariableSalary: EmployeeVariableSalaryHistoryEntity?
        get() = variableSalaryHistory.maxByOrNull { it.effectiveDate }

    /** True only once loading has finished AND no employee with this id exists. */
    val employeeNotFound: Boolean
        get() = !isLoading && profile == null

    val isPayrollTypeMonthly: Boolean
        get() = profile?.employee?.payrollType == PayrollType.MONTHLY

    /** GPPH saat ini (GP terbaru / DM) - basis default UL/PL dan pembagi harian GV. */
    val gpph: Long
        get() = gajiPokokPerHari(latestSalary?.basicSalary ?: 0L, generalSettings.daysPerMonth)

    val overtimeRateOrDefault: Long
        get() = profile?.employee?.overtimeRate ?: gpph

    val overtimePremiumOrDefault: Long
        get() = profile?.employee?.overtimePremium ?: gpph

    /**
     * Total Kompensasi = (GP+TJ) + estimasi GV bulanan (UM/UT/UH harian x DM) + total Insentif
     * (seluruh riwayat). Tidak termasuk Lembur (UL/PL) - itu hanya info pengaturan tarif, bukan
     * komponen yang "didapat" setiap bulan. Null jika GP belum ada sama sekali (tidak ada basis
     * untuk menghitung apa pun).
     */
    val totalKompensasi: Long?
        get() {
            val salary = latestSalary ?: return null
            val gpTj = salary.basicSalary + salary.positionAllowance
            val gvMonthlyEstimate = latestVariableSalary?.let {
                (it.uangMakan + it.uangTransport + it.uangHadir) * generalSettings.daysPerMonth
            } ?: 0L
            val totalInsentif = incentives.sumOf { it.amount }
            return gpTj + gvMonthlyEstimate + totalInsentif
        }
}

private data class CoreData(
    val profile: EmployeeProfile?,
    val salaryHistory: List<EmployeeSalaryHistoryEntity>,
    val variableSalaryHistory: List<EmployeeVariableSalaryHistoryEntity>,
    val incentives: List<EmployeeIncentiveEntity>,
    val generalSettings: PayrollGeneralSettings
)

private data class CoreData2(
    val core: CoreData,
    val incentiveTypes: List<IncentiveTypeEntity>
)

/**
 * Hub kompensasi satu karyawan (5 tab: Ringkasan/Gaji Pokok(GP)/Gaji Variable(GV)/Lembur/Insentif).
 *
 * - Gaji Pokok (GP) + Tunjangan Jabatan (TJ): satu riwayat effective-dated di
 *   [EmployeeSalaryHistoryRepository] (GP selalu bulanan; TJ hanya berlaku jika
 *   employee.payrollType == MONTHLY, 0 untuk Harian).
 * - Gaji Variable (GV = UM+UT+UH harian): [EmployeeVariableSalaryHistoryRepository].
 * - Lembur (UL/PL): bukan riwayat - satu nilai per karyawan di [EmployeeEntity.overtimeRate]/
 *   [EmployeeEntity.overtimePremium], default GPPH saat belum pernah disimpan eksplisit.
 * - Insentif: [EmployeeIncentiveRepository], dengan nama insentif dari katalog global
 *   [IncentiveTypeRepository] ("Pengaturan Nama Insentif" di Payroll Setup).
 */
class EmployeeCompensationViewModel(
    private val employeeId: Long,
    private val observeEmployeeProfileUseCase: ObserveEmployeeProfileUseCase,
    private val employeeRepository: EmployeeRepository,
    private val salaryHistoryRepository: EmployeeSalaryHistoryRepository,
    private val variableSalaryHistoryRepository: EmployeeVariableSalaryHistoryRepository,
    private val incentiveRepository: EmployeeIncentiveRepository,
    private val incentiveTypeRepository: IncentiveTypeRepository,
    private val generalSettingsRepository: PayrollGeneralSettingsRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _infoMessage = MutableStateFlow<String?>(null)

    val uiState = combine(
        combine(
            combine(
                observeEmployeeProfileUseCase(employeeId),
                salaryHistoryRepository.observeByEmployee(employeeId),
                variableSalaryHistoryRepository.observeByEmployee(employeeId),
                incentiveRepository.observeByEmployee(employeeId),
                generalSettingsRepository.observeSettings()
            ) { profile, salary, variableSalary, incentives, settings ->
                CoreData(profile, salary, variableSalary, incentives, settings)
            },
            incentiveTypeRepository.observeAll()
        ) { core, incentiveTypes -> CoreData2(core, incentiveTypes) },
        _errorMessage,
        _infoMessage
    ) { core2, error, info ->
        EmployeeCompensationUiState(
            profile = core2.core.profile,
            salaryHistory = core2.core.salaryHistory.sortedByDescending { it.effectiveDate },
            variableSalaryHistory = core2.core.variableSalaryHistory.sortedByDescending { it.effectiveDate },
            incentives = core2.core.incentives.sortedByDescending { it.date },
            incentiveTypes = core2.incentiveTypes,
            generalSettings = core2.core.generalSettings,
            isLoading = false,
            errorMessage = error,
            infoMessage = info
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EmployeeCompensationUiState())

    fun addSalary(effectiveDate: LocalDate, basicSalary: Long, positionAllowance: Long, reason: String?) {
        viewModelScope.launch {
            runCatching {
                require(basicSalary > 0L) { "Gaji pokok harus lebih dari Rp0." }
                require(positionAllowance >= 0L) { "Tunjangan Jabatan tidak boleh negatif." }
                salaryHistoryRepository.insert(
                    EmployeeSalaryHistoryEntity(
                        employeeId = employeeId,
                        effectiveDate = effectiveDate,
                        basicSalary = basicSalary,
                        salaryType = SalaryType.MONTHLY,
                        positionAllowance = positionAllowance,
                        reason = reason?.ifBlank { null }
                    )
                )
            }.onSuccess {
                _infoMessage.value = "Riwayat gaji pokok berhasil disimpan."
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan riwayat gaji" }
        }
    }

    fun addVariableSalary(effectiveDate: LocalDate, uangMakan: Long, uangTransport: Long, uangHadir: Long, reason: String?) {
        viewModelScope.launch {
            runCatching {
                require(uangMakan >= 0L && uangTransport >= 0L && uangHadir >= 0L) { "Nilai UM/UT/UH tidak boleh negatif." }
                require(uangMakan + uangTransport + uangHadir > 0L) { "Total Gaji Variable harus lebih dari Rp0." }
                variableSalaryHistoryRepository.insert(
                    EmployeeVariableSalaryHistoryEntity(
                        employeeId = employeeId,
                        effectiveDate = effectiveDate,
                        uangMakan = uangMakan,
                        uangTransport = uangTransport,
                        uangHadir = uangHadir,
                        reason = reason?.ifBlank { null }
                    )
                )
            }.onSuccess {
                _infoMessage.value = "Riwayat Gaji Variable berhasil disimpan."
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan Gaji Variable" }
        }
    }

    fun addIncentive(date: LocalDate, amount: Long, incentiveTypeId: Long, reason: String?) {
        viewModelScope.launch {
            runCatching {
                require(amount > 0L) { "Jumlah insentif harus lebih dari Rp0." }
                require(incentiveTypeId > 0L) { "Pilih nama insentif terlebih dahulu." }
                incentiveRepository.insert(
                    EmployeeIncentiveEntity(employeeId = employeeId, date = date, payComponentId = incentiveTypeId, amount = amount, reason = reason?.ifBlank { null })
                )
            }.onSuccess {
                _infoMessage.value = "Insentif berhasil disimpan."
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan insentif" }
        }
    }

    fun updateOvertimeRates(overtimeRate: Long, overtimePremium: Long) {
        viewModelScope.launch {
            val employee = uiState.value.profile?.employee
            if (employee == null) {
                _errorMessage.value = "Data karyawan belum siap, coba lagi."
                return@launch
            }
            runCatching {
                require(overtimeRate >= 0L && overtimePremium >= 0L) { "Upah Lembur dan Premi Lembur tidak boleh negatif." }
                employeeRepository.update(employee.copy(overtimeRate = overtimeRate, overtimePremium = overtimePremium))
            }.onSuccess {
                _infoMessage.value = "Upah Lembur dan Premi Lembur berhasil disimpan."
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan Upah/Premi Lembur" }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _infoMessage.value = null
    }
}
