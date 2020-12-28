package com.example.bookmanager.models

data class BookSearchResultItem(
    val id: String,
    val title: String,
    val authors: List<String>,
    val averageRating: Float?,
    val ratingsCount: Int,
    val description: String,
    val image: String
)