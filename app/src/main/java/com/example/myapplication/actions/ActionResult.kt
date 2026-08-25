package com.example.myapplication.actions

data class ActionResult(
    val title: String,
    val content: String,
    val isError: Boolean = false
)
