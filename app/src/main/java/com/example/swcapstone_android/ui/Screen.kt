package com.example.swcapstone_android.ui


sealed class Screen(val route: String) {
    object SplashScreen : Screen("splash_screen")

    object LoginScreen : Screen("login_screen")

    object DetailScreen : Screen("detail_screen")

    object HomeScreen : Screen("home_screen")
}