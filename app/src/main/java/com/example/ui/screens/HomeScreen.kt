package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.R
import com.example.data.AuthRepository
import com.example.data.QuizRepository
import com.example.models.Category
import com.example.ui.navigation.Categories
import com.example.ui.navigation.Subcategories
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val user by AuthRepository.currentUser.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredCategories = QuizRepository.categories.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, "Home") },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Blue Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RoyalBlue)
                            .padding(top = 48.dp, bottom = 56.dp, start = 24.dp, end = 24.dp)
                    ) {
                        Text(
                            text = "Hello, ${user?.fullName ?: "User"}!",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enhance your aptitude & technical skills",
                            color = Color(0xFFD0D4F5),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Overlapping Search Bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 28.dp)
                            .padding(horizontal = 24.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search Java, Aptitude, Database...", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(28.dp)),
                            shape = RoundedCornerShape(28.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            singleLine = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Popular Quiz Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { navController.navigate(Categories) }) {
                        Text("View All", color = CorrectGreen, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    for (row in 0 until (filteredCategories.size + 1) / 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (col in 0..1) {
                                val index = row * 2 + col
                                if (index < filteredCategories.size) {
                                    HomeCategoryCard(
                                        modifier = Modifier.weight(1f),
                                        category = filteredCategories[index],
                                        onClick = { navController.navigate(Subcategories(filteredCategories[index].id, filteredCategories[index].title)) }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun HomeCategoryCard(modifier: Modifier = Modifier, category: Category, onClick: () -> Unit) {
    val topicCount = category.subcategories.size

    val iconRes = when (category.id) {
        "aptitude" -> R.drawable.ic_aptitude
        "technical" -> R.drawable.ic_technical
        "programming" -> R.drawable.ic_programming
        "gk" -> R.drawable.ic_gk
        else -> R.drawable.ic_aptitude
    }

    val description = when (category.id) {
        "aptitude" -> "Quantitative, Reasoning and Verbal..."
        "technical" -> "Core computer science concepts..."
        "programming" -> "Languages and frameworks..."
        "gk" -> "Current affairs and general..."
        else -> "Quiz questions..."
    }

    Card(
        modifier = modifier
            .height(190.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFF5F7FA), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = category.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = category.title.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
            Text(
                text = "$topicCount Topics",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = RoyalBlue
            )
        }
    }
}
