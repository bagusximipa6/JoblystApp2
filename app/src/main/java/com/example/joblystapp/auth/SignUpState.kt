package com.example.joblystapp.auth

data class SignUpState (
    val isSignUpSuccessful: Boolean = false,
    val signUpError: String? = null,
    val username: String,
    val email: String,
    val password: String
)
