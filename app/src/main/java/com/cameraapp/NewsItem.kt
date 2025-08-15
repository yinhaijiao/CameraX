package com.example.cameraapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val imageUrl: String,
    val publishTime: String,
    val source: String
) : Parcelable