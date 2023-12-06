package com.example.joblystapp.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.joblystapp.auth.SignUpState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpViewModel : ViewModel() {
    private val _signupState = MutableStateFlow(SignUpState(username= "",email = "", password = ""))
    val signupState = _signupState.asStateFlow()

    private var navController: NavController? = null

    fun setNavController(navController: NavController) {
        this.navController = navController
    }

    fun resetState() {
        _signupState.update { SignUpState(username = "", email = "", password = "") }
    }

    fun signUpWithEmailAndPassword(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                // Create user in Firebase Authentication
                val authResult = FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .await()

                // Save user data to Firestore
                val user = hashMapOf(
                    "email" to email,
                    "password" to password
                )

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(authResult.user?.uid ?: "")
                    .set(user)
                    .await()

                // Successful signup
                resetState()
                navController?.navigate("sign_in")
            } catch (e: Exception) {
                // Handle signup failure
                _signupState.update { it.copy(signUpError = "Register Failed") }
            }
        }
    }
}
