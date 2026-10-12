package com.mypayroll.domain.usecase

import com.mypayroll.data.local.entity.ContractStatus
import com.mypayroll.domain.model.EmployeeProfile
import com.mypayroll.domain.repository.DepartmentRepository
import com.mypayroll.domain.repository.EmployeeContractRepository
import com.mypayroll.domain.repository.EmployeePositionHistoryRepository
import com.mypayroll.domain.repository.EmployeeRepository
import com.mypayroll.domain.repository.EmployeeSalaryHistoryRepository
import com.mypayroll.domain.repository.PositionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private data class EmployeeCore(
    val employee: com.mypayroll.data.local.entity.EmployeeEntity?,
    val positionHistories: List<com.mypayroll.data.local.entity.EmployeePositionHistoryEntity>,
    val contracts: List<com.mypayroll.data.local.entity.EmployeeContractEntity>,
    val salaries: List<com.mypayroll.data.local.entity.EmployeeSalaryHistoryEntity>
)

/**
 * Builds the employee "hub" profile by joining employee + current position/department +
 * active contract + latest salary in-memory, since none of the DAOs expose join queries.
 */
class ObserveEmployeeProfileUseCase(
    private val employeeRepository: EmployeeRepository,
    private val positionHistoryRepository: EmployeePositionHistoryRepository,
    private val contractRepository: EmployeeContractRepository,
    private val salaryHistoryRepository: EmployeeSalaryHistoryRepository,
    private val departmentRepository: DepartmentRepository,
    private val positionRepository: PositionRepository
) {
    operator fun invoke(employeeId: Long): Flow<EmployeeProfile?> {
        val coreFlow = combine(
            employeeRepository.observeAll(),
            positionHistoryRepository.observeByEmployee(employeeId),
            contractRepository.observeByEmployee(employeeId),
            salaryHistoryRepository.observeByEmployee(employeeId)
        ) { employees, positionHistories, contracts, salaries ->
            EmployeeCore(employees.firstOrNull { it.id == employeeId }, positionHistories, contracts, salaries)
        }

        return combine(coreFlow, departmentRepository.observeAll(), positionRepository.observeAll()) { core, departments, positions ->
            val employee = core.employee ?: return@combine null
            val currentPositionHistory = core.positionHistories
                .filter { it.endDate == null }
                .maxByOrNull { it.startDate }
                ?: core.positionHistories.maxByOrNull { it.startDate }
            val activeContract = core.contracts
                .filter { it.status == ContractStatus.ACTIVE }
                .maxByOrNull { it.startDate }
                ?: core.contracts.maxByOrNull { it.startDate }
            val latestSalary = core.salaries.maxByOrNull { it.effectiveDate }

            EmployeeProfile(
                employee = employee,
                currentPosition = currentPositionHistory?.let { h -> positions.firstOrNull { it.id == h.positionId } },
                currentDepartment = currentPositionHistory?.let { h -> departments.firstOrNull { it.id == h.departmentId } },
                currentPositionHistory = currentPositionHistory,
                activeContract = activeContract,
                latestSalary = latestSalary
            )
        }
    }
}
