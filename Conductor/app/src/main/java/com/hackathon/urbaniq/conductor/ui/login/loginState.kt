package com.hackathon.urbaniq.conductor.ui.login

sealed class LoginState {
    object Idle : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
