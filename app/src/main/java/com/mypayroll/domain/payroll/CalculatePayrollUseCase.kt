package com.mypayroll.domain.payroll

/** Use Case menghitung payroll tanpa ketergantungan pada DAO/Entity. */
class CalculatePayrollUseCase(
    private val payrollCalculator: PayrollCalculator = PayrollCalculator()
) {
    operator fun invoke(input: PayrollCalculationInput): PayrollCalculationResult {
        return payrollCalculator.calculate(input)
    }
}
