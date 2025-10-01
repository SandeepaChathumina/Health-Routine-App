package com.example.healthapp

import java.io.Serializable

data class Exercise(
    val id: Int,
    val name: String,
    val duration: String,
    val category: String,
    val imageResource: String,
    val color: String
) : Serializable