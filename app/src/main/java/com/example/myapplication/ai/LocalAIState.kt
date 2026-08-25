package com.example.myapplication.ai

sealed class LocalAIState(val displayName: String) {
    object Idle : LocalAIState("Idle")
    object Loading : LocalAIState("Loading")
    object Generating : LocalAIState("Generating")
    object Ready : LocalAIState("Ready")
    object Unavailable : LocalAIState("Unavailable")
    data class Error(val message: String) : LocalAIState("Error")
}
