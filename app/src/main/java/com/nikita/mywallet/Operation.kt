package com.nikita.mywallet

data class Operation(
    val id: Int,
    val amount: Double,
    val type: String,
    val description: String,
    val date: String
)
