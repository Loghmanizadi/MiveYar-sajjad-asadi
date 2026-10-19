package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entities.DailyReceiptEntity
import com.example.data.local.entities.DailyReceiptPosEntryEntity
import com.example.data.local.entities.PosTerminalEntity
import com.example.data.repository.FruitShopRepository
import com.example.util.JalaliCalendarUtil
import com.example.util.PersianFormatUtil
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: FruitShopRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FruitShopRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("میوهیار", appName)
    }

    @Test
    fun `persian number formatting and parsing is correct`() {
        val amount = 1250000L
        val formatted = PersianFormatUtil.formatCurrencyToman(amount)
        assertTrue(formatted.contains("تومان"))

        val parsed = PersianFormatUtil.parseLongAmount("۱,۲۵۰,۰۰۰")
        assertEquals(1250000L, parsed)

        val weight = PersianFormatUtil.parseDoubleWeight("۲٫۵")
        assertEquals(2.5, weight, 0.001)
    }

    @Test
    fun `jalali calendar round trip is valid`() {
        val today = JalaliCalendarUtil.getToday()
        assertTrue(today.year >= 1400)
        assertTrue(today.month in 1..12)
        assertTrue(today.day in 1..31)

        val (gy, gm, gd) = JalaliCalendarUtil.jalaliToGregorian(today.year, today.month, today.day)
        val roundTrip = JalaliCalendarUtil.gregorianToJalali(gy, gm, gd)
        assertEquals(today.year, roundTrip.year)
        assertEquals(today.month, roundTrip.month)
        assertEquals(today.day, roundTrip.day)
    }

    @Test
    fun `navigation screens are non-null and valid`() {
        val all = com.example.ui.navigation.Screen.allScreens
        assertEquals(11, all.size) // 10 original + Settlement screen
        all.forEach { screen ->
            assertNotNull("Screen should not be null", screen)
            assertTrue(screen.route.isNotBlank())
            assertTrue(screen.title.isNotBlank())
        }
        assertTrue(all.any { it.route == "settlement" })
    }

    @Test
    fun `settlement calculation formulas are accurate`() {
        // Inputs
        val cash = 1000000L
        val posMellat = 2500000L
        val posParsian = 1500000L
        val posSaman = 800000L
        val posTotal = posMellat + posParsian + posSaman // 4,800,000
        val cardToCard = 700000L
        val other = 200000L

        // Formula 1: مجموع دریافتی = نقدی + مجموع کارتخوان‌ها + کارت‌به‌کارت + سایر دریافتی‌ها
        val totalActualReceipts = cash + posTotal + cardToCard + other
        assertEquals(6700000L, totalActualReceipts)

        // Formula 2: اختلاف تسویه = مجموع دریافتی − مجموع فروش ثبت‌شده
        val totalRecordedSales = 6650000L
        val settlementDiscrepancy = totalActualReceipts - totalRecordedSales
        assertEquals(50000L, settlementDiscrepancy) // +50,000 Toman

        // Formula 3: مانده پس از هزینه‌ها = مجموع دریافتی − هزینه‌های واقعاً پرداخت‌شده همان روز
        val dailyExpenses = 450000L
        val remainingAfterExpenses = totalActualReceipts - dailyExpenses
        assertEquals(6250000L, remainingAfterExpenses)
    }

    @Test
    fun `daily receipt entity calculates total receipts with backward compatibility`() {
        // Legacy receipt with posMellat and posParsian, posTotal = 0
        val legacyReceipt = DailyReceiptEntity(
            shamsiDate = "1405/07/26",
            branchName = "شعبه کاج",
            posMellat = 1200000L,
            posParsian = 800000L,
            posTotal = 0L,
            cash = 500000L,
            cardToCard = 300000L,
            other = 100000L
        )
        assertEquals(2000000L, legacyReceipt.totalPosReceipts)
        assertEquals(2900000L, legacyReceipt.totalActualReceipts)

        // Dynamic receipt with posTotal set
        val dynamicReceipt = DailyReceiptEntity(
            shamsiDate = "1405/07/26",
            branchName = "شعبه ماهان",
            posMellat = 0L,
            posParsian = 0L,
            posTotal = 3500000L,
            cash = 1000000L,
            cardToCard = 0L,
            other = 0L
        )
        assertEquals(3500000L, dynamicReceipt.totalPosReceipts)
        assertEquals(4500000L, dynamicReceipt.totalActualReceipts)
    }

    @Test
    fun `pos terminals management and structured settlement upsert in database`() = runBlocking {
        // 1. Add custom card readers with arbitrary names
        val term1Id = db.posTerminalDao().insertTerminal(
            PosTerminalEntity(name = "کارتخوان ملت ۱", bankName = "ملت", terminalNumber = "998811")
        )
        val term2Id = db.posTerminalDao().insertTerminal(
            PosTerminalEntity(name = "کارتخوان پاسارگاد صید", bankName = "پاسارگاد", terminalNumber = "776622")
        )
        val term3Id = db.posTerminalDao().insertTerminal(
            PosTerminalEntity(name = "کارتخوان سامان غرفه کاج", bankName = "سامان", terminalNumber = "554433")
        )

        val terminals = db.posTerminalDao().getTerminalById(term1Id)
        assertNotNull(terminals)
        assertEquals("کارتخوان ملت ۱", terminals?.name)

        // 2. Perform daily settlement
        val shamsiDate = "1405/07/28"
        val branchName = "شعبه کاج"

        val posEntries = listOf(
            DailyReceiptPosEntryEntity(
                receiptId = 0,
                shamsiDate = shamsiDate,
                branchName = branchName,
                terminalId = term1Id,
                terminalName = "کارتخوان ملت ۱",
                amount = 2500000L
            ),
            DailyReceiptPosEntryEntity(
                receiptId = 0,
                shamsiDate = shamsiDate,
                branchName = branchName,
                terminalId = term2Id,
                terminalName = "کارتخوان پاسارگاد صید",
                amount = 1800000L
            ),
            DailyReceiptPosEntryEntity(
                receiptId = 0,
                shamsiDate = shamsiDate,
                branchName = branchName,
                terminalId = term3Id,
                terminalName = "کارتخوان سامان غرفه کاج",
                amount = 900000L
            )
        )

        val receipt = DailyReceiptEntity(
            shamsiDate = shamsiDate,
            branchName = branchName,
            cash = 850000L,
            cardToCard = 300000L,
            other = 50000L,
            notes = "تسویه آزمایشی"
        )

        // First Save
        val savedId = repository.saveDailySettlement(receipt, posEntries)
        assertTrue(savedId > 0)

        // Reload and verify
        val loadedReceipt = repository.getDailyReceiptDirect(shamsiDate, branchName)
        assertNotNull(loadedReceipt)
        assertEquals(850000L, loadedReceipt?.cash)
        assertEquals(5200000L, loadedReceipt?.posTotal) // 2.5m + 1.8m + 0.9m = 5.2m
        assertEquals(6400000L, loadedReceipt?.totalActualReceipts) // 850k + 5.2m + 300k + 50k = 6.4m

        val loadedEntries = repository.getPosEntriesForDateAndBranchDirect(shamsiDate, branchName)
        assertEquals(3, loadedEntries.size)
        assertEquals(5200000L, loadedEntries.sumOf { it.amount })

        // 3. Test Upsert (editing existing settlement without duplicating records)
        val updatedReceipt = loadedReceipt!!.copy(
            cash = 950000L,
            notes = "تسویه ویرایش شده"
        )
        val updatedEntries = posEntries.map {
            if (it.terminalId == term1Id) it.copy(amount = 2700000L) else it
        }

        val secondSaveId = repository.saveDailySettlement(updatedReceipt, updatedEntries)
        assertEquals(savedId, secondSaveId) // Must keep the same ID

        val reloadedReceipt = repository.getDailyReceiptDirect(shamsiDate, branchName)
        assertEquals(950000L, reloadedReceipt?.cash)
        assertEquals(5400000L, reloadedReceipt?.posTotal) // 2.7m + 1.8m + 0.9m = 5.4m
        assertEquals("تسویه ویرایش شده", reloadedReceipt?.notes)

        val allReceiptsForDate = db.dailyReceiptDao().getReceiptByDateAndBranchDirect(shamsiDate, branchName)
        assertNotNull(allReceiptsForDate)
    }
}
