package com.example.joblystapp.model

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.joblystapp.auth.SignInState
import com.example.joblystapp.data.SignInResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignInViewModel : ViewModel() {
    private val _signinstate = MutableStateFlow(SignInState(email = "", password = ""))
    val signinstate = _signinstate.asStateFlow()
    private val auth = FirebaseAuth.getInstance()

    private val _navigateToProfile = MutableStateFlow(false)
    val navigateToProfile = _navigateToProfile.asStateFlow()

    private var navController: NavController? = null

    fun setNavController(navController: NavController) {
        this.navController = navController
    }

    fun onSignInResult(result: SignInResult) {
        _signinstate.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                signInError = result.errorMessage
            )
        }

        if (_signinstate.value.isSignInSuccessful) {
            _navigateToProfile.value = true
        }
    }

    fun resetState() {
        _signinstate.update { SignInState(email = "", password = "") }
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update the state when sign-in is successful
                    resetState()
                    navController?.navigate("profile")
                } else {
                    _signinstate.update { it.copy(signInError = "Login Failed") }
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun onProfileNavigated() {
        _navigateToProfile.value = false
    }
}
