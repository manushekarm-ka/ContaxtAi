package com.example.myapplication.ai

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Manages the MediaPipe-compatible converted LLM model artifact (.bin).
 * For MediaTek/Mali devices (like OPPO A3x), use the CPU-optimized artifact.
 * The model must be present at context.filesDir/llm_model.bin for runtime use.
 */
class LocalModelManager(private val context: Context) {
    
    companion object {
        private const val TAG = "LocalModelManager"
        const val MODEL_FILENAME = "llm_model.bin"
        private const val STAGING_PATH = "/data/local/tmp/$MODEL_FILENAME"
        // Falcon 1B IT CPU Int4 is ~650MB. Setting 500MB minimum to support 
        // 1B models while still excluding invalid small files.
        private const val MIN_MODEL_SIZE_BYTES = 500L * 1024L * 1024L 
    }

    /**
     * Ensures the model is in the internal files directory.
     * If missing from internal storage but present in the /data/local/tmp staging area,
     * it copies it to internal storage.
     */
    fun setupModel(): Result<Unit> {
        val targetFile = File(context.filesDir, MODEL_FILENAME)
        val stagingFile = File(STAGING_PATH)

        // DEVELOPMENT RULE: If a valid staging file is present, always ensure it is the one in internal storage.
        // This solves the "stale GPU model" issue when switching to the CPU model via ADB push.
        if (isValid(stagingFile)) {
            val needsCopy = !targetFile.exists() || targetFile.length() != stagingFile.length()
            
            if (needsCopy) {
                Log.i(TAG, "New model detected in staging ($STAGING_PATH). Copying to internal storage...")
                return try {
                    copyFile(stagingFile, targetFile)
                    Log.i(TAG, "Model update successful.")
                    Result.success(Unit)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update model from staging", e)
                    Result.failure(e)
                }
            } else {
                Log.d(TAG, "Internal model matches staging file size. Skipping copy.")
                return Result.success(Unit)
            }
        }

        // If no staging file, just check if the internal one is already valid
        if (isValid(targetFile)) {
            Log.d(TAG, "Valid model already present in internal storage.")
            return Result.success(Unit)
        }

        return Result.failure(Exception("Model not found in internal storage or staging. " +
                "Expected at ${targetFile.absolutePath} or $STAGING_PATH. " +
                "Internal exists: ${targetFile.exists()}, Staging exists: ${File(STAGING_PATH).exists()}"))
    }

    /**
     * Returns the validated model file from internal storage.
     */
    fun getModelFile(): File? {
        val internalFile = File(context.filesDir, MODEL_FILENAME)
        return if (isValid(internalFile)) internalFile else null
    }

    private fun isValid(file: File): Boolean {
        return file.exists() && 
               file.isFile && 
               file.length() >= MIN_MODEL_SIZE_BYTES && 
               file.canRead()
    }

    private fun copyFile(source: File, target: File) {
        FileInputStream(source).use { input ->
            FileOutputStream(target).use { output ->
                input.copyTo(output)
            }
        }
    }

    fun getRuntimePath(): String {
        return File(context.filesDir, MODEL_FILENAME).absolutePath
    }
}
