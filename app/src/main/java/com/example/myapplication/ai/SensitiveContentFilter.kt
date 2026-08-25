package com.example.myapplication.ai

object SensitiveContentFilter {
    
    private val SENSITIVE_PATTERNS = listOf(
        // Passwords / Private Keys
        Regex("""(?i)\b(password|passwd|secret|private_key|apikey)\b\s*[:=]\s*\S+"""),
        // Credit Cards (Basic Luhn-like check not implemented here, just 16 digits)
        Regex("""\b\d{4}[\s-]?\d{4}[\s-]?\d{4}[\s-]?\d{4}\b"""),
        // OTP / Auth Codes
        Regex("""(?i)\b(otp|verification code|auth code|security code)\b\s*(is\s*)?(\b\d{4,8}\b)"""),
        // Typical Private Keys
        Regex("""-----BEGIN [A-Z ]+ PRIVATE KEY-----""")
    )

    fun containsSensitiveInfo(text: String): Boolean {
        return SENSITIVE_PATTERNS.any { it.containsMatchIn(text) }
    }
}
