package com.example.myapplication.actions

data class SuggestedAction(
    val id: String,
    val title: String,
    val description: String? = null,
    val type: ActionType,
    val icon: String // Emoji for now as requested
)
