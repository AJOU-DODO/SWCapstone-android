package com.example.swcapstone_android.ui


sealed class Screen(val route: String) {
    object SplashScreen : Screen("splash_screen")

    object LoginScreen : Screen("login_screen")

    object NextScreen : Screen("next_screen")
}