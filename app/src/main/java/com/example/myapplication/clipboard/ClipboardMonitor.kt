package com.example.myapplication.clipboard

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build

/**
 * Reads text from the system clipboard only while monitoring is on and the app
 * has window focus. This follows Android 10+ rules: background clipboard access
 * is not allowed, and this class does not try to bypass that.
 */
class ClipboardMonitor(
    private val context: Context,
    private val clipboardManager: ClipboardManager =
        context.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
) {
    fun interface Callback {
        fun onClipboardResult(result: ClipboardReadResult)
    }

    private var callback: Callback? = null
    private var clipChangedListener: ClipboardManager.OnPrimaryClipChangedListener? = null
    private var monitoringEnabled: Boolean = false
    private var hasWindowFocus: Boolean = false

    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        monitoringEnabled = enabled
        if (enabled) {
            registerListener()
            tryRead()
        } else {
            unregisterListener()
        }
    }

    fun setHasWindowFocus(hasFocus: Boolean) {
        hasWindowFocus = hasFocus
        if (hasFocus && monitoringEnabled) {
            tryRead()
        }
    }

    fun release() {
        unregisterListener()
        callback = null
        monitoringEnabled = false
        hasWindowFocus = false
    }

    private fun registerListener() {
        if (clipChangedListener != null) return
        val listener = ClipboardManager.OnPrimaryClipChangedListener {
            tryRead()
        }
        clipChangedListener = listener
        clipboardManager.addPrimaryClipChangedListener(listener)
    }

    private fun unregisterListener() {
        clipChangedListener?.let { listener ->
            clipboardManager.removePrimaryClipChangedListener(listener)
        }
        clipChangedListener = null
    }

    private fun tryRead() {
        if (!monitoringEnabled || !hasWindowFocus) return
        callback?.onClipboardResult(readClipboard())
    }

    private fun readClipboard(): ClipboardReadResult {
        return try {
            if (!clipboardManager.hasPrimaryClip()) {
                return ClipboardReadResult.Empty
            }

            val clip = clipboardManager.primaryClip
                ?: return ClipboardReadResult.Unavailable

            if (clip.itemCount <= 0) {
                return ClipboardReadResult.Empty
            }

            val description = clip.description
            if (isSensitive(description)) {
                return ClipboardReadResult.Sensitive
            }

            val rawText = clip.getItemAt(0)
                .coerceToText(context)
                .toString()

            if (rawText.isBlank()) {
                return ClipboardReadResult.Empty
            }

            val truncated = rawText.length > PREVIEW_CHAR_LIMIT
            val preview = if (truncated) {
                rawText.take(PREVIEW_CHAR_LIMIT) + ELLIPSIS
            } else {
                rawText
            }
            ClipboardReadResult.Text(preview = preview, truncated = truncated)
        } catch (_: SecurityException) {
            ClipboardReadResult.Unavailable
        } catch (_: Exception) {
            ClipboardReadResult.Unavailable
        }
    }

    private fun isSensitive(description: ClipDescription?): Boolean {
        if (description == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return false
        }
        return description.extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE, false) == true
    }

    companion object {
        const val PREVIEW_CHAR_LIMIT = 500
        private const val ELLIPSIS = "…"
    }
}

sealed interface ClipboardReadResult {
    data class Text(val preview: String, val truncated: Boolean) : ClipboardReadResult
    data object Empty : ClipboardReadResult
    data object Unavailable : ClipboardReadResult
    data object Sensitive : ClipboardReadResult
}
