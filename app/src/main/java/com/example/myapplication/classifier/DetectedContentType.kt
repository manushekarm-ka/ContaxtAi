package com.example.myapplication.classifier

enum class DetectedContentType {
    URL,
    EMAIL,
    PHONE_NUMBER,
    MATHEMATICS,
    CODE,
    QUESTION,
    STUDY_MATERIAL,
    MESSAGE,
    LONG_TEXT,
    NORMAL_TEXT,
    UNKNOWN;

    val emoji: String
        get() = when (this) {
            URL -> "🔗"
            EMAIL -> "✉️"
            PHONE_NUMBER -> "📞"
            MATHEMATICS -> "🧮"
            CODE -> "💻"
            QUESTION -> "❓"
            STUDY_MATERIAL -> "📚"
            MESSAGE -> "💬"
            LONG_TEXT -> "📄"
            NORMAL_TEXT -> "📝"
            UNKNOWN -> "❔"
        }

    val displayName: String
        get() = when (this) {
            URL -> "URL"
            EMAIL -> "Email"
            PHONE_NUMBER -> "Phone Number"
            MATHEMATICS -> "Mathematics"
            CODE -> "Code"
            QUESTION -> "Question"
            STUDY_MATERIAL -> "Study Material"
            MESSAGE -> "Message"
            LONG_TEXT -> "Long Text"
            NORMAL_TEXT -> "Normal Text"
            UNKNOWN -> "Unknown"
        }
}
