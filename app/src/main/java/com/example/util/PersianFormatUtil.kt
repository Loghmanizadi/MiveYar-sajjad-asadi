package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Utility for Persian numbers, currency, weight, and input normalization.
 */
object PersianFormatUtil {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val ARABIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    /**
     * Converts any Latin or Arabic digits in the string to Persian digits (۰-۹).
     */
    fun toPersianDigits(text: String): String {
        val sb = java.lang.StringBuilder()
        for (ch in text) {
            when (ch) {
                in '0'..'9' -> sb.append(PERSIAN_DIGITS[ch - '0'])
                in '٠'..'٩' -> sb.append(PERSIAN_DIGITS[ch - '٠'])
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Converts Persian and Arabic digits to standard English/Latin digits (0-9).
     * Also normalizes Persian decimal separator '/' or '،' to standard dot '.'.
     */
    fun toEnglishDigits(text: String): String {
        val sb = java.lang.StringBuilder()
        for (ch in text) {
            when {
                ch in '۰'..'۹' -> sb.append((ch - '۰'))
                ch in '٠'..'٩' -> sb.append((ch - '٠'))
                ch == '٫' || ch == '/' || ch == '،' -> sb.append('.')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Safely parses user input into Long (currency in Toman).
     * Removes commas, spaces, and handles Persian digits.
     */
    fun parseLongAmount(input: String): Long {
        val clean = toEnglishDigits(input).replace(",", "").replace(" ", "").trim()
        return clean.toLongOrNull() ?: 0L
    }

    /**
     * Safely parses user input into Double (weight in kg).
     */
    fun parseDoubleWeight(input: String): Double {
        val clean = toEnglishDigits(input).replace(",", "").replace(" ", "").trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    /**
     * Formats a Long amount as Persian currency with commas (e.g. "۱۵۰,۰۰۰ تومان").
     */
    fun formatCurrencyToman(amount: Long, withUnit: Boolean = true): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' }
        val formatter = DecimalFormat("#,###", symbols)
        val formatted = if (amount == 0L) "0" else formatter.format(amount)
        val persian = toPersianDigits(formatted)
        return if (withUnit) "$persian تومان" else persian
    }

    /**
     * Formats weight in kg with Persian numbers (e.g. "۲٫۵ کیلوگرم" or "۱۴۰ کیلوگرم").
     */
    fun formatWeightKg(weight: Double, withUnit: Boolean = true): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply { decimalSeparator = '.' }
        val formatter = DecimalFormat("0.##", symbols)
        val formatted = formatter.format(weight)
        val persian = toPersianDigits(formatted)
        return if (withUnit) "$persian کیلوگرم" else persian
    }

    /**
     * Formats count / package (e.g. "۲۴ جعبه" or "۱۰ عدد")
     */
    fun formatCount(count: Int, unit: String): String {
        val persian = toPersianDigits(count.toString())
        return "$persian $unit"
    }
}
