package com.example.myapplication.classifier

import org.junit.Assert.assertEquals
import org.junit.Test

class ContentClassifierTest {

    private val classifier = RuleBasedContentClassifier()

    @Test
    fun `test URL detection`() {
        assertEquals(DetectedContentType.URL, classifier.classify("https://example.com"))
        assertEquals(DetectedContentType.URL, classifier.classify("http://www.google.com/search?q=test"))
    }

    @Test
    fun `test Email detection`() {
        assertEquals(DetectedContentType.EMAIL, classifier.classify("student@example.com"))
        assertEquals(DetectedContentType.EMAIL, classifier.classify("my.name+tag@company.co.uk"))
    }

    @Test
    fun `test Phone number detection`() {
        assertEquals(DetectedContentType.PHONE_NUMBER, classifier.classify("1234567890"))
        assertEquals(DetectedContentType.PHONE_NUMBER, classifier.classify("+1 (555) 123-4567"))
        assertEquals(DetectedContentType.PHONE_NUMBER, classifier.classify("080-1234-5678"))
    }

    @Test
    fun `test Mathematics detection`() {
        assertEquals(DetectedContentType.MATHEMATICS, classifier.classify("2 + 3"))
        assertEquals(DetectedContentType.MATHEMATICS, classifier.classify("25 * 40"))
        assertEquals(DetectedContentType.MATHEMATICS, classifier.classify("100 / 5"))
        assertEquals(DetectedContentType.MATHEMATICS, classifier.classify("(20 + 10) * 2"))
    }

    @Test
    fun `test Question detection`() {
        assertEquals(DetectedContentType.QUESTION, classifier.classify("What is photosynthesis?"))
        assertEquals(DetectedContentType.QUESTION, classifier.classify("How does gravity work?"))
        assertEquals(DetectedContentType.QUESTION, classifier.classify("Why is the sky blue?"))
    }

    @Test
    fun `test Message detection`() {
        assertEquals(DetectedContentType.MESSAGE, classifier.classify("I'll call you later."))
        assertEquals(DetectedContentType.MESSAGE, classifier.classify("I'm on my way"))
        assertEquals(DetectedContentType.MESSAGE, classifier.classify("See you soon"))
    }

    @Test
    fun `test Study material detection`() {
        assertEquals(DetectedContentType.STUDY_MATERIAL, classifier.classify("Photosynthesis is defined as the process used by plants to convert light energy into chemical energy."))
        assertEquals(DetectedContentType.STUDY_MATERIAL, classifier.classify("The mitochondria is the powerhouse of the cell."))
    }

    @Test
    fun `test Code detection`() {
        assertEquals(DetectedContentType.CODE, classifier.classify("fun main() { println(\"Hello\") }"))
        assertEquals(DetectedContentType.CODE, classifier.classify("import androidx.compose.runtime.Composable"))
        assertEquals(DetectedContentType.CODE, classifier.classify("class MyClass { private val x = 0; }"))
    }

    @Test
    fun `test Long text detection`() {
        val longText = "This is a very long text that should be classified as LONG_TEXT because it has many words and is substantially longer than a normal message. ".repeat(10)
        assertEquals(DetectedContentType.LONG_TEXT, classifier.classify(longText))
    }

    @Test
    fun `test Normal text detection`() {
        assertEquals(DetectedContentType.NORMAL_TEXT, classifier.classify("Just some random sentence that doesn't fit other categories."))
    }

    @Test
    fun `test Priority URL over Normal Text`() {
        assertEquals(DetectedContentType.URL, classifier.classify("https://example.com"))
    }

    @Test
    fun `test Priority Mathematics over Normal Text`() {
        assertEquals(DetectedContentType.MATHEMATICS, classifier.classify("2 + 3"))
    }
}
