package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String = "",
    val title: String = "",
    val subcategories: List<Subcategory> = emptyList()
)

@Serializable
data class Subcategory(
    val id: String = "",
    val title: String = ""
)

@Serializable
data class Question(
    val id: String = "",
    val categoryId: String = "",
    val text: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val explanation: String = "",
    val difficulty: String = "",
    val eventDate: String? = null,
    val questionAddedDate: String? = null,
    val sourceName: String? = null
)
