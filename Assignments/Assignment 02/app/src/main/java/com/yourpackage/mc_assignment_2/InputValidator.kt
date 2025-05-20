package com.yourpackage.mc_assignment_2

object InputValidator {
    fun checkFlightCode(code: String): Boolean {
        val normalizedCode = code.replace("\\s".toRegex(), "").uppercase()
        return normalizedCode.matches(Regex("[A-Z]{2}\\d{1,4}"))
    }
}