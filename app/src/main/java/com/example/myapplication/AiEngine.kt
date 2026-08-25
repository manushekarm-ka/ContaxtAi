package com.example.myapplication

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object AiEngine {
    private var llmInference: LlmInference? = null
    var isInitialized: Boolean = false
        private set

    fun init(context: Context, modelPath: String = "/data/local/tmp/llm_model.bin") {
        if (llmInference != null) return

        val file = File(modelPath)
        if (!file.exists()) return

        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(128)
                .build()
            llmInference = LlmInference.createFromOptions(context, options)
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
            isInitialized = false
        }
    }

    suspend fun generateAction(text: String, actionType: String): String = withContext(Dispatchers.Default) {
        val instance = llmInference ?: return@withContext "Model not ready. Ensure model is at /data/local/tmp/llm_model.bin"

        val prompt = when (actionType) {
            "REPLY" -> "<start_of_turn>user\nSuggest 2 short 5-word quick replies for: \"$text\"\n<end_of_turn>\n<start_of_turn>model\n"
            "SUMMARIZE" -> "<start_of_turn>user\nSummarize this in one sentence: \"$text\"\n<end_of_turn>\n<start_of_turn>model\n"
            else -> "<start_of_turn>user\nExplain simply: \"$text\"\n<end_of_turn>\n<start_of_turn>model\n"
        }

        return@withContext try {
            instance.generateResponse(prompt)
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}