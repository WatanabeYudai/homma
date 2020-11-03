package com.example.bookmanager.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class VolumeInfo(
    var title: String?,
    var authors: List<String>?,
    var averageRating: Float?,
    var ratingsCount: Int?,
    var description: String?,
    var imageLinks: ImageLinks?
)
