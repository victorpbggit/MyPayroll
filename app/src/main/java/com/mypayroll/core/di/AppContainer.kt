package com.mypayroll.core.di

import android.content.Context
import com.mypayroll.data.local.AppDatabase
import com.mypayroll.data.repository.AttendanceExceptionRepositoryImpl
import com.mypayroll.data.repository.AttendanceRepositoryImpl
import com.mypayroll.data.repository.ContractTypeRepositoryImpl
import com.mypayroll.data.repository.DepartmentRepositoryImpl
import com.mypayroll.data.repository.DocumentRepositoryImpl
import com.mypayroll.data.repository.EmployeeContractRepositoryImpl
import com.mypayroll.data.repository.EmployeeDisciplinaryActionRepositoryImpl
import com.mypayroll.data.repository.EmployeeIncentiveRepositoryImpl
import com.mypayroll.data.repository.EmployeePositionHistoryRepositoryImpl
import com.mypayroll.data.repository.EmployeeRepositoryImpl
import com.mypayroll.data.repository.EmployeeSalaryHistoryRepositoryImpl
import com.mypayroll.data.repository.EmployeeVariableSalaryHistoryRepositoryImpl
import com.mypayroll.data.repository.IncentiveTypeRepositoryImpl
import com.mypayroll.data.repository.LatePenaltyConfigRepositoryImpl
import com.mypayroll.data.repository.PayrollGeneralSettingsRepositoryImpl
import com.mypayroll.data.repository.LeaveRequestRepositoryImpl
import com.mypayroll.data.repository.LeaveTypeRepositoryImpl
import com.mypayroll.data.repository.OvertimeAssignmentRepositoryImpl
import com.mypayroll.data.repository.OvertimeRecordRepositoryImpl
import com.mypayroll.data.repository.OvertimeRuleConfigRepositoryImpl
import com.mypayroll.data.repository.PayrollApprovalRepositoryImpl
import com.mypayroll.data.repository.PayrollCalendarRepositoryImpl
import com.mypayroll.data.repository.PayrollPeriodRepositoryImpl
import com.mypayroll.data.repository.PayrollResultRepositoryImpl
import com.mypayroll.data.repository.PositionRepositoryImpl
import com.mypayroll.domain.repository.AttendanceExceptionRepository
import com.mypayroll.domain.repository.AttendanceRepository
import com.mypayroll.domain.repository.ContractTypeRepository
import com.mypayroll.domain.repository.DepartmentRepository
import com.mypayroll.domain.repository.DocumentRepository
import com.mypayroll.domain.repository.EmployeeContractRepository
import com.mypayroll.domain.repository.EmployeeDisciplinaryActionRepository
import com.mypayroll.domain.repository.EmployeeIncentiveRepository
import com.mypayroll.domain.repository.EmployeePositionHistoryRepository
import com.mypayroll.domain.repository.EmployeeRepository
import com.mypayroll.domain.repository.EmployeeSalaryHistoryRepository
import com.mypayroll.domain.repository.EmployeeVariableSalaryHistoryRepository
import com.mypayroll.domain.repository.IncentiveTypeRepository
import com.mypayroll.domain.repository.LatePenaltyConfigRepository
import com.mypayroll.domain.repository.PayrollGeneralSettingsRepository
import com.mypayroll.domain.repository.LeaveRequestRepository
import com.mypayroll.domain.repository.LeaveTypeRepository
import com.mypayroll.domain.repository.OvertimeAssignmentRepository
import com.mypayroll.domain.repository.OvertimeRecordRepository
import com.mypayroll.domain.repository.OvertimeRuleConfigRepository
import com.mypayroll.domain.repository.PayrollApprovalRepository
import com.mypayroll.domain.repository.PayrollCalendarRepository
import com.mypayroll.domain.repository.PayrollPeriodRepository
import com.mypayroll.domain.repository.PayrollResultRepository
import com.mypayroll.domain.repository.PositionRepository

/**
 * Manual dependency container. The project has no DI framework (Hilt/Koin) wired in,
 * so this is the single place that turns [AppDatabase] DAOs into repositories used
 * by use cases / ViewModels across every feature module.
 */
class AppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)

    val employeeRepository: EmployeeRepository = EmployeeRepositoryImpl(database.employeeDao())
    val departmentRepository: DepartmentRepository = DepartmentRepositoryImpl(database.departmentDao())
    val positionRepository: PositionRepository = PositionRepositoryImpl(database.positionDao())
    val contractTypeRepository: ContractTypeRepository = ContractTypeRepositoryImpl(database.contractTypeDao())
    val employeeContractRepository: EmployeeContractRepository = EmployeeContractRepositoryImpl(database.employeeContractDao())
    val employeePositionHistoryRepository: EmployeePositionHistoryRepository =
        EmployeePositionHistoryRepositoryImpl(database.employeePositionHistoryDao())

    val attendanceRepository: AttendanceRepository = AttendanceRepositoryImpl(database.attendanceDao())
    val attendanceExceptionRepository: AttendanceExceptionRepository =
        AttendanceExceptionRepositoryImpl(database.attendanceExceptionDao())
    val leaveTypeRepository: LeaveTypeRepository = LeaveTypeRepositoryImpl(database.leaveTypeDao())
    val leaveRequestRepository: LeaveRequestRepository = LeaveRequestRepositoryImpl(database.leaveRequestDao())

    val overtimeAssignmentRepository: OvertimeAssignmentRepository =
        OvertimeAssignmentRepositoryImpl(database.overtimeAssignmentDao())
    val overtimeRecordRepository: OvertimeRecordRepository = OvertimeRecordRepositoryImpl(database.overtimeRecordDao())

    val employeeSalaryHistoryRepository: EmployeeSalaryHistoryRepository =
        EmployeeSalaryHistoryRepositoryImpl(database.employeeSalaryHistoryDao())
    val employeeVariableSalaryHistoryRepository: EmployeeVariableSalaryHistoryRepository =
        EmployeeVariableSalaryHistoryRepositoryImpl(database.employeeVariableSalaryHistoryDao())
    val employeeIncentiveRepository: EmployeeIncentiveRepository =
        EmployeeIncentiveRepositoryImpl(database.employeeIncentiveDao())
    val employeeDisciplinaryActionRepository: EmployeeDisciplinaryActionRepository =
        EmployeeDisciplinaryActionRepositoryImpl(database.employeeDisciplinaryActionDao())
    val incentiveTypeRepository: IncentiveTypeRepository = IncentiveTypeRepositoryImpl(database.incentiveTypeDao())

    val documentRepository: DocumentRepository = DocumentRepositoryImpl(database.documentDao())

    val payrollPeriodRepository: PayrollPeriodRepository = PayrollPeriodRepositoryImpl(database.payrollPeriodDao())
    val payrollResultRepository: PayrollResultRepository = PayrollResultRepositoryImpl(database.payrollResultDao())
    val payrollApprovalRepository: PayrollApprovalRepository = PayrollApprovalRepositoryImpl(database.payrollApprovalDao())
    val payrollCalendarRepository: PayrollCalendarRepository = PayrollCalendarRepositoryImpl(context)
    val overtimeRuleConfigRepository: OvertimeRuleConfigRepository = OvertimeRuleConfigRepositoryImpl(context)
    val latePenaltyConfigRepository: LatePenaltyConfigRepository = LatePenaltyConfigRepositoryImpl(context)
    val payrollGeneralSettingsRepository: PayrollGeneralSettingsRepository = PayrollGeneralSettingsRepositoryImpl(context)

    companion object {
        @Volatile
        private var INSTANCE: AppContainer? = null

        fun getInstance(context: Context): AppContainer =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppContainer(context.applicationContext).also { INSTANCE = it }
            }
    }
}
