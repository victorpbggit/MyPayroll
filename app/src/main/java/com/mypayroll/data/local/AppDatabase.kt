package com.mypayroll.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mypayroll.data.local.converter.RoomConverters
import com.mypayroll.data.local.entity.AttendanceEntity
import com.mypayroll.data.local.entity.AttendanceExceptionEntity
import com.mypayroll.data.local.entity.ContractTypeEntity
import com.mypayroll.data.local.entity.DepartmentEntity
import com.mypayroll.data.local.entity.DocumentEntity
import com.mypayroll.data.local.entity.EmployeeContractEntity
import com.mypayroll.data.local.entity.EmployeeDisciplinaryActionEntity
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.data.local.entity.EmployeeIncentiveEntity
import com.mypayroll.data.local.entity.EmployeePositionHistoryEntity
import com.mypayroll.data.local.entity.EmployeeSalaryHistoryEntity
import com.mypayroll.data.local.entity.EmployeeVariableSalaryHistoryEntity
import com.mypayroll.data.local.entity.IncentiveTypeEntity
import com.mypayroll.data.local.entity.LeaveRequestEntity
import com.mypayroll.data.local.entity.LeaveTypeEntity
import com.mypayroll.data.local.entity.OvertimeAssignmentEntity
import com.mypayroll.data.local.entity.OvertimeRecordEntity
import com.mypayroll.data.local.entity.PayrollApprovalEntity
import com.mypayroll.data.local.entity.PayrollPeriodEntity
import com.mypayroll.data.local.entity.PayrollResultEntity
import com.mypayroll.data.local.entity.PositionEntity
import com.mypayroll.data.local.dao.AttendanceDao
import com.mypayroll.data.local.dao.AttendanceExceptionDao
import com.mypayroll.data.local.dao.ContractTypeDao
import com.mypayroll.data.local.dao.DepartmentDao
import com.mypayroll.data.local.dao.DocumentDao
import com.mypayroll.data.local.dao.EmployeeContractDao
import com.mypayroll.data.local.dao.EmployeeDao
import com.mypayroll.data.local.dao.EmployeeDisciplinaryActionDao
import com.mypayroll.data.local.dao.EmployeeIncentiveDao
import com.mypayroll.data.local.dao.EmployeePositionHistoryDao
import com.mypayroll.data.local.dao.EmployeeSalaryHistoryDao
import com.mypayroll.data.local.dao.EmployeeVariableSalaryHistoryDao
import com.mypayroll.data.local.dao.IncentiveTypeDao
import com.mypayroll.data.local.dao.LeaveRequestDao
import com.mypayroll.data.local.dao.LeaveTypeDao
import com.mypayroll.data.local.dao.OvertimeAssignmentDao
import com.mypayroll.data.local.dao.OvertimeRecordDao
import com.mypayroll.data.local.dao.PayrollApprovalDao
import com.mypayroll.data.local.dao.PayrollPeriodDao
import com.mypayroll.data.local.dao.PayrollResultDao
import com.mypayroll.data.local.dao.PositionDao

@Database(
    entities = [
        AttendanceEntity::class,
        AttendanceExceptionEntity::class,
        ContractTypeEntity::class,
        DepartmentEntity::class,
        DocumentEntity::class,
        EmployeeContractEntity::class,
        EmployeeDisciplinaryActionEntity::class,
        EmployeeEntity::class,
        EmployeeIncentiveEntity::class,
        EmployeePositionHistoryEntity::class,
        EmployeeSalaryHistoryEntity::class,
        EmployeeVariableSalaryHistoryEntity::class,
        IncentiveTypeEntity::class,
        LeaveRequestEntity::class,
        LeaveTypeEntity::class,
        OvertimeAssignmentEntity::class,
        OvertimeRecordEntity::class,
        PositionEntity::class,
        PayrollPeriodEntity::class,
        PayrollResultEntity::class,
        PayrollApprovalEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun attendanceDao(): AttendanceDao
    abstract fun attendanceExceptionDao(): AttendanceExceptionDao
    abstract fun contractTypeDao(): ContractTypeDao
    abstract fun departmentDao(): DepartmentDao
    abstract fun documentDao(): DocumentDao
    abstract fun employeeContractDao(): EmployeeContractDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun employeeDisciplinaryActionDao(): EmployeeDisciplinaryActionDao
    abstract fun employeeIncentiveDao(): EmployeeIncentiveDao
    abstract fun employeePositionHistoryDao(): EmployeePositionHistoryDao
    abstract fun employeeSalaryHistoryDao(): EmployeeSalaryHistoryDao
    abstract fun employeeVariableSalaryHistoryDao(): EmployeeVariableSalaryHistoryDao
    abstract fun incentiveTypeDao(): IncentiveTypeDao
    abstract fun leaveRequestDao(): LeaveRequestDao
    abstract fun leaveTypeDao(): LeaveTypeDao
    abstract fun overtimeAssignmentDao(): OvertimeAssignmentDao
    abstract fun overtimeRecordDao(): OvertimeRecordDao
    abstract fun positionDao(): PositionDao
    abstract fun payrollPeriodDao(): PayrollPeriodDao
    abstract fun payrollResultDao(): PayrollResultDao
    abstract fun payrollApprovalDao(): PayrollApprovalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vic_business.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { INSTANCE = it }
            }
    }
}
