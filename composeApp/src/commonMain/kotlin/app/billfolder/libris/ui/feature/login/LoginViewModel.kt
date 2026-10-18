package app.billfolder.libris.ui.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.data.auth.AuthSession
import app.billfolder.libris.ui.common.userMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val pending: Boolean = false,
    val error: String? = null,
) {
    private val emailLooksValid get() = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(email.trim())
    val canSubmit get() = emailLooksValid && password.isNotEmpty() && !pending
}

class LoginViewModel(private val session: AuthSession) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmail(value: String) = _state.update { it.copy(email = value, error = null) }

    fun onPassword(value: String) = _state.update { it.copy(password = value, error = null) }

    fun togglePassword() = _state.update { it.copy(showPassword = !it.showPassword) }

    /** Navigation follows [AuthSession.state]; this only reports failures. */
    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(pending = true, error = null) }
        viewModelScope.launch {
            try {
                session.login(current.email, current.password)
                _state.update { it.copy(pending = false, password = "") }
            } catch (e: Exception) {
                _state.update { it.copy(pending = false, error = e.userMessage()) }
            }
        }
    }
}
