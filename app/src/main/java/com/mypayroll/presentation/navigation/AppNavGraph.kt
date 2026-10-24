package com.mypayroll.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.mypayroll.presentation.attendance.AttendanceScreen
import com.mypayroll.presentation.common.BackScaffold
import com.mypayroll.presentation.compensation.EmployeeCompensationScreen
import com.mypayroll.presentation.dashboard.DashboardQuickActions
import com.mypayroll.presentation.dashboard.DashboardScreen
import com.mypayroll.presentation.document.DocumentScreen
import com.mypayroll.presentation.employee.EmployeeDetailScreen
import com.mypayroll.presentation.employee.EmployeeEditScreen
import com.mypayroll.presentation.employee.EmployeeScreen
import com.mypayroll.presentation.employee.EmployeeViewModel
import com.mypayroll.presentation.leave.LeaveScreen
import com.mypayroll.presentation.more.MoreMenuItem
import com.mypayroll.presentation.more.MoreScreen
import com.mypayroll.presentation.organization.OrganizationScreen
import com.mypayroll.presentation.overtime.OvertimeScreen
import com.mypayroll.presentation.payroll.PayrollCalculatorScreen
import com.mypayroll.presentation.payroll.PayrollPeriodDetailScreen
import com.mypayroll.presentation.payroll.PayrollScreen
import com.mypayroll.presentation.payrollsetup.PayrollSetupScreen
import com.mypayroll.presentation.payrollsetup.calendar.PayrollCalendarScreen
import com.mypayroll.presentation.payrollsetup.generalsettings.PayrollGeneralSettingsScreen
import com.mypayroll.presentation.payrollsetup.incentivetypes.IncentiveTypeSettingsScreen
import com.mypayroll.presentation.payrollsetup.latepenalty.LatePenaltyScreen
import com.mypayroll.presentation.payrollsetup.overtimerules.OvertimeRulesScreen
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.domain.usecase.DeleteEmployeeUseCase
import com.mypayroll.domain.usecase.ObserveEmployeesUseCase
import com.mypayroll.domain.usecase.SaveEmployeeUseCase

private data class BottomDestination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomDestinations = listOf(
    BottomDestination(Routes.DASHBOARD, "Dashboard", Icons.Filled.Dashboard),
    BottomDestination(Routes.EMPLOYEES, "Karyawan", Icons.Filled.People),
    BottomDestination(Routes.ATTENDANCE, "Absensi", Icons.Filled.CalendarMonth),
    BottomDestination(Routes.PAYROLL, "Payroll", Icons.Filled.AttachMoney),
    BottomDestination(Routes.MORE, "Lainnya", Icons.Filled.MoreHoriz)
)

@Composable
fun AppNavGraph() {
    val navController = androidx.navigation.compose.rememberNavController()
    val container = LocalAppContainer.current

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            NavigationBar {
                bottomDestinations.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute?.hierarchy?.any { it.route == dest.route } == true,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    DashboardQuickActions(
                        onOpenEmployees = { navController.navigate(Routes.EMPLOYEES) },
                        onOpenAttendance = { navController.navigate(Routes.ATTENDANCE) },
                        onOpenLeave = { navController.navigate(Routes.LEAVE) },
                        onOpenOvertime = { navController.navigate(Routes.OVERTIME) },
                        onOpenPayroll = { navController.navigate(Routes.PAYROLL) }
                    )
                )
            }
            composable(Routes.EMPLOYEES) {
                val viewModel = androidx.compose.runtime.remember {
                    EmployeeViewModel(
                        ObserveEmployeesUseCase(container.employeeRepository),
                        SaveEmployeeUseCase(container.employeeRepository),
                        DeleteEmployeeUseCase(container.employeeRepository)
                    )
                }
                EmployeeScreen(viewModel, onOpenEmployee = { id -> navController.navigate(Routes.employeeDetail(id)) })
            }
            composable(
                Routes.EMPLOYEE_DETAIL,
                arguments = listOf(navArgument("employeeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val employeeId = backStackEntry.arguments?.getLong("employeeId") ?: return@composable
                EmployeeDetailScreen(
                    employeeId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.employeeEdit(id)) },
                    onOpenCompensation = { id -> navController.navigate(Routes.employeeCompensation(id)) }
                )
            }
            composable(
                Routes.EMPLOYEE_EDIT,
                arguments = listOf(navArgument("employeeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val employeeId = backStackEntry.arguments?.getLong("employeeId") ?: return@composable
                EmployeeEditScreen(employeeId, onDone = { navController.popBackStack() })
            }
            composable(
                Routes.EMPLOYEE_COMPENSATION,
                arguments = listOf(navArgument("employeeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val employeeId = backStackEntry.arguments?.getLong("employeeId") ?: return@composable
                EmployeeCompensationScreen(employeeId, onBack = { navController.popBackStack() })
            }
            composable(Routes.ATTENDANCE) { AttendanceScreen() }
            composable(Routes.LEAVE) {
                BackScaffold("Cuti", onBack = { navController.popBackStack() }) { padding2 ->
                    androidx.compose.foundation.layout.Box(Modifier.padding(padding2)) { LeaveScreen() }
                }
            }
            composable(Routes.OVERTIME) { OvertimeScreen() }
            composable(Routes.ORGANIZATION) {
                BackScaffold("Organisasi", onBack = { navController.popBackStack() }) { padding2 ->
                    androidx.compose.foundation.layout.Box(Modifier.padding(padding2)) { OrganizationScreen() }
                }
            }
            composable(Routes.DOCUMENTS) { DocumentScreen() }
            composable(Routes.MORE) {
                MoreScreen(
                    listOf(
                        MoreMenuItem("Cuti", "Pengajuan dan persetujuan cuti karyawan") { navController.navigate(Routes.LEAVE) },
                        MoreMenuItem("Lembur", "Penugasan dan pencatatan lembur") { navController.navigate(Routes.OVERTIME) },
                        MoreMenuItem("Organisasi & Kontrak", "Departemen, posisi, jenis kontrak, dan kontrak karyawan") { navController.navigate(Routes.ORGANIZATION) },
                        MoreMenuItem("Dokumen", "Metadata dokumen karyawan") { navController.navigate(Routes.DOCUMENTS) },
                        MoreMenuItem("Payroll Setup", "Calendar Hari Kerja & Holiday, Presentase Perhitungan Lembur, Penalty Keterlambatan") { navController.navigate(Routes.PAYROLL_SETUP) }
                    )
                )
            }
            composable(Routes.PAYROLL) {
                PayrollScreen(
                    onOpenPeriod = { id -> navController.navigate(Routes.payrollDetail(id)) },
                    onOpenSetup = { navController.navigate(Routes.PAYROLL_SETUP) }
                )
            }
            composable(Routes.PAYROLL_SETUP) {
                PayrollSetupScreen(
                    onBack = { navController.popBackStack() },
                    onOpenCalendar = { navController.navigate(Routes.PAYROLL_CALENDAR) },
                    onOpenOvertimeRules = { navController.navigate(Routes.PAYROLL_OVERTIME_RULES) },
                    onOpenLatePenalty = { navController.navigate(Routes.PAYROLL_LATE_PENALTY) },
                    onOpenGeneralSettings = { navController.navigate(Routes.PAYROLL_GENERAL_SETTINGS) },
                    onOpenIncentiveTypes = { navController.navigate(Routes.PAYROLL_INCENTIVE_TYPES) }
                )
            }
            composable(Routes.PAYROLL_CALENDAR) { PayrollCalendarScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PAYROLL_OVERTIME_RULES) { OvertimeRulesScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PAYROLL_LATE_PENALTY) { LatePenaltyScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PAYROLL_GENERAL_SETTINGS) { PayrollGeneralSettingsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PAYROLL_INCENTIVE_TYPES) { IncentiveTypeSettingsScreen(onBack = { navController.popBackStack() }) }
            composable(
                Routes.PAYROLL_DETAIL,
                arguments = listOf(navArgument("periodId") { type = NavType.LongType })
            ) { backStackEntry ->
                val periodId = backStackEntry.arguments?.getLong("periodId") ?: return@composable
                PayrollPeriodDetailScreen(
                    periodId,
                    onBack = { navController.popBackStack() },
                    onOpenCalculator = { navController.navigate(Routes.payrollCalculator(periodId)) }
                )
            }
            composable(
                Routes.PAYROLL_CALCULATOR,
                arguments = listOf(navArgument("periodId") { type = NavType.LongType })
            ) { backStackEntry ->
                val periodId = backStackEntry.arguments?.getLong("periodId") ?: return@composable
                PayrollCalculatorScreen(periodId, onBack = { navController.popBackStack() })
            }
        }
    }
}
