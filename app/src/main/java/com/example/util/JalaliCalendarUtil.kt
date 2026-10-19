package com.example.util

import java.util.Calendar

/**
 * Robust Solar Hijri (Jalali / شمسی) Calendar utility for MiveYar.
 * Provides accurate conversions, formatted dates, day-of-week, and period helpers.
 */
data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int
) : Comparable<JalaliDate> {
    override operator fun compareTo(other: JalaliDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    /**
     * Formats as "1405/07/26" (always 4 digits year, 2 digits month, 2 digits day)
     */
    fun toFormattedString(): String {
        return String.format("%04d/%02d/%02d", year, month, day)
    }

    /**
     * Formats as "۲۶ مهر ۱۴۰۵"
     */
    fun toPersianSpokenString(): String {
        val monthName = JalaliCalendarUtil.getMonthName(month)
        val pDay = PersianFormatUtil.toPersianDigits(day.toString())
        val pYear = PersianFormatUtil.toPersianDigits(year.toString())
        return "$pDay $monthName $pYear"
    }
}

object JalaliCalendarUtil {

    private val MONTH_NAMES = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    private val WEEKDAY_NAMES = arrayOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
    )

    fun getMonthName(month: Int): String {
        return if (month in 1..12) MONTH_NAMES[month - 1] else ""
    }

    fun getAllMonthNames(): List<String> = MONTH_NAMES.toList()

    /**
     * Returns current Jalali date
     */
    fun getToday(): JalaliDate {
        val cal = Calendar.getInstance()
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Returns today's weekday in Persian (e.g. "شنبه")
     */
    fun getTodayWeekday(): String {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "شنبه"
            Calendar.SUNDAY -> "یکشنبه"
            Calendar.MONDAY -> "دوشنبه"
            Calendar.TUESDAY -> "سه‌شنبه"
            Calendar.WEDNESDAY -> "چهارشنبه"
            Calendar.THURSDAY -> "پنج‌شنبه"
            Calendar.FRIDAY -> "جمعه"
            else -> ""
        }
    }

    /**
     * Returns Jalali date for a given timestamp in millis
     */
    fun getJalaliDateFromMillis(millis: Long): JalaliDate {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Returns yesterday's Jalali date
     */
    fun getYesterday(): JalaliDate {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Returns the start and end Jalali date for current week (Saturday to Friday)
     */
    fun getCurrentWeekRange(): Pair<JalaliDate, JalaliDate> {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        // In Java Calendar, Sunday is 1, Saturday is 7
        val daysFromSaturday = when (dayOfWeek) {
            Calendar.SATURDAY -> 0
            Calendar.SUNDAY -> 1
            Calendar.MONDAY -> 2
            Calendar.TUESDAY -> 3
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 5
            Calendar.FRIDAY -> 6
            else -> 0
        }

        val startCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -daysFromSaturday) }
        val endCal = (startCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 6) }

        val startJalali = gregorianToJalali(
            startCal.get(Calendar.YEAR),
            startCal.get(Calendar.MONTH) + 1,
            startCal.get(Calendar.DAY_OF_MONTH)
        )
        val endJalali = gregorianToJalali(
            endCal.get(Calendar.YEAR),
            endCal.get(Calendar.MONTH) + 1,
            endCal.get(Calendar.DAY_OF_MONTH)
        )
        return Pair(startJalali, endJalali)
    }

    /**
     * Returns start and end of current Jalali month
     */
    fun getCurrentMonthRange(): Pair<JalaliDate, JalaliDate> {
        val today = getToday()
        val start = JalaliDate(today.year, today.month, 1)
        val daysInMonth = getDaysInJalaliMonth(today.year, today.month)
        val end = JalaliDate(today.year, today.month, daysInMonth)
        return Pair(start, end)
    }

    /**
     * Returns start and end of current Jalali year (Farvardin 1 to Esfand 29/30)
     */
    fun getCurrentYearRange(): Pair<JalaliDate, JalaliDate> {
        val today = getToday()
        val start = JalaliDate(today.year, 1, 1)
        val daysInEsfand = if (isLeapJalaliYear(today.year)) 30 else 29
        val end = JalaliDate(today.year, 12, daysInEsfand)
        return Pair(start, end)
    }

    /**
     * Add or subtract days from a JalaliDate
     */
    fun addDays(date: JalaliDate, days: Int): JalaliDate {
        val (gy, gm, gd) = jalaliToGregorian(date.year, date.month, date.day)
        val cal = Calendar.getInstance().apply {
            set(gy, gm - 1, gd)
            add(Calendar.DAY_OF_MONTH, days)
        }
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Calculate days between two Jalali dates (inclusive difference)
     */
    fun daysBetween(start: JalaliDate, end: JalaliDate): Int {
        val (gy1, gm1, gd1) = jalaliToGregorian(start.year, start.month, start.day)
        val (gy2, gm2, gd2) = jalaliToGregorian(end.year, end.month, end.day)
        val c1 = Calendar.getInstance().apply { set(gy1, gm1 - 1, gd1, 0, 0, 0) }
        val c2 = Calendar.getInstance().apply { set(gy2, gm2 - 1, gd2, 0, 0, 0) }
        val diffMillis = c2.timeInMillis - c1.timeInMillis
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    /**
     * Calculate the immediately preceding period of equal length
     */
    fun getPreviousPeriod(startStr: String, endStr: String): Pair<String, String>? {
        val s = parseJalali(startStr) ?: return null
        val e = parseJalali(endStr) ?: return null
        val diff = daysBetween(s, e)
        if (diff < 0) return null
        val count = diff + 1
        val prevEnd = addDays(s, -1)
        val prevStart = addDays(prevEnd, -(count - 1))
        return Pair(prevStart.toFormattedString(), prevEnd.toFormattedString())
    }

    fun isLeapJalaliYear(year: Int): Boolean {
        // 33-year cycle standard
        val rem = year % 33
        return rem in intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
    }

    fun getDaysInJalaliMonth(year: Int, month: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            month == 12 -> if (isLeapJalaliYear(year)) 30 else 29
            else -> 30
        }
    }

    /**
     * Parses "YYYY/MM/DD" or "YYYY-MM-DD" into JalaliDate
     */
    fun parseJalali(dateStr: String): JalaliDate? {
        val normalized = PersianFormatUtil.toEnglishDigits(dateStr).trim()
            .replace("-", "/")
        val parts = normalized.split("/")
        if (parts.size == 3) {
            val y = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            val d = parts[2].toIntOrNull() ?: return null
            if (m in 1..12 && d in 1..31) {
                return JalaliDate(y, m, d)
            }
        }
        return null
    }

    /**
     * Astronomical Gregorian to Jalali conversion algorithm
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val jy: Int
        val jm: Int
        val jd: Int

        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + g_d_m[gm - 1]
        jy = -1595 + (33 * (days / 12053))
        days %= 12053
        var jyAdd = 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jyAdd += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        val finalJy = jy + jyAdd
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return JalaliDate(finalJy, jm, jd)
    }

    /**
     * Jalali to Gregorian conversion algorithm
     */
    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val adjustedJy = jy + 1595
        var days = -355668 + (365 * adjustedJy) + ((adjustedJy / 33) * 8) + (((adjustedJy % 33) + 3) / 4) + jd + (if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186)
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days--
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        val sal_a = intArrayOf(0, 31, if ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13 && days >= sal_a[gm]) {
            days -= sal_a[gm]
            gm++
        }
        val gd = days + 1
        return Triple(gy, gm, gd)
    }
}
