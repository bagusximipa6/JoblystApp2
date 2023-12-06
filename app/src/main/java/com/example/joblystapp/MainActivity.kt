package com.example.joblystapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.joblystapp.auth.GoogleAuthUIClient
import com.example.joblystapp.auth.ProfileScreen
import com.example.joblystapp.auth.SignInScreen
import com.example.joblystapp.auth.SignUpScreen
import com.example.joblystapp.auth.WelcomeScreen
import com.example.joblystapp.auth.WelcomeScreen2
import com.example.joblystapp.model.SignInViewModel
import com.example.joblystapp.model.SignUpViewModel
import com.example.joblystapp.ui.theme.JoblystAppTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val googleAuthUIClient by lazy {
        GoogleAuthUIClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JoblystAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "welcome1") {
                        composable("welcome1") {
                            WelcomeScreen(navController = navController)
                        }
                        composable("welcome2") {
                            WelcomeScreen2(navController = navController)
                        }
                        composable("sign_in") {
                            val viewModel = viewModel<SignInViewModel>()
                            val signinstate by viewModel.signinstate.collectAsStateWithLifecycle()

                            LaunchedEffect(key1 = Unit) {
                                if(googleAuthUIClient.getSignedInUser() != null) {
                                    navController.navigate("profile")
                                }
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if(result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUIClient.SignInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )

                            LaunchedEffect(key1 = signinstate.isSignInSuccessful) {
                                if(signinstate.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("profile")
                                    viewModel.resetState()
                                }
                            }

                            SignInScreen(
                                state = signinstate,
                                onSignInClick = {email, password ->
                                    viewModel.signInWithEmailAndPassword(email, password)
                                },
                                onGoogleSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUIClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                },
                                navController = navController
                            )
                        }
                        composable("sign_up") {
                            val viewModel = viewModel<SignUpViewModel>()
                            val signupstate by viewModel.signupState.collectAsStateWithLifecycle()

                            LaunchedEffect(key1 = signupstate.isSignUpSuccessful) {
                                if(signupstate.isSignUpSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign up successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("sign_in")
                                    viewModel.resetState()
                                }
                            }

                            SignUpScreen(
                                state = signupstate,
                                onSignUpClick = { username, email, password ->
                                    viewModel.signUpWithEmailAndPassword(username, email, password)
                                },
                                navController = navController
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                userData = googleAuthUIClient.getSignedInUser(),
                                onSignOut = {
                                    lifecycleScope.launch {
                                        googleAuthUIClient.signOut()
                                        Toast.makeText(
                                            applicationContext,
                                            "Signed out",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}