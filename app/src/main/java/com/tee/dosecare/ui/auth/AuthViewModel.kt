package com.tee.dosecare.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tee.dosecare.utils.PreferencesManager
import com.tee.dosecare.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<Unit>?>(null)
    val loginState: StateFlow<Resource<Unit>?> = _loginState

    private val _registerState = MutableStateFlow<Resource<Unit>?>(null)
    val registerState: StateFlow<Resource<Unit>?> = _registerState

    val isUserLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    val isOnboardingCompleted: StateFlow<Boolean> =
        preferencesManager.isOnboardingCompleted
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                _loginState.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _loginState.value = Resource.Error(
                    e.message ?: "Login failed. Please try again."
                )
            }
        }
    }

    fun register(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                _registerState.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _registerState.value = Resource.Error(
                    e.message ?: "Registration failed. Please try again."
                )
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setOnboardingCompleted()
        }
    }
    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            onResult(true, null)
        } catch (e: Exception) {
            onResult(false, e.message ?: "Failed to send reset email")
        }
    }
}
                firebaseAuth.sendPasswordResetEmail(email).await()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to send reset email")
            }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun resetLoginState() {
        _loginState.value = null
    }

    fun resetRegisterState() {
        _registerState.value = null
    }
}