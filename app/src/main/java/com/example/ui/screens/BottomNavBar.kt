package com.example.ui.screens

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.navigation.Categories
import com.example.ui.navigation.Home
import com.example.ui.navigation.Profile

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String) {
    NavigationBar(
        modifier = Modifier.height(96.dp),
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        NavigationBarItem(
            selected = currentRoute == "Home",
            onClick = { navController.navigate(Home) { popUpTo(Home) { inclusive = true } } },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            alwaysShowLabel = true
        )
        NavigationBarItem(
            selected = currentRoute == "Categories",
            onClick = { navController.navigate(Categories) { popUpTo(Home) } },
            icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
            label = { Text("Categories") },
            alwaysShowLabel = true
        )
        NavigationBarItem(
            selected = currentRoute == "Profile",
            onClick = { navController.navigate(Profile) { popUpTo(Home) } },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            alwaysShowLabel = true
        )
    }
}
