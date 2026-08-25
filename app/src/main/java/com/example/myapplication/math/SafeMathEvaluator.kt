package com.example.myapplication.math

import java.util.Locale
import kotlin.math.round

/**
 * A safe, local mathematical expression evaluator.
 * Supports +, -, *, /, parentheses, and decimal numbers.
 */
class SafeMathEvaluator {

    fun evaluate(expression: String): MathResult {
        return try {
            val tokens = tokenize(expression)
            if (tokens.isEmpty()) return MathResult.Error("Empty expression")
            
            val parser = Parser(tokens)
            val result = parser.parseExpression()
            
            if (parser.hasNext()) {
                MathResult.Error("Unexpected tokens at end of expression")
            } else {
                MathResult.Success(formatResult(result))
            }
        } catch (e: Exception) {
            MathResult.Error(e.message ?: "Invalid expression")
        }
    }

    private fun formatResult(value: Double): String {
        return if (value == round(value)) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", value)
        }
    }

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isWhitespace() -> i++
                c in "+-*/()" -> {
                    tokens.add(c.toString())
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < input.length && (input[i].isDigit() || input[i] == '.')) {
                        sb.append(input[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                else -> throw Exception("Unknown character: $c")
            }
        }
        return tokens
    }

    private class Parser(private val tokens: List<String>) {
        private var pos = 0

        fun hasNext() = pos < tokens.size
        private fun peek() = if (hasNext()) tokens[pos] else null
        private fun consume() = tokens[pos++]

        fun parseExpression(): Double {
            var result = parseTerm()
            while (peek() == "+" || peek() == "-") {
                val op = consume()
                val nextTerm = parseTerm()
                if (op == "+") result += nextTerm else result -= nextTerm
            }
            return result
        }

        private fun parseTerm(): Double {
            var result = parseFactor()
            while (peek() == "*" || peek() == "/") {
                val op = consume()
                val nextFactor = parseFactor()
                if (op == "*") {
                    result *= nextFactor
                } else {
                    if (nextFactor == 0.0) throw Exception("Division by zero")
                    result /= nextFactor
                }
            }
            return result
        }

        private fun parseFactor(): Double {
            val token = peek() ?: throw Exception("Unexpected end of expression")
            return when {
                token == "(" -> {
                    consume()
                    val result = parseExpression()
                    if (peek() != ")") throw Exception("Missing closing parenthesis")
                    consume()
                    result
                }
                token == "-" -> {
                    consume()
                    -parseFactor()
                }
                else -> {
                    token.toDoubleOrNull()?.also { consume() } ?: throw Exception("Invalid number: $token")
                }
            }
        }
    }

    sealed class MathResult {
        data class Success(val value: String) : MathResult()
        data class Error(val message: String) : MathResult()
    }
}
