package com.mypayroll.domain.model

import com.mypayroll.data.local.entity.DepartmentEntity
import com.mypayroll.data.local.entity.EmployeeContractEntity
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.data.local.entity.EmployeePositionHistoryEntity
import com.mypayroll.data.local.entity.EmployeeSalaryHistoryEntity
import com.mypayroll.data.local.entity.PositionEntity

/**
 * Aggregated view of an employee used as the "hub" screen. Built in-memory from
 * several repositories since the DAOs expose no join queries.
 */
data class EmployeeProfile(
    val employee: EmployeeEntity,
    val currentPosition: PositionEntity?,
    val currentDepartment: DepartmentEntity?,
    val currentPositionHistory: EmployeePositionHistoryEntity?,
    val activeContract: EmployeeContractEntity?,
    val latestSalary: EmployeeSalaryHistoryEntity?
)
