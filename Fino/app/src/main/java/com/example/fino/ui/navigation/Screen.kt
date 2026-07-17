package com.example.fino.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "ホーム")
    object Goals : Screen("goals", "目標")
    object Scan : Screen("scan", "スキャン")
    object History : Screen("history", "履歴")
}
