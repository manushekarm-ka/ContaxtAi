package com.example.myapplication.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeMathEvaluatorTest {

    private val evaluator = SafeMathEvaluator()

    @Test
    fun `test simple addition`() {
        val result = evaluator.evaluate("25 + 40")
        assertTrue(result is SafeMathEvaluator.MathResult.Success)
        assertEquals("65", (result as SafeMathEvaluator.MathResult.Success).value)
    }

    @Test
    fun `test complex expression`() {
        val result = evaluator.evaluate("25 + 40 * 2")
        assertTrue(result is SafeMathEvaluator.MathResult.Success)
        assertEquals("105", (result as SafeMathEvaluator.MathResult.Success).value)
    }

    @Test
    fun `test parentheses`() {
        val result = evaluator.evaluate("(20 + 10) * 2")
        assertTrue(result is SafeMathEvaluator.MathResult.Success)
        assertEquals("60", (result as SafeMathEvaluator.MathResult.Success).value)
    }

    @Test
    fun `test decimal numbers`() {
        val result = evaluator.evaluate("10 / 4")
        assertTrue(result is SafeMathEvaluator.MathResult.Success)
        assertEquals("2.50", (result as SafeMathEvaluator.MathResult.Success).value)
    }

    @Test
    fun `test invalid math`() {
        val result = evaluator.evaluate("25 + *")
        assertTrue(result is SafeMathEvaluator.MathResult.Error)
    }

    @Test
    fun `test division by zero`() {
        val result = evaluator.evaluate("10 / 0")
        assertTrue(result is SafeMathEvaluator.MathResult.Error)
        assertEquals("Division by zero", (result as SafeMathEvaluator.MathResult.Error).message)
    }

    @Test
    fun `test unknown character`() {
        val result = evaluator.evaluate("2 + x")
        assertTrue(result is SafeMathEvaluator.MathResult.Error)
    }
}
