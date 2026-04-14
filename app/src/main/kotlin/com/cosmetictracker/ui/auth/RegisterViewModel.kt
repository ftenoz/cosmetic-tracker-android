package com.cosmetictracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmetictracker.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(email: String, password: String, firstName: String, lastName: String?) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                val result = authRepository.register(email, password, firstName, lastName)
                if (result.isSuccess) {
                    _uiState.value = RegisterUiState.Success
                } else {
                    val error = result.exceptionOrNull()
                    _uiState.value = RegisterUiState.Error(error?.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error("Network error: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
