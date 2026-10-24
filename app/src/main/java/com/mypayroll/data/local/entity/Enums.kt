package com.mypayroll.data.local.entity

enum class EmployeeStatus { ACTIVE, RESIGNED, INACTIVE }
enum class Gender { MALE, FEMALE }
enum class SalaryType { MONTHLY, DAILY, HOURLY }
enum class ContractStatus { ACTIVE, EXPIRED, TERMINATED }
enum class DisciplinaryActionType { VERBAL_WARNING, WRITTEN_WARNING, SP1, SP2, SP3, OTHER }
enum class AttendanceStatus { PRESENT, LATE, ABSENT, PERMITTED, SICK, UNEXCUSED, HOLIDAY, LEAVE }
enum class AttendanceExceptionType { PERMITTED, SICK, UNEXCUSED, LATE_WITH_PERMISSION, EARLY_LEAVE_WITH_PERMISSION }
enum class ApprovalStatus { PENDING, APPROVED, REJECTED, CANCELLED }
enum class OvertimeAssignmentStatus { ASSIGNED, ACCEPTED, REJECTED, CANCELLED }
enum class OvertimeRecordStatus { PENDING, APPROVED, REJECTED }
enum class PayrollType { MONTHLY, DAILY }
enum class MemberType { PERTALITE, PERTAMAX }
