package com.example.swpp.tododapp.tasks

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for [dueDateLabel], one per Tip in specs/DueDateUtils.spec.md.
 */
class DueDateUtilsTest {

    private fun date(text: String): LocalDate = LocalDate.parse(text)

    private fun assertLabel(expected: String, dueDate: String?, today: String, isCompleted: Boolean) {
        // WHEN - Build the label
        val result = dueDateLabel(dueDate?.let { date(it) }, date(today), isCompleted)

        // THEN - The label matches the spec
        assertEquals(expected, result)
    }

    // ---- Success cases ----

    @Test
    fun dueDateLabel_completedWithFutureDueDate_returnsClear() {
        // GIVEN - A completed task due in 5 days
        assertLabel("CLEAR", dueDate = "2026-10-25", today = "2026-10-20", isCompleted = true)
    }

    @Test
    fun dueDateLabel_completedWithOverdueDueDate_returnsClear() {
        // GIVEN - A completed task overdue by 3 days
        assertLabel("CLEAR", dueDate = "2026-10-17", today = "2026-10-20", isCompleted = true)
    }

    @Test
    fun dueDateLabel_completedWithNoDueDate_returnsClear() {
        // GIVEN - A completed task with no due date
        assertLabel("CLEAR", dueDate = null, today = "2026-10-20", isCompleted = true)
    }

    @Test
    fun dueDateLabel_activeWithNoDueDate_returnsEmpty() {
        // GIVEN - An active task with no due date
        assertLabel("", dueDate = null, today = "2026-10-20", isCompleted = false)
    }

    @Test
    fun dueDateLabel_activeDueInFiveDays_returnsDMinus5() {
        // GIVEN - An active task due later in the same month
        assertLabel("D-5", dueDate = "2026-10-25", today = "2026-10-20", isCompleted = false)
    }

    @Test
    fun dueDateLabel_activeOverdueByThreeDays_returnsDPlus3() {
        // GIVEN - An active task overdue within the same month
        assertLabel("D+3", dueDate = "2026-10-17", today = "2026-10-20", isCompleted = false)
    }

    @Test
    fun dueDateLabel_activeDueInTwoMonths_returnsDMinus66() {
        // GIVEN - An active task due 11 + 30 + 25 days later
        assertLabel("D-66", dueDate = "2026-12-25", today = "2026-10-20", isCompleted = false)
    }

    @Test
    fun dueDateLabel_activeOverdueByOneYear_returnsDPlus365() {
        // GIVEN - An active task overdue by one non-leap year
        assertLabel("D+365", dueDate = "2025-10-20", today = "2026-10-20", isCompleted = false)
    }

    // ---- Edge cases: due-today boundary ----

    @Test
    fun dueDateLabel_activeOverdueByOneDay_returnsDPlus1() {
        assertLabel("D+1", dueDate = "2026-10-19", today = "2026-10-20", isCompleted = false)
    }

    @Test
    fun dueDateLabel_activeDueToday_returnsDDay() {
        assertLabel("D-Day", dueDate = "2026-10-20", today = "2026-10-20", isCompleted = false)
    }

    @Test
    fun dueDateLabel_activeDueTomorrow_returnsDMinus1() {
        assertLabel("D-1", dueDate = "2026-10-21", today = "2026-10-20", isCompleted = false)
    }

    // ---- Edge cases: completed/active boundary ----

    @Test
    fun dueDateLabel_completedDueToday_returnsClear() {
        assertLabel("CLEAR", dueDate = "2026-10-20", today = "2026-10-20", isCompleted = true)
    }

    // ---- Edge cases: day-of-month boundary ----

    @Test
    fun dueDateLabel_activeDueInThirtyDays_returnsDMinus30() {
        assertLabel("D-30", dueDate = "2026-11-19", today = "2026-10-20", isCompleted = false)
    }

    @Test
    fun dueDateLabel_activeDueInOneMonth_returnsDMinus31() {
        assertLabel("D-31", dueDate = "2026-11-20", today = "2026-10-20", isCompleted = false)
    }

    @Test
    fun dueDateLabel_activeDueInThirtyTwoDays_returnsDMinus32() {
        assertLabel("D-32", dueDate = "2026-11-21", today = "2026-10-20", isCompleted = false)
    }

    @Test
    fun dueDateLabel_activeOverdueByOneMonth_returnsDPlus30() {
        assertLabel("D+30", dueDate = "2026-09-20", today = "2026-10-20", isCompleted = false)
    }

    // ---- Edge cases: month end ----

    @Test
    fun dueDateLabel_monthEndOverdueByOneDay_returnsDPlus1() {
        assertLabel("D+1", dueDate = "2026-10-30", today = "2026-10-31", isCompleted = false)
    }

    @Test
    fun dueDateLabel_monthEndDueNextMonthFirstDay_returnsDMinus1() {
        assertLabel("D-1", dueDate = "2026-11-01", today = "2026-10-31", isCompleted = false)
    }

    // ---- Edge cases: year end ----

    @Test
    fun dueDateLabel_yearEndDueNewYearsDay_returnsDMinus1() {
        assertLabel("D-1", dueDate = "2027-01-01", today = "2026-12-31", isCompleted = false)
    }

    @Test
    fun dueDateLabel_newYearsDayOverdueFromLastYear_returnsDPlus1() {
        assertLabel("D+1", dueDate = "2026-12-31", today = "2027-01-01", isCompleted = false)
    }

    // ---- Edge cases: leap day ----

    @Test
    fun dueDateLabel_leapYearDueLeapDay_returnsDMinus1() {
        assertLabel("D-1", dueDate = "2028-02-29", today = "2028-02-28", isCompleted = false)
    }

    @Test
    fun dueDateLabel_leapYearDueMarchFirst_returnsDMinus2() {
        assertLabel("D-2", dueDate = "2028-03-01", today = "2028-02-28", isCompleted = false)
    }

    @Test
    fun dueDateLabel_leapDayDueMarchFirst_returnsDMinus1() {
        assertLabel("D-1", dueDate = "2028-03-01", today = "2028-02-29", isCompleted = false)
    }

    @Test
    fun dueDateLabel_nonLeapYearDueMarchFirst_returnsDMinus1() {
        assertLabel("D-1", dueDate = "2027-03-01", today = "2027-02-28", isCompleted = false)
    }

    // ---- Edge cases: one year apart ----

    @Test
    fun dueDateLabel_activeDueInOneYearMinusOneDay_returnsDMinus364() {
        assertLabel("D-364", dueDate = "2027-10-19", today = "2026-10-20", isCompleted = false)
    }

    @Test
    fun dueDateLabel_activeDueInOneYear_returnsDMinus365() {
        assertLabel("D-365", dueDate = "2027-10-20", today = "2026-10-20", isCompleted = false)
    }

    @Test
    fun dueDateLabel_activeDueInOneYearPlusOneDay_returnsDMinus366() {
        assertLabel("D-366", dueDate = "2027-10-21", today = "2026-10-20", isCompleted = false)
    }

    @Test
    fun dueDateLabel_activeDueInOneYearOverLeapDay_returnsDMinus366() {
        assertLabel("D-366", dueDate = "2029-02-28", today = "2028-02-28", isCompleted = false)
    }
}
