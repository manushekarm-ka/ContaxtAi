package com.example.myapplication.actions

import com.example.myapplication.classifier.DetectedContentType

class ActionSuggestionEngine {
    fun getSuggestions(type: DetectedContentType, text: String = ""): List<SuggestedAction> {
        return when (type) {
            DetectedContentType.URL -> listOf(
                SuggestedAction("open", "Open", "Open this link in a browser.", ActionType.OPEN_URL, "🌐"),
                SuggestedAction("copy", "Copy", "Copy this URL again.", ActionType.COPY, "📋")
            )
            DetectedContentType.EMAIL -> listOf(
                SuggestedAction("email", "Compose Email", "Draft a new email.", ActionType.COMPOSE_EMAIL, "✉️"),
                SuggestedAction("copy", "Copy", "Copy this address.", ActionType.COPY, "📋")
            )
            DetectedContentType.PHONE_NUMBER -> listOf(
                SuggestedAction("call", "Call", "Open dialer for this number.", ActionType.CALL, "📞"),
                SuggestedAction("copy", "Copy", "Copy this number.", ActionType.COPY, "📋")
            )
            DetectedContentType.MATHEMATICS -> listOf(
                SuggestedAction("calculate", "Calculate", "Evaluate expression.", ActionType.CALCULATE, "🧮"),
                SuggestedAction("show_steps", "Show Steps", "How was this solved?", ActionType.SHOW_STEPS, "📝")
            )
            DetectedContentType.QUESTION -> {
                if (isAcademicQuestion(text)) {
                    listOf(
                        SuggestedAction("answer", "Prepare Answer", "Get a direct answer.", ActionType.PREPARE_ANSWER, "💡"),
                        SuggestedAction("explain", "Explain", "Detailed structured explanation.", ActionType.EXPLAIN, "🎓"),
                        SuggestedAction("questions", "Study Questions", "Generate 2-3 quiz questions.", ActionType.GENERATE_STUDY_QUESTIONS, "❓"),
                        SuggestedAction("flashcards", "Flashcards", "Generate study cards.", ActionType.FLASHCARDS, "🎴"),
                        SuggestedAction("quiz", "Quiz Mode", "Test your knowledge.", ActionType.QUIZ, "🎯"),
                        SuggestedAction("simplify", "Simplify", "Use simpler language.", ActionType.SIMPLIFY, "✨"),
                        SuggestedAction("summarize", "Summarize", "Concise bullet points.", ActionType.SUMMARIZE, "📄"),
                        SuggestedAction("copy", "Copy", "Copy this question.", ActionType.COPY, "📋")
                    )
                } else {
                    listOf(
                        SuggestedAction("answer", "Prepare Answer", "AI answer generation.", ActionType.PREPARE_ANSWER, "💡"),
                        SuggestedAction("copy", "Copy", "Copy this question.", ActionType.COPY, "📋")
                    )
                }
            }
            DetectedContentType.STUDY_MATERIAL -> listOf(
                SuggestedAction("explain", "Explain", "Detailed structured explanation.", ActionType.EXPLAIN, "🎓"),
                SuggestedAction("summarize", "Summarize", "Concise bullet points.", ActionType.SUMMARIZE, "📄"),
                SuggestedAction("simplify", "Simplify", "Use simpler language.", ActionType.SIMPLIFY, "✨"),
                SuggestedAction("questions", "Study Questions", "Generate 2-3 quiz questions.", ActionType.GENERATE_STUDY_QUESTIONS, "❓"),
                SuggestedAction("flashcards", "Flashcards", "Generate study cards.", ActionType.FLASHCARDS, "🎴"),
                SuggestedAction("quiz", "Quiz Mode", "Test your knowledge.", ActionType.QUIZ, "🎯")
            )
            DetectedContentType.MESSAGE -> listOf(
                SuggestedAction("reply", "Reply", "Draft a local reply.", ActionType.REPLY, "💬"),
                SuggestedAction("rewrite_friendly", "Friendly", "Rewrite in a friendly tone.", ActionType.REWRITE_FRIENDLY, "😇"),
                SuggestedAction("rewrite_professional", "Professional", "Rewrite professionally.", ActionType.REWRITE_PROFESSIONAL, "💼"),
                SuggestedAction("rewrite_casual", "Casual", "Make it casual.", ActionType.REWRITE_CASUAL, "😎")
            )
            DetectedContentType.CODE -> listOf(
                SuggestedAction("explain", "Explain", "Explain code logic.", ActionType.EXPLAIN, "💻"),
                SuggestedAction("copy", "Copy", "Copy this code.", ActionType.COPY, "📋")
            )
            DetectedContentType.LONG_TEXT,
            DetectedContentType.NORMAL_TEXT -> listOf(
                SuggestedAction("summarize", "Summarize", "Summarize locally.", ActionType.SUMMARIZE, "📄"),
                SuggestedAction("rewrite_concise", "Concise", "Make it concise.", ActionType.REWRITE_CONCISE, "✂️"),
                SuggestedAction("rewrite_clear", "Clear", "Make it clearer.", ActionType.REWRITE_CLEAR, "✨"),
                SuggestedAction("copy", "Copy", "Copy this text.", ActionType.COPY, "📋")
            )
            DetectedContentType.UNKNOWN -> emptyList()
        }
    }

    private fun isAcademicQuestion(text: String): Boolean {
        val academicKeywords = listOf(
            "photosynthesis", "mitosis", "osmosis", "gravity", "acceleration", 
            "molecule", "atom", "electron", "compound", "theorem", "hypothesis", 
            "organism", "chromosome", "evaporation", "condensation", "kinetic", 
            "potential", "energy", "newton", "einstein", "biology", "chemistry", 
            "physics", "dna", "rna", "ribosome", "mitochondria", "cell", "protein",
            "evolution", "thermodynamics", "quantum", "calculus", "algebra"
        )
        val academicVerbs = listOf("explain", "define", "describe", "what is", "why does", "how does", "what are")
        
        val lowercaseText = text.lowercase()
        val hasKeyword = academicKeywords.any { lowercaseText.contains(it) }
        val hasVerb = academicVerbs.any { lowercaseText.contains(it) }
        
        return hasKeyword || (hasVerb && lowercaseText.split(" ").size > 2)
    }
}
