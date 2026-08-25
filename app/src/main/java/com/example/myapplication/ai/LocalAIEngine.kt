package com.example.myapplication.ai

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

interface LocalAIEngine {
    val state: StateFlow<LocalAIState>
    suspend fun explain(text: String): String
    suspend fun summarize(text: String): String
    suspend fun rewrite(text: String, style: String): String
    suspend fun generateReply(text: String): String
    suspend fun answerQuestion(text: String): String
    suspend fun generateStudyQuestions(text: String): String
    suspend fun simplify(text: String): String
    suspend fun generateFlashcards(text: String): String
    suspend fun generateQuiz(text: String): String
    suspend fun answerFollowUp(originalText: String, followUpQuestion: String): String
    fun release()
}

class DefaultLocalAIEngine(
    private val context: Context,
    private val modelManager: LocalModelManager
) : LocalAIEngine {

    private val _state = MutableStateFlow<LocalAIState>(LocalAIState.Idle)
    override val state: StateFlow<LocalAIState> = _state.asStateFlow()

    private var llmInference: LlmInference? = null

    init {
        initializeModel()
    }

    private fun initializeModel() {
        val TAG = "LocalAIEngine"
        Log.d(TAG, "Starting model setup and initialization...")
        
        _state.value = LocalAIState.Loading

        // Step 1: Ensure model is in internal storage (copy from staging if needed)
        Log.d(TAG, "Step 1: Setting up model file...")
        val setupResult = modelManager.setupModel()
        if (setupResult.isFailure) {
            val errorMsg = "Stage 1 (Setup) Failed: ${setupResult.exceptionOrNull()?.message}"
            Log.e(TAG, errorMsg)
            _state.value = LocalAIState.Error(errorMsg)
            return
        }

        // Step 2: Get the validated internal file
        Log.d(TAG, "Step 2: Validating internal model file...")
        val modelFile = modelManager.getModelFile()
        if (modelFile == null) {
            val errorMsg = "Stage 2 (Validation) Failed: Model file missing after setup."
            Log.e(TAG, errorMsg)
            _state.value = LocalAIState.Error(errorMsg)
            return
        }

        val modelPath = modelFile.absolutePath
        Log.i(TAG, "Step 3: Initializing MediaPipe (CPU Path) with $modelPath (${modelFile.length()} bytes)")

        try {
            Log.d(TAG, "Building LlmInferenceOptions for CPU/XNNPACK...")
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(256)
                .build()
            
            Log.d(TAG, "Calling LlmInference.createFromOptions (This may fail if model is GPU-only)...")
            // The engine only becomes READY if this call succeeds without OpenCL errors
            llmInference = LlmInference.createFromOptions(context, options)
            Log.i(TAG, "LlmInference initialized successfully on CPU. Engine is READY.")
            _state.value = LocalAIState.Ready
        } catch (e: Throwable) {
            val errorMsg = "Stage 3 (MediaPipe CPU Init) Failed: ${e.javaClass.simpleName} - ${e.localizedMessage}"
            Log.e(TAG, errorMsg)
            if (errorMsg.contains("OpenCL", ignoreCase = true)) {
                _state.value = LocalAIState.Error("GPU Incompatibility: Please ensure you pushed the CPU model artifact (gemma-2b-it-cpu-int4.bin).")
            } else {
                _state.value = LocalAIState.Error(errorMsg)
            }
        }
    }

    private suspend fun runInference(prompt: String): String = withContext(Dispatchers.Default) {
        val engine = llmInference ?: return@withContext "Model not initialized"
        val previousState = _state.value
        _state.value = LocalAIState.Generating
        return@withContext try {
            val response = engine.generateResponse(prompt)
            _state.value = previousState
            response
        } catch (e: Exception) {
            _state.value = previousState
            "Error: ${e.localizedMessage}"
        }
    }

    override suspend fun explain(text: String): String {
        return runInference("<start_of_turn>user\nExplain the concept in this text. Provide a simple definition, step-by-step key ideas, important terms, and an example if possible. Return only the answer.\n\nText: \"$text\"<end_of_turn>\n<start_of_turn>model\n")
    }

    override suspend fun summarize(text: String): String {
        return runInference("<start_of_turn>user\nSummarize the main ideas of this text using concise bullet points. Preserve important facts and terms. Return only the summary.\n\nText: \"$text\"<end_of_turn>\n<start_of_turn>model\n")
    }

    override suspend fun rewrite(text: String, style: String): String {
        return runInference("<start_of_turn>user\nRewrite this text in a $style style. Preserve the original meaning. Return only the rewritten text. Do not explain the changes.\n\nText: \"$text\"<end_of_turn>\n<start_of_turn>model\n")
    }

    override suspend fun generateReply(text: String): String {
        return runInference("<start_of_turn>user\nGenerate a natural reply to this message. Return only the reply. Do not explain your reasoning. Do not add quotation marks.\n\nMessage: \"$text\"<end_of_turn>\n<start_of_turn>model\n")
    }

    override suspend fun answerQuestion(text: String): String {
        return runInference("<start_of_turn>user\nAnswer the following question directly and concisely. Do not include any introductory phrases, disclaimers, or information about being an AI. Return only the answer.\n\nQuestion: \"$text\"<end_of_turn>\n<start_of_turn>model\n")
    }

    override suspend fun generateStudyQuestions(text: String): String {
        return runInference("<start_of_turn>user\nGenerate 2-3 study questions based on this text. Ensure they are answerable from the text and numbered. Return only the questions.\n\nText: \"$text\"<end_of_turn>\n<start_of_turn>model\n")
    }

    override suspend fun simplify(text: String): String {
        return runInference("<start_of_turn>user\nSimplify this text by replacing difficult terminology with simpler language while preserving facts and terms. Return only the simplified text.\n\nText: \"$text\"<end_of_turn>\n<start_of_turn>model\n")
    }

    override suspend fun generateFlashcards(text: String): String {
        return runInference("<start_of_turn>user\nGenerate 5 study flashcards from the text. For each card, provide a Question and a concise Answer. Format like this: CARD 1\\nQ: [Question]\\nA: [Answer]\\n\\nReturn only the flashcards based on the text provided.\n\nText: \"$text\"<end_of_turn>\n<start_of_turn>model\n")
    }

    override suspend fun generateQuiz(text: String): String {
        return runInference("<start_of_turn>user\nGenerate a 5-question multiple-choice quiz from the text. Each question must have 4 options (A, B, C, D) and a single correct answer. Format like this: Q1: [Question]\\nA) [Option]\\nB) [Option]\\nC) [Option]\\nD) [Option]\\nCorrect: [A/B/C/D]\\n\\nReturn only the quiz based on the text.\n\nText: \"$text\"<end_of_turn>\n<start_of_turn>model\n")
    }

    override suspend fun answerFollowUp(originalText: String, followUpQuestion: String): String {
        return runInference("<start_of_turn>user\nContext: \"$originalText\"\n\nFollow-up Question: \"$followUpQuestion\"\n\nAnswer the follow-up question directly using the context provided. Return only the answer concisely.\n<end_of_turn>\n<start_of_turn>model\n")
    }

    override fun release() {
        llmInference?.close()
        llmInference = null
        _state.value = LocalAIState.Idle
    }
}