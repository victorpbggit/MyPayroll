package com.mypayroll.data.local.converter

import androidx.room.TypeConverter
import com.mypayroll.data.local.entity.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class RoomConverters {

    @TypeConverter fun fromLocalDate(value: LocalDate?): String? = value?.toString()
    @TypeConverter fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()
    @TypeConverter fun toLocalDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)

    @TypeConverter fun fromLocalTime(value: LocalTime?): String? = value?.toString()
    @TypeConverter fun toLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter fun fromEmployeeStatus(v: EmployeeStatus?) = v?.name
    @TypeConverter fun toEmployeeStatus(v: String?) = v?.let(EmployeeStatus::valueOf)

    @TypeConverter fun fromGender(v: Gender?) = v?.name
    @TypeConverter fun toGender(v: String?) = v?.let(Gender::valueOf)

    @TypeConverter fun fromSalaryType(v: SalaryType?) = v?.name
    @TypeConverter fun toSalaryType(v: String?) = v?.let(SalaryType::valueOf)

    @TypeConverter fun fromContractStatus(v: ContractStatus?) = v?.name
    @TypeConverter fun toContractStatus(v: String?) = v?.let(ContractStatus::valueOf)

    @TypeConverter fun fromPayComponentCategory(v: PayComponentCategory?) = v?.name
    @TypeConverter fun toPayComponentCategory(v: String?) = v?.let(PayComponentCategory::valueOf)

    @TypeConverter fun fromCalculationType(v: CalculationType?) = v?.name
    @TypeConverter fun toCalculationType(v: String?) = v?.let(CalculationType::valueOf)

    @TypeConverter fun fromDisciplinaryActionType(v: DisciplinaryActionType?) = v?.name
    @TypeConverter fun toDisciplinaryActionType(v: String?) = v?.let(DisciplinaryActionType::valueOf)

    @TypeConverter fun fromAttendanceStatus(v: AttendanceStatus?) = v?.name
    @TypeConverter fun toAttendanceStatus(v: String?) = v?.let(AttendanceStatus::valueOf)

    @TypeConverter fun fromAttendanceExceptionType(v: AttendanceExceptionType?) = v?.name
    @TypeConverter fun toAttendanceExceptionType(v: String?) = v?.let(AttendanceExceptionType::valueOf)

    @TypeConverter fun fromApprovalStatus(v: ApprovalStatus?) = v?.name
    @TypeConverter fun toApprovalStatus(v: String?) = v?.let(ApprovalStatus::valueOf)

    @TypeConverter fun fromOvertimeAssignmentStatus(v: OvertimeAssignmentStatus?) = v?.name
    @TypeConverter fun toOvertimeAssignmentStatus(v: String?) = v?.let(OvertimeAssignmentStatus::valueOf)

    @TypeConverter fun fromOvertimeRecordStatus(v: OvertimeRecordStatus?) = v?.name
    @TypeConverter fun toOvertimeRecordStatus(v: String?) = v?.let(OvertimeRecordStatus::valueOf)

    @TypeConverter fun fromPayrollPeriodStatus(v: PayrollPeriodStatus?) = v?.name
    @TypeConverter fun toPayrollPeriodStatus(v: String?) = v?.let(PayrollPeriodStatus::valueOf)

    @TypeConverter fun fromPayrollResultStatus(v: PayrollResultStatus?) = v?.name
    @TypeConverter fun toPayrollResultStatus(v: String?) = v?.let(PayrollResultStatus::valueOf)

    @TypeConverter fun fromPayrollApprovalAction(v: PayrollApprovalAction?) = v?.name
    @TypeConverter fun toPayrollApprovalAction(v: String?) = v?.let(PayrollApprovalAction::valueOf)
}
