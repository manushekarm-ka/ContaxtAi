package com.example.myapplication.actions

import com.example.myapplication.classifier.DetectedContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionSuggestionEngineTest {

    private val engine = ActionSuggestionEngine()

    @Test
    fun `test URL actions`() {
        val suggestions = engine.getSuggestions(DetectedContentType.URL)
        assertEquals(2, suggestions.size)
        assertTrue(suggestions.any { it.type == ActionType.OPEN_URL })
        assertTrue(suggestions.any { it.type == ActionType.COPY })
    }

    @Test
    fun `test Email actions`() {
        val suggestions = engine.getSuggestions(DetectedContentType.EMAIL)
        assertTrue(suggestions.any { it.type == ActionType.COMPOSE_EMAIL })
        assertTrue(suggestions.any { it.type == ActionType.COPY })
    }

    @Test
    fun `test Math actions`() {
        val suggestions = engine.getSuggestions(DetectedContentType.MATHEMATICS)
        assertTrue(suggestions.any { it.type == ActionType.CALCULATE })
        assertTrue(suggestions.any { it.type == ActionType.SHOW_STEPS })
    }

    @Test
    fun `test Question actions`() {
        val suggestions = engine.getSuggestions(DetectedContentType.QUESTION)
        assertTrue(suggestions.any { it.type == ActionType.PREPARE_ANSWER })
    }

    @Test
    fun `test Empty for Unknown`() {
        val suggestions = engine.getSuggestions(DetectedContentType.UNKNOWN)
        assertTrue(suggestions.isEmpty())
    }
}
