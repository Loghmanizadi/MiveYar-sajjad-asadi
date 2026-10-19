package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object SmsReportUtil {

    /**
     * Builds the exact weekly staff SMS template requested in Section 4.1:
     * «گزارش هفتگی [نام کارمند]، از [تاریخ شروع] تا [تاریخ پایان]؛ روزهای کارکرد: [تعداد]، مساعده: [مبلغ] تومان، پرداختی: [مبلغ] تومان، مانده حساب: [مبلغ] تومان. وضعیت حساب: [طلبکار/بدهکار/تسویه]. جزئیات: [خلاصه تاریخ‌های حضور، مرخصی و غیبت].»
     */
    fun buildEmployeeWeeklySmsBody(
        employeeName: String,
        startDate: String,
        endDate: String,
        workDaysCount: Int,
        totalAdvances: Long,
        totalPayments: Long,
        netBalance: Long,
        attendanceDetails: String
    ): String {
        val statusText = when {
            netBalance > 0 -> "طلبکار (مبلغ ${PersianFormatUtil.formatCurrencyToman(netBalance)} از فروشگاه)"
            netBalance == 0L -> "تسویه کامل"
            else -> "بدهکار (مبلغ ${PersianFormatUtil.formatCurrencyToman(kotlin.math.abs(netBalance))} به فروشگاه)"
        }

        val detailsClean = if (attendanceDetails.isNotBlank()) " جزئیات: $attendanceDetails." else ""

        return "گزارش هفتگی $employeeName، از $startDate تا $endDate؛ روزهای کارکرد: ${PersianFormatUtil.toPersianDigits(workDaysCount.toString())} روز، مساعده: ${PersianFormatUtil.formatCurrencyToman(totalAdvances)}، پرداختی: ${PersianFormatUtil.formatCurrencyToman(totalPayments)}، مانده حساب: ${PersianFormatUtil.formatCurrencyToman(netBalance)}. وضعیت حساب: $statusText.$detailsClean"
    }

    /**
     * Builds partner statement SMS text.
     */
    fun buildPartnerSmsBody(
        partnerName: String,
        dateRangeStr: String,
        sharePercentage: Double,
        withdrawals: Long,
        deposits: Long,
        netBalance: Long
    ): String {
        val pShare = PersianFormatUtil.toPersianDigits(sharePercentage.toString())
        val statusText = if (netBalance >= 0) "مانده طلب شما: ${PersianFormatUtil.formatCurrencyToman(netBalance)}" else "مانده بدهکاری: ${PersianFormatUtil.formatCurrencyToman(kotlin.math.abs(netBalance))}"
        return "گزارش حساب شریک محترم $partnerName، بازه: $dateRangeStr؛ سهم: $pShare٪، برداشت‌ها: ${PersianFormatUtil.formatCurrencyToman(withdrawals)}، واریزی‌ها: ${PersianFormatUtil.formatCurrencyToman(deposits)}، $statusText."
    }

    /**
     * Builds SMS text for daily end-of-day settlement.
     */
    fun buildDailySettlementSmsBody(
        storeName: String,
        shamsiDate: String,
        branchName: String,
        totalSales: Long,
        totalReceipts: Long,
        discrepancy: Long,
        expenses: Long,
        remainingBalance: Long
    ): String {
        val diffLabel = when {
            discrepancy > 0 -> "+${PersianFormatUtil.formatCurrencyToman(discrepancy)} (مازاد)"
            discrepancy < 0 -> "−${PersianFormatUtil.formatCurrencyToman(kotlin.math.abs(discrepancy))} (کسری)"
            else -> "۰ تومان (تطبیق کامل)"
        }
        return "گزارش تسویه روزانه $storeName • $branchName\n" +
                "تاریخ: $shamsiDate\n" +
                "• فروش سیستم: ${PersianFormatUtil.formatCurrencyToman(totalSales)}\n" +
                "• کل دریافتی: ${PersianFormatUtil.formatCurrencyToman(totalReceipts)}\n" +
                "• اختلاف تسویه: $diffLabel\n" +
                "• هزینه‌های روز: ${PersianFormatUtil.formatCurrencyToman(expenses)}\n" +
                "• مانده پس از هزینه‌ها: ${PersianFormatUtil.formatCurrencyToman(remainingBalance)}"
    }

    /**
     * Builds SMS text for daily partner report.
     */
    fun buildDailyPartnerSmsBody(
        storeName: String,
        partnerName: String,
        shamsiDate: String,
        branchName: String,
        sharePercentage: Double,
        totalSales: Long,
        expenses: Long,
        remainingNet: Long,
        partnerShareAmount: Long
    ): String {
        val pShare = PersianFormatUtil.toPersianDigits(sharePercentage.toString())
        return "گزارش روزانه شریک محترم $partnerName • $storeName\n" +
                "تاریخ: $shamsiDate ($branchName)\n" +
                "• فروش کل: ${PersianFormatUtil.formatCurrencyToman(totalSales)}\n" +
                "• هزینه‌های غرفه: ${PersianFormatUtil.formatCurrencyToman(expenses)}\n" +
                "• مانده قابل تقسیم: ${PersianFormatUtil.formatCurrencyToman(remainingNet)}\n" +
                "• سهم شراکت شما ($pShare٪): ${PersianFormatUtil.formatCurrencyToman(partnerShareAmount)}"
    }

    /**
     * Opens the device default SMS messenger pre-filled with recipient phone number and report body.
     * Uses zero external servers or fees.
     */
    fun openSmsApp(context: Context, phoneNumber: String, messageBody: String) {
        val cleanPhone = PersianFormatUtil.toEnglishDigits(phoneNumber).replace(" ", "").trim()
        try {
            val uri = if (cleanPhone.isNotBlank()) {
                Uri.parse("smsto:$cleanPhone")
            } else {
                Uri.parse("smsto:")
            }
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", messageBody)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback for devices without standard smsto handler
            try {
                val sendIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:$cleanPhone")
                    putExtra("sms_body", messageBody)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(sendIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "برنامه پیام‌رسان در این دستگاه یافت نشد", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
