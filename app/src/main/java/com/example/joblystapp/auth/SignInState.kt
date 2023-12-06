package com.example.joblystapp.auth

data class SignInState (
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val email: String,
    val password: String
)
