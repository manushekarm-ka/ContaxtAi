package com.example.myapplication.classifier

/**
 * Lightweight local classifier. Checks types from most specific to most generic
 * so URLs, emails, and math are not swallowed by MESSAGE or NORMAL_TEXT.
 */
class RuleBasedContentClassifier : ContentClassifier {

    override fun classify(text: String): DetectedContentType {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return DetectedContentType.UNKNOWN

        return when {
            isUrl(trimmed) -> DetectedContentType.URL
            isEmail(trimmed) -> DetectedContentType.EMAIL
            isPhoneNumber(trimmed) -> DetectedContentType.PHONE_NUMBER
            isMathematics(trimmed) -> DetectedContentType.MATHEMATICS
            isCode(trimmed) -> DetectedContentType.CODE
            isQuestion(trimmed) -> DetectedContentType.QUESTION
            isStudyMaterial(trimmed) -> DetectedContentType.STUDY_MATERIAL
            isMessage(trimmed) -> DetectedContentType.MESSAGE
            isLongText(trimmed) -> DetectedContentType.LONG_TEXT
            else -> DetectedContentType.NORMAL_TEXT
        }
    }

    private fun isUrl(text: String): Boolean {
        if (!URL_REGEX.containsMatchIn(text)) return false
        val remainder = text.replace(URL_REGEX, "").trim()
        return remainder.isEmpty() || text.length <= URL_DOMINANT_MAX_LENGTH
    }

    private fun isEmail(text: String): Boolean {
        return EMAIL_REGEX.matches(text) ||
            (text.length <= 80 && EMAIL_REGEX.containsMatchIn(text) && text.split(WHITESPACE).size <= 3)
    }

    private fun isPhoneNumber(text: String): Boolean {
        if (text.any { it.isLetter() }) return false
        if (DATE_REGEX.matches(text)) return false
        val digits = text.filter { it.isDigit() }
        if (digits.length !in 10..15) return false
        if (text.any { it in "*/=" }) return false
        return text.all { it.isDigit() || it in PHONE_ALLOWED } && PHONE_REGEX.matches(text)
    }

    private fun isMathematics(text: String): Boolean {
        if (DATE_REGEX.matches(text)) return false
        if (text.any { it.isLetter() }) return false
        if (!MATH_OPERATOR_REGEX.containsMatchIn(text)) return false
        return MATH_EXPRESSION_REGEX.matches(text)
    }

    private fun isCode(text: String): Boolean {
        val keywordHits = CODE_KEYWORD_REGEX.findAll(text).map { it.value.lowercase() }.toSet().size
        val hasBraces = text.contains('{') && text.contains('}')
        val hasSemicolons = text.count { it == ';' } >= 2
        val hasClassicShape = CLASSIC_CODE_REGEX.containsMatchIn(text)
        if (hasClassicShape) return true
        if (keywordHits >= 2) return true
        if (keywordHits >= 1 && (hasBraces || hasSemicolons)) return true
        return hasBraces && hasSemicolons
    }

    private fun isQuestion(text: String): Boolean {
        if (text.endsWith('?')) return true
        return QUESTION_START_REGEX.containsMatchIn(text) && text.length <= QUESTION_MAX_LENGTH
    }

    private fun isStudyMaterial(text: String): Boolean {
        if (text.length < 30) return false
        val phraseHit = STUDY_PHRASES.any { text.contains(it, ignoreCase = true) }
        val keywordHits = STUDY_KEYWORD_REGEX.findAll(text).map { it.value.lowercase() }.toSet().size
        if (phraseHit) return true
        if (keywordHits >= 2) return true
        return keywordHits >= 1 && text.length >= 40
    }

    private fun isMessage(text: String): Boolean {
        if (text.length > MESSAGE_MAX_LENGTH) return false
        return MESSAGE_CUE_REGEX.containsMatchIn(text)
    }

    private fun isLongText(text: String): Boolean {
        return text.length >= LONG_TEXT_MIN_LENGTH || text.split(WHITESPACE).size >= LONG_TEXT_MIN_WORDS
    }

    companion object {
        private const val URL_DOMINANT_MAX_LENGTH = 120
        private const val QUESTION_MAX_LENGTH = 180
        private const val MESSAGE_MAX_LENGTH = 160
        private const val LONG_TEXT_MIN_LENGTH = 220
        private const val LONG_TEXT_MIN_WORDS = 40
        private const val PHONE_ALLOWED = " +()-."

        private val WHITESPACE = Regex("\\s+")

        private val URL_REGEX = Regex(
            """https?://[^\s/$.?#].[^\s]*""",
            RegexOption.IGNORE_CASE
        )

        private val EMAIL_REGEX = Regex(
            """[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}""",
            RegexOption.IGNORE_CASE
        )

        private val PHONE_REGEX = Regex(
            """^(\+\d{1,3}[\s.-]?)?(\(\d{2,4}\)[\s.-]?)?[\d\s.-]{7,18}$"""
        )

        private val DATE_REGEX = Regex("""^\d{4}-\d{2}-\d{2}$""")

        private val MATH_OPERATOR_REGEX = Regex("""[+\*/×÷=^]|(\d\s+-\s+\d)|(\d-\d)""")

        private val MATH_EXPRESSION_REGEX = Regex(
            """^\(?\s*\d+(\.\d+)?\s*\)?(\s*[+\-*/×÷^]\s*\(?\s*\d+(\.\d+)?\s*\)?)+\s*$"""
        )

        private val CODE_KEYWORD_REGEX = Regex(
            """\b(fun|function|def|class|import|package|public|private|protected|return|const|val|var|let|void|int|string|boolean|struct|enum|async|await|extends|implements)\b""",
            RegexOption.IGNORE_CASE
        )

        private val CLASSIC_CODE_REGEX = Regex(
            """(\b(import|package)\s+[\w.]+)|(\b(fun|def|function|class)\s+\w+\s*[\({])|(#include\s*<)""",
            RegexOption.IGNORE_CASE
        )

        private val QUESTION_START_REGEX = Regex(
            """^(what|how|why|when|where|who|which|whose|is|are|can|do|does|did|should|could|would)\b""",
            RegexOption.IGNORE_CASE
        )

        private val STUDY_PHRASES = listOf(
            "is defined as",
            "is the process",
            "refers to",
            "in other words",
            "textbook"
        )

        private val STUDY_KEYWORD_REGEX = Regex(
            """\b(photosynthesis|chlorophyll|mitochondria|gravity|acceleration|molecule|atom|electron|compound|theorem|hypothesis|organism|chromosome|evaporation|condensation|kinetic|newton|einstein|biology|chemistry|physics|equation)\b""",
            RegexOption.IGNORE_CASE
        )

        private val MESSAGE_CUE_REGEX = Regex(
            """\b(i'll|i am|i'm|we'll|let's|are you|can we|call you|meet|tomorrow|later|coming|hey|yeah|okay|thanks|please|wanna|gonna|see you|text me|on my way)\b""",
            RegexOption.IGNORE_CASE
        )
    }
}
