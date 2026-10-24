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

/**
 * v2 -> v3: removes `pay_component` and `employee_pay_component_history` (the "Komponen Gaji
 * Default"/PayComponent feature - unused catalog screen, never wired to navigation, replaced by
 * the global Payroll Setup config). `employee_incentive.payComponentId` referenced `pay_component`
 * via foreign key (RESTRICT); that table is kept (actively used by PreviewPayrollUseCase and by
 * the already-approved staged payroll-unification plan, Tahap 7), so its table is rebuilt here
 * WITHOUT that foreign key instead of being dropped. The `payComponentId` column itself is kept
 * (not removed) - only the FK constraint to the now-deleted table goes away.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `employee_incentive_new` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `employeeId` INTEGER NOT NULL,
                `date` TEXT NOT NULL,
                `payComponentId` INTEGER NOT NULL,
                `amount` INTEGER NOT NULL,
                `workOrderId` INTEGER,
                `referenceId` INTEGER,
                `reason` TEXT,
                `approvedBy` INTEGER,
                `notes` TEXT,
                FOREIGN KEY(`employeeId`) REFERENCES `employee`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `employee_incentive_new`
            SELECT `id`, `employeeId`, `date`, `payComponentId`, `amount`, `workOrderId`, `referenceId`, `reason`, `approvedBy`, `notes`
            FROM `employee_incentive`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `employee_incentive`")
        db.execSQL("ALTER TABLE `employee_incentive_new` RENAME TO `employee_incentive`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_employee_incentive_employeeId_date` ON `employee_incentive` (`employeeId`, `date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_employee_incentive_payComponentId` ON `employee_incentive` (`payComponentId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_employee_incentive_workOrderId` ON `employee_incentive` (`workOrderId`)")

        db.execSQL("DROP TABLE IF EXISTS `employee_pay_component_history`")
        db.execSQL("DROP TABLE IF EXISTS `pay_component`")
    }
}

/**
 * v3 -> v4: adds `employee_variable_salary_history` (Gaji Variable / GV riwayat: uangMakan/UM,
 * uangTransport/UT, uangHadir/UH per tanggal berlaku, satu baris per karyawan per perubahan -
 * pola yang sama dengan `employee_salary_history`). No existing table is touched. Authorized by
 * user for the Kompensasi UI redesign (tab Variable), see mod report.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `employee_variable_salary_history` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `employeeId` INTEGER NOT NULL,
                `effectiveDate` TEXT NOT NULL,
                `uangMakan` INTEGER NOT NULL,
                `uangTransport` INTEGER NOT NULL,
                `uangHadir` INTEGER NOT NULL,
                `reason` TEXT,
                FOREIGN KEY(`employeeId`) REFERENCES `employee`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_employee_variable_salary_history_employeeId_effectiveDate` ON `employee_variable_salary_history` (`employeeId`, `effectiveDate`)")
    }
}

/**
 * v4 -> v5: Edit Karyawan redesign (authorized by user, see mod report) -
 * - `employee`: adds nullable `payrollType` (Bulanan/Harian), `memberType` (Pertalite/Pertamax),
 *   `emergencyContact` (free text), `overtimeRate` (UL), `overtimePremium` (PL). All nullable -
 *   existing rows get NULL, no backfill/guessing. UL/PL null means "not yet explicitly saved",
 *   UI falls back to showing GPPH as a default, not stored as a real value until edited.
 * - `employee_salary_history`: adds `positionAllowance` (TJ) NOT NULL DEFAULT 0 - existing rows
 *   get 0 (equivalent to "no TJ"), which is correct for rows predating this field.
 * - new `incentive_type` table (Payroll Setup "Pengaturan Nama Insentif" catalog). No foreign key
 *   from `employee_incentive.payComponentId` to it - that column has been a plain, non-relational
 *   Long since the original PayComponent deletion; it is now reused to reference this new catalog
 *   going forward, without a rename (avoids another destructive table rebuild for a cosmetic name).
 * No existing table is dropped or has data removed.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `employee` ADD COLUMN `payrollType` TEXT")
        db.execSQL("ALTER TABLE `employee` ADD COLUMN `memberType` TEXT")
        db.execSQL("ALTER TABLE `employee` ADD COLUMN `emergencyContact` TEXT")
        db.execSQL("ALTER TABLE `employee` ADD COLUMN `overtimeRate` INTEGER")
        db.execSQL("ALTER TABLE `employee` ADD COLUMN `overtimePremium` INTEGER")
        db.execSQL("ALTER TABLE `employee_salary_history` ADD COLUMN `positionAllowance` INTEGER NOT NULL DEFAULT 0")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `incentive_type` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `name` TEXT NOT NULL,
                `description` TEXT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_incentive_type_name` ON `incentive_type` (`name`)")
    }
}
