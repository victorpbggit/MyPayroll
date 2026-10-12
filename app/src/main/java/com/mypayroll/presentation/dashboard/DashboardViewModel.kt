package com.mypayroll.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.domain.model.DashboardSummary
import com.mypayroll.domain.usecase.GetDashboardSummaryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(getDashboardSummaryUseCase: GetDashboardSummaryUseCase) : ViewModel() {

    val summary = getDashboardSummaryUseCase()
        .catch { emit(DashboardSummary()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())
}
