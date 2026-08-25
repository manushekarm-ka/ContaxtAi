package com.example.myapplication.classifier

fun interface ContentClassifier {
    fun classify(text: String): DetectedContentType
}
