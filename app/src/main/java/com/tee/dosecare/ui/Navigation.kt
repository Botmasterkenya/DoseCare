package com.tee.dosecare.ui

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tee.dosecare.ui.auth.AuthViewModel
import com.tee.dosecare.ui.auth.LoginScreen
import com.tee.dosecare.ui.auth.RegisterScreen
import com.tee.dosecare.ui.home.AddMedicationScreen
import com.tee.dosecare.ui.home.HomeScreen
import com.tee.dosecare.ui.onboarding.OnboardingScreen
import com.tee.dosecare.ui.onboarding.SplashScreen
import com.tee.dosecare.utils.Resource

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object AddMedication : Screen("add_medication")
}

@Composable
fun DoseCareNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val isOnboardingCompleted by authViewModel.isOnboardingCompleted.collectAsState()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateNext = {
                    val destination = when {
                        authViewModel.isUserLoggedIn -> Screen.Home.route
                        isOnboardingCompleted -> Screen.Login.route
                        else -> Screen.Onboarding.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    authViewModel.completeOnboarding()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            val loginState by authViewModel.loginState.collectAsState()
            val isLoading = loginState is Resource.Loading
            val errorMessage = (loginState as? Resource.Error)?.message

            LaunchedEffect(loginState) {
                if (loginState is Resource.Success) {
                    authViewModel.resetLoginState()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                onLoginClick = { email, password -> authViewModel.login(email, password) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }

        composable(Screen.Register.route) {
            val registerState by authViewModel.registerState.collectAsState()
            val isLoading = registerState is Resource.Loading
            val errorMessage = (registerState as? Resource.Error)?.message

            LaunchedEffect(registerState) {
                if (registerState is Resource.Success) {
                    authViewModel.resetRegisterState()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            }

            RegisterScreen(
                onRegisterClick = { name, email, password -> authViewModel.register(name, email, password) },
                onNavigateToLogin = { navController.popBackStack() },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onAddMedication = { navController.navigate(Screen.AddMedication.route) },
                onViewHistory = { },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AddMedication.route) {
            AddMedicationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}