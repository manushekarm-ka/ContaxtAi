package com.example.myapplication.actions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.myapplication.ai.LocalAIEngine
import com.example.myapplication.math.SafeMathEvaluator

class ActionHandler(
    private val context: Context,
    private val aiEngine: LocalAIEngine
) {

    private val mathEvaluator = SafeMathEvaluator()
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    suspend fun handleAction(action: SuggestedAction, text: String): ActionResult {
        return when (action.type) {
            ActionType.COPY -> copyToClipboard(text)
            ActionType.OPEN_URL -> openUrl(text)
            ActionType.COMPOSE_EMAIL -> composeEmail(text)
            ActionType.CALL -> openDialer(text)
            ActionType.CALCULATE -> calculate(text)
            ActionType.SHOW_STEPS -> showSteps(text)
            ActionType.PREPARE_ANSWER -> ActionResult("Answer", aiEngine.answerQuestion(text))
            ActionType.EXPLAIN -> ActionResult("Explain", aiEngine.explain(text))
            ActionType.SUMMARIZE -> ActionResult("Summarize", aiEngine.summarize(text))
            ActionType.SIMPLIFY -> ActionResult("Simplified", aiEngine.simplify(text))
            ActionType.REPLY -> ActionResult("Reply", aiEngine.generateReply(text))
            ActionType.REWRITE_CONCISE -> ActionResult("Concise", aiEngine.rewrite(text, "concise"))
            ActionType.REWRITE_FRIENDLY -> ActionResult("Friendly", aiEngine.rewrite(text, "friendly"))
            ActionType.REWRITE_PROFESSIONAL -> ActionResult("Professional", aiEngine.rewrite(text, "professional"))
            ActionType.REWRITE_CASUAL -> ActionResult("Casual", aiEngine.rewrite(text, "casual"))
            ActionType.REWRITE_CLEAR -> ActionResult("Clear", aiEngine.rewrite(text, "clear and simple"))
            ActionType.GENERATE_STUDY_QUESTIONS -> ActionResult("Study Questions", aiEngine.generateStudyQuestions(text))
            ActionType.FLASHCARDS -> ActionResult("Flashcards", aiEngine.generateFlashcards(text))
            ActionType.QUIZ -> ActionResult("Quiz", aiEngine.generateQuiz(text))
            ActionType.FOLLOW_UP -> ActionResult("Follow-up", "Follow-up question processed.") // Logic handled in VM/UI
        }
    }

    private fun copyToClipboard(text: String): ActionResult {
        return try {
            val clip = ClipData.newPlainText("ContextAI", text)
            clipboardManager.setPrimaryClip(clip)
            ActionResult("Success", "Copied to clipboard.")
        } catch (e: Exception) {
            ActionResult("Error", "Failed to copy: ${e.message}", isError = true)
        }
    }

    private fun openUrl(text: String): ActionResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ActionResult("Error", "URL is empty", isError = true)

        val urlToOpen = if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            trimmed
        } else {
            "https://$trimmed"
        }

        return try {
            val uri = Uri.parse(urlToOpen)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                ActionResult("Opening Browser", "Launching link...")
            } else {
                // Secondary check for Android 11+ package visibility limitations
                try {
                    context.startActivity(intent)
                    ActionResult("Opening Browser", "Launching link...")
                } catch (e: Exception) {
                    ActionResult("Error", "No browser found to open this link.", isError = true)
                }
            }
        } catch (e: Exception) {
            ActionResult("Error", "Invalid URL format.", isError = true)
        }
    }

    private fun composeEmail(text: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$text")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult("Compose Email", "Opening email app...")
        } catch (e: Exception) {
            ActionResult("Error", "Failed to open email app.", isError = true)
        }
    }

    private fun openDialer(text: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${text.filter { it.isDigit() || it == '+' }}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult("Call", "Opening dialer...")
        } catch (e: Exception) {
            ActionResult("Error", "Failed to open dialer.", isError = true)
        }
    }

    private fun calculate(text: String): ActionResult {
        return when (val result = mathEvaluator.evaluate(text)) {
            is SafeMathEvaluator.MathResult.Success -> ActionResult("Result", result.value)
            is SafeMathEvaluator.MathResult.Error -> ActionResult("Calculation Error", result.message, isError = true)
        }
    }

    private fun showSteps(text: String): ActionResult {
        // Simple deterministic explanation for basic math
        val trimmed = text.trim()
        return if (trimmed.contains("+")) {
            ActionResult("Steps", "Adding the numbers together locally.")
        } else if (trimmed.contains("-")) {
            ActionResult("Steps", "Subtracting the values locally.")
        } else if (trimmed.contains("*")) {
            ActionResult("Steps", "Multiplying the terms locally.")
        } else if (trimmed.contains("/")) {
            ActionResult("Steps", "Dividing the numerator by the denominator.")
        } else {
            ActionResult("Steps", "Determining the value of the expression.")
        }
    }
}
