package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.entities.DailyReceiptEntity
import com.example.data.local.entities.EmployeeEntity
import com.example.data.local.entities.EmployeePaymentEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.PartnerEntity
import com.example.data.local.entities.PartnerTransactionEntity
import com.example.data.local.entities.PurchaseEntity
import com.example.data.local.entities.SaleEntity
import com.example.data.local.entities.SupplierEntity
import com.example.data.local.entities.SupplierTransactionEntity
import com.example.data.repository.ProductStockInfo
import java.io.File
import java.io.FileOutputStream

object PdfReportUtil {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN = 36f

    /**
     * Generates a real Persian PDF for an employee weekly settlement statement.
     */
    fun createEmployeeWeeklyReportPdf(
        context: Context,
        employee: EmployeeEntity,
        weekDateRange: String,
        workDaysCount: Int,
        absentDaysCount: Int,
        leaveDaysCount: Int,
        baseWage: Long,
        earnedWage: Long,
        totalAdvances: Long,
        totalPayments: Long,
        netBalance: Long,
        paymentsList: List<EmployeePaymentEntity>,
        attendanceSummary: String
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(13, 92, 58) // Emerald
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            textAlign = Paint.Align.RIGHT
        }

        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(235, 246, 240)
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        // Draw Header Box
        val headerRect = RectF(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, MARGIN + 70f)
        canvas.drawRoundRect(headerRect, 10f, 10f, headerBgPaint)

        canvas.drawText("میوهیار • صورت‌حساب و کارکرد هفتگی پرسنل", PAGE_WIDTH - MARGIN - 16f, MARGIN + 26f, titlePaint)
        canvas.drawText("نام همکار: ${employee.fullName} • تلفن: ${employee.phone.ifBlank { "ثبت نشده" }}", PAGE_WIDTH - MARGIN - 16f, MARGIN + 45f, boldTextPaint)
        canvas.drawText("دوره گزارش: $weekDateRange • تاریخ صدور: ${JalaliCalendarUtil.getToday().toFormattedString()}", PAGE_WIDTH - MARGIN - 16f, MARGIN + 62f, textPaint)

        var yPos = MARGIN + 100f

        // Financial & Work Summary Box
        val summaryRect = RectF(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos + 130f)
        canvas.drawRoundRect(summaryRect, 8f, 8f, headerBgPaint)

        val colRight = PAGE_WIDTH - MARGIN - 16f
        val colLeft = MARGIN + 16f

        canvas.drawText("دستمزد ثابت هفتگی:", colRight, yPos + 24f, textPaint)
        canvas.drawText(PersianFormatUtil.formatCurrencyToman(baseWage), colRight - 150f, yPos + 24f, boldTextPaint)

        canvas.drawText("روزهای کارکرد / حاضر:", colRight, yPos + 44f, textPaint)
        canvas.drawText("${PersianFormatUtil.toPersianDigits(workDaysCount.toString())} روز (غیبت: ${PersianFormatUtil.toPersianDigits(absentDaysCount.toString())}، مرخصی: ${PersianFormatUtil.toPersianDigits(leaveDaysCount.toString())})", colRight - 150f, yPos + 44f, boldTextPaint)

        canvas.drawText("مجموع مساعده‌های این هفته:", colRight, yPos + 64f, textPaint)
        canvas.drawText("− " + PersianFormatUtil.formatCurrencyToman(totalAdvances), colRight - 150f, yPos + 64f, boldTextPaint)

        canvas.drawText("مجموع پرداخت‌های تسویه:", colRight, yPos + 84f, textPaint)
        canvas.drawText("− " + PersianFormatUtil.formatCurrencyToman(totalPayments), colRight - 150f, yPos + 84f, boldTextPaint)

        canvas.drawLine(MARGIN + 10f, yPos + 96f, PAGE_WIDTH - MARGIN - 10f, yPos + 96f, linePaint)

        val balanceText = when {
            netBalance > 0 -> "طلبکار: کارمند مبلغ ${PersianFormatUtil.formatCurrencyToman(netBalance)} از فروشگاه طلب دارد."
            netBalance == 0L -> "تسویه کامل: حساب این دوره صفر و تسویه است."
            else -> "بدهکار: کارمند مبلغ ${PersianFormatUtil.formatCurrencyToman(kotlin.math.abs(netBalance))} به فروشگاه بدهکار است."
        }

        canvas.drawText("وضعیت مانده نهایی:", colRight, yPos + 116f, boldTextPaint)
        canvas.drawText(balanceText, colRight - 110f, yPos + 116f, titlePaint)

        yPos += 150f

        // Table of advances and payments
        canvas.drawText("ریز تراکنش‌های مالی و مساعده‌های ثبت شده در این هفته:", colRight, yPos, boldTextPaint)
        yPos += 16f

        if (paymentsList.isEmpty()) {
            canvas.drawText("هیچ مساعده یا پرداختی در این دوره ثبت نشده است.", colRight, yPos + 16f, textPaint)
            yPos += 30f
        } else {
            paymentsList.forEachIndexed { index, p ->
                val typeName = if (p.type == "ADVANCE") "مساعده" else "پرداخت حقوق"
                val lineStr = "${index + 1}. $typeName • تاریخ: ${p.shamsiDate} • مبلغ: ${PersianFormatUtil.formatCurrencyToman(p.amount)} (${p.paymentMethod}) ${p.notes}"
                canvas.drawText(lineStr, colRight, yPos + 14f, textPaint)
                yPos += 18f
            }
        }

        yPos += 20f
        if (attendanceSummary.isNotBlank()) {
            canvas.drawText("خلاصه وضعیت حضور و غیاب روزهای دوره:", colRight, yPos, boldTextPaint)
            yPos += 16f
            canvas.drawText(attendanceSummary, colRight, yPos, textPaint)
            yPos += 30f
        }

        // Signatures area
        yPos = PAGE_HEIGHT - MARGIN - 60f
        canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, linePaint)
        canvas.drawText("امضاء و تأیید مدیریت فروشگاه", PAGE_WIDTH - MARGIN - 60f, yPos + 30f, boldTextPaint)
        canvas.drawText("امضاء همکار محترم", MARGIN + 160f, yPos + 30f, boldTextPaint)

        document.finishPage(page)

        val outFile = File(context.cacheDir, "گزارش_حقوق_${employee.fullName.replace(" ", "_")}.pdf")
        FileOutputStream(outFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()
        return outFile
    }

    /**
     * Generates a real Persian PDF for a sales report.
     */
    fun createSalesReportPdf(
        context: Context,
        storeName: String,
        dateRangeStr: String,
        sales: List<SaleEntity>,
        totalWeightedSales: Long,
        totalNonWeightedSales: Long,
        totalSales: Long,
        dailyReceipt: DailyReceiptEntity?,
        branchName: String = "همه شعب / غرفه‌ها"
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(13, 92, 58)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }

        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(238, 248, 242)
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        val colRight = PAGE_WIDTH - MARGIN - 16f

        // Header
        val headerRect = RectF(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, MARGIN + 60f)
        canvas.drawRoundRect(headerRect, 8f, 8f, headerBgPaint)
        canvas.drawText("گزارش فروش و تطبیق صندوق • $storeName", colRight, MARGIN + 24f, titlePaint)
        canvas.drawText("بازه: $dateRangeStr • غرفه: $branchName • تاریخ تهیه: ${JalaliCalendarUtil.getToday().toFormattedString()}", colRight, MARGIN + 44f, textPaint)

        var yPos = MARGIN + 80f

        // Summary Card
        val cardRect = RectF(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos + 90f)
        canvas.drawRoundRect(cardRect, 8f, 8f, headerBgPaint)

        canvas.drawText("جمع فروش وزنی:", colRight, yPos + 22f, textPaint)
        canvas.drawText(PersianFormatUtil.formatCurrencyToman(totalWeightedSales), colRight - 150f, yPos + 22f, boldTextPaint)

        canvas.drawText("جمع فروش غیروزنی:", colRight, yPos + 40f, textPaint)
        canvas.drawText(PersianFormatUtil.formatCurrencyToman(totalNonWeightedSales), colRight - 150f, yPos + 40f, boldTextPaint)

        canvas.drawText("جمع کل فروش ثبت شده:", colRight, yPos + 58f, boldTextPaint)
        canvas.drawText(PersianFormatUtil.formatCurrencyToman(totalSales), colRight - 150f, yPos + 58f, titlePaint)

        if (dailyReceipt != null) {
            val totalActual = dailyReceipt.totalActualReceipts
            val diff = totalActual - totalSales
            val diffLabel = if (diff >= 0) "+${PersianFormatUtil.formatCurrencyToman(diff)}" else "−${PersianFormatUtil.formatCurrencyToman(kotlin.math.abs(diff))}"
            canvas.drawText("جمع دریافتی واقعی پایان روز: ${PersianFormatUtil.formatCurrencyToman(totalActual)} • اختلاف تطبیق: $diffLabel", colRight, yPos + 78f, textPaint)
        }

        yPos += 110f

        canvas.drawText("فهرست فاکتورهای فروش ثبت شده در این بازه:", colRight, yPos, boldTextPaint)
        yPos += 18f

        // List up to 25 sales rows per page
        val displaySales = sales.take(25)
        displaySales.forEachIndexed { i, s ->
            val rowStr = "${i + 1}. فاکتور #${s.id} • ${s.shamsiDate} • ${s.branchName} • مبلغ: ${PersianFormatUtil.formatCurrencyToman(s.totalAmount)} (${s.paymentMethod}) ${s.notes}"
            canvas.drawText(rowStr, colRight, yPos, textPaint)
            yPos += 16f
        }

        document.finishPage(page)

        val outFile = File(context.cacheDir, "گزارش_فروش_${JalaliCalendarUtil.getToday().toFormattedString().replace("/", "_")}.pdf")
        FileOutputStream(outFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()
        return outFile
    }

    /**
     * Generates a real Persian PDF for an expense statement.
     */
    fun createExpensesReportPdf(
        context: Context,
        storeName: String,
        dateRangeStr: String,
        expenses: List<ExpenseEntity>,
        totalAmount: Long
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(186, 26, 26) // Loss red for expenses
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }

        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(255, 245, 245)
        }

        val colRight = PAGE_WIDTH - MARGIN - 16f

        val headerRect = RectF(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, MARGIN + 60f)
        canvas.drawRoundRect(headerRect, 8f, 8f, headerBgPaint)
        canvas.drawText("گزارش هزینه‌های جاری و روزانه • $storeName", colRight, MARGIN + 24f, titlePaint)
        canvas.drawText("بازه: $dateRangeStr • جمع کل هزینه‌ها: ${PersianFormatUtil.formatCurrencyToman(totalAmount)}", colRight, MARGIN + 44f, boldTextPaint)

        var yPos = MARGIN + 85f

        expenses.take(30).forEachIndexed { index, exp ->
            val rowStr = "${index + 1}. ${exp.title} (${exp.category}) • تاریخ: ${exp.shamsiDate} • مبلغ: ${PersianFormatUtil.formatCurrencyToman(exp.amount)} [${exp.paymentMethod}]"
            canvas.drawText(rowStr, colRight, yPos, textPaint)
            yPos += 17f
        }

        document.finishPage(page)

        val outFile = File(context.cacheDir, "گزارش_هزینه‌ها_${JalaliCalendarUtil.getToday().toFormattedString().replace("/", "_")}.pdf")
        FileOutputStream(outFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()
        return outFile
    }

    /**
     * Generates a Persian RTL PDF report for partner daily profit share and settlement.
     */
    fun createPartnerDailyReportPdf(
        context: Context,
        storeName: String,
        partner: PartnerEntity,
        dateRangeStr: String,
        branchName: String,
        totalSales: Long,
        totalExpenses: Long,
        totalReceipts: Long,
        netRemainingToShare: Long,
        partnerShareAmount: Long,
        partnerTransactions: List<PartnerTransactionEntity>
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(13, 92, 58) // Emerald
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }

        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(238, 248, 242)
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        val colRight = PAGE_WIDTH - MARGIN - 16f

        // Header Box
        val headerRect = RectF(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, MARGIN + 68f)
        canvas.drawRoundRect(headerRect, 8f, 8f, headerBgPaint)
        canvas.drawText("صورت‌حساب و گزارش سهم شریک • $storeName", colRight, MARGIN + 24f, titlePaint)
        canvas.drawText("نام شریک: ${partner.fullName} • سهم شراکت: ${PersianFormatUtil.toPersianDigits(partner.sharePercentage.toString())}٪ • غرفه: $branchName", colRight, MARGIN + 44f, boldTextPaint)
        canvas.drawText("دوره گزارش: $dateRangeStr • تاریخ تهیه: ${JalaliCalendarUtil.getToday().toFormattedString()}", colRight, MARGIN + 60f, textPaint)

        var yPos = MARGIN + 85f

        // Financial Summary Box
        val cardRect = RectF(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos + 125f)
        canvas.drawRoundRect(cardRect, 8f, 8f, headerBgPaint)

        canvas.drawText("کل فروش ثبت‌شده در دوره:", colRight, yPos + 22f, textPaint)
        canvas.drawText(PersianFormatUtil.formatCurrencyToman(totalSales), colRight - 160f, yPos + 22f, boldTextPaint)

        canvas.drawText("کل دریافتی‌های صندوق (نقد و پوز):", colRight, yPos + 40f, textPaint)
        canvas.drawText(PersianFormatUtil.formatCurrencyToman(totalReceipts), colRight - 160f, yPos + 40f, boldTextPaint)

        canvas.drawText("هزینه‌های پرداخت‌شده دوره:", colRight, yPos + 58f, textPaint)
        canvas.drawText("− " + PersianFormatUtil.formatCurrencyToman(totalExpenses), colRight - 160f, yPos + 58f, boldTextPaint)

        canvas.drawLine(MARGIN + 10f, yPos + 70f, PAGE_WIDTH - MARGIN - 10f, yPos + 70f, linePaint)

        canvas.drawText("مانده دریافتی پس از کسر هزینه‌ها:", colRight, yPos + 88f, boldTextPaint)
        canvas.drawText(PersianFormatUtil.formatCurrencyToman(netRemainingToShare), colRight - 160f, yPos + 88f, titlePaint)

        canvas.drawText("سهم ریالی شریک (${PersianFormatUtil.toPersianDigits(partner.sharePercentage.toString())}٪):", colRight, yPos + 108f, boldTextPaint)
        canvas.drawText(PersianFormatUtil.formatCurrencyToman(partnerShareAmount), colRight - 160f, yPos + 108f, titlePaint)

        yPos += 145f

        // Partner transactions in period
        canvas.drawText("ریز تراکنش‌ها، برداشت‌ها و واریزی‌های شریک در این دوره:", colRight, yPos, boldTextPaint)
        yPos += 18f

        if (partnerTransactions.isEmpty()) {
            canvas.drawText("هیچ تراکنش مالی یا برداشتی برای این شریک در این دوره ثبت نشده است.", colRight, yPos + 10f, textPaint)
            yPos += 30f
        } else {
            partnerTransactions.take(15).forEachIndexed { i, tx ->
                val typeLabel = when (tx.type) {
                    "WITHDRAWAL" -> "برداشت سود/سرمایه"
                    "DEPOSIT" -> "واریز سرمایه"
                    else -> "تسویه"
                }
                val rowStr = "${i + 1}. $typeLabel • تاریخ: ${tx.shamsiDate} • مبلغ: ${PersianFormatUtil.formatCurrencyToman(tx.amount)} (${tx.paymentMethod}) ${tx.notes}"
                canvas.drawText(rowStr, colRight, yPos, textPaint)
                yPos += 16f
            }
        }

        // Signatures Area
        yPos = PAGE_HEIGHT - MARGIN - 60f
        canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, linePaint)
        canvas.drawText("امضاء و تأیید مدیریت فروشگاه", PAGE_WIDTH - MARGIN - 60f, yPos + 30f, boldTextPaint)
        canvas.drawText("امضاء و تأیید شریک محترم", MARGIN + 160f, yPos + 30f, boldTextPaint)

        document.finishPage(page)

        val cleanName = partner.fullName.replace(" ", "_")
        val outFile = File(context.cacheDir, "گزارش_شریک_${cleanName}_${JalaliCalendarUtil.getToday().toFormattedString().replace("/", "_")}.pdf")
        FileOutputStream(outFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()
        return outFile
    }

    /**
     * Helper to view or share the generated PDF using Android standard Share Sheet.
     */
    fun sharePdf(context: Context, pdfFile: File, chooserTitle: String = "ارسال یا چاپ گزارش PDF") {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, pdfFile.nameWithoutExtension)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Helper to open the PDF directly in device's PDF viewer.
     */
    fun openPdf(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            sharePdf(context, pdfFile)
        }
    }
}
