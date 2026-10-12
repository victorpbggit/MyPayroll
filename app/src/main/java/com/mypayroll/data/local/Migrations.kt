package com.mypayroll.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: adds payroll_period, payroll_result and payroll_approval tables.
 * No existing table is touched.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `payroll_period` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `code` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `startDate` TEXT NOT NULL,
                `endDate` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `createdAt` TEXT NOT NULL,
                `finalizedAt` TEXT,
                `finalizedBy` INTEGER,
                `notes` TEXT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_payroll_period_code` ON `payroll_period` (`code`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `payroll_result` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `payrollPeriodId` INTEGER NOT NULL,
                `employeeId` INTEGER NOT NULL,
                `basicSalary` INTEGER NOT NULL,
                `totalAllowance` INTEGER NOT NULL,
                `totalIncentive` INTEGER NOT NULL,
                `totalOvertimeAmount` INTEGER NOT NULL,
                `attendanceDeduction` INTEGER NOT NULL,
                `totalDeduction` INTEGER NOT NULL,
                `grossPay` INTEGER NOT NULL,
                `netPay` INTEGER NOT NULL,
                `status` TEXT NOT NULL,
                `calculatedAt` TEXT NOT NULL,
                `notes` TEXT,
                FOREIGN KEY(`payrollPeriodId`) REFERENCES `payroll_period`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`employeeId`) REFERENCES `employee`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_payroll_result_payrollPeriodId_employeeId` ON `payroll_result` (`payrollPeriodId`, `employeeId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payroll_result_employeeId` ON `payroll_result` (`employeeId`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `payroll_approval` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `payrollPeriodId` INTEGER NOT NULL,
                `action` TEXT NOT NULL,
                `actedBy` INTEGER,
                `actedAt` TEXT NOT NULL,
                `notes` TEXT,
                FOREIGN KEY(`payrollPeriodId`) REFERENCES `payroll_period`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payroll_approval_payrollPeriodId` ON `payroll_approval` (`payrollPeriodId`)")
    }
}
