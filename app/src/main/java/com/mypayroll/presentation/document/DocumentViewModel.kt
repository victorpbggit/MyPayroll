package com.mypayroll.presentation.document

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.DocumentEntity
import com.mypayroll.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class DocumentUiState(
    val items: List<DocumentEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/** storagePath is a plain string reference (e.g. a content:// URI or file path the user
 * typed). This project has no file picker / Storage Access Framework wiring yet, so actual
 * file bytes are not read or copied here - only metadata is tracked. */
class DocumentViewModel(private val repository: DocumentRepository) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState = combine(repository.observeAll(), _errorMessage) { items, error ->
        DocumentUiState(items, isLoading = false, errorMessage = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DocumentUiState())

    fun add(fileName: String, documentType: String, storagePath: String) {
        viewModelScope.launch {
            runCatching {
                repository.insert(DocumentEntity(fileName = fileName, documentType = documentType, storagePath = storagePath, createdAt = LocalDateTime.now()))
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan metadata dokumen" }
        }
    }

    fun remove(document: DocumentEntity) {
        viewModelScope.launch {
            runCatching { repository.delete(document) }
                .onFailure { e -> _errorMessage.value = e.message ?: "Gagal menghapus dokumen" }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
