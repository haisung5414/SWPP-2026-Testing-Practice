package com.example.swpp.tododapp.tasks

import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.swpp.tododapp.data.Task
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Tests for [setDueDateLabel], one per adapter-level Tip in specs/TasksListBindings.spec.md.
 * The list-level Tips are in [TasksFragmentDueDateTest].
 */
@SmallTest
@RunWith(AndroidJUnit4::class)
class TasksListBindingsTest {

    private lateinit var textView: TextView
    private val today: LocalDate = LocalDate.now()

    @Before
    fun createTextView() {
        // A fresh TextView starts VISIBLE with empty text, like due_date_text in task_item.xml
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            textView = TextView(ApplicationProvider.getApplicationContext())
        }
    }

    private fun bind(task: Task?) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            setDueDateLabel(textView, task)
        }
    }

    private fun task(title: String, isCompleted: Boolean, dueDate: LocalDate?) =
        Task(title, "", isCompleted, dueDate = dueDate)

    private fun assertShown(expected: String) {
        assertEquals(View.VISIBLE, textView.visibility)
        assertEquals(expected, textView.text.toString())
    }

    private fun assertHidden() {
        assertEquals(View.GONE, textView.visibility)
    }

    // ---- Success cases ----

    @Test
    fun setDueDateLabel_activeDueToday_showsDDay() {
        // GIVEN - An active task due today
        val task = task("A", false, today)
        // WHEN - Bind it
        bind(task)
        // THEN - "D-Day" is shown
        assertShown("D-Day")
    }

    @Test
    fun setDueDateLabel_activeDueInThreeDays_showsDMinus3() {
        val task = task("A", false, today.plusDays(3))
        bind(task)
        assertShown("D-3")
    }

    @Test
    fun setDueDateLabel_activeOverdueByTwoDays_showsDPlus2() {
        val task = task("A", false, today.minusDays(2))
        bind(task)
        assertShown("D+2")
    }

    @Test
    fun setDueDateLabel_completedWithDueDate_showsClear() {
        val task = task("A", true, today.plusDays(3))
        bind(task)
        assertShown("CLEAR")
    }

    @Test
    fun setDueDateLabel_completedWithNoDueDate_showsClear() {
        val task = task("A", true, null)
        bind(task)
        assertShown("CLEAR")
    }

    @Test
    fun setDueDateLabel_activeWithNoDueDate_hidesLabel() {
        val task = task("A", false, null)
        bind(task)
        assertHidden()
    }

    @Test
    fun setDueDateLabel_nullTask_hidesLabel() {
        bind(null)
        assertHidden()
    }

    // ---- Edge cases: due-today boundary ----

    @Test
    fun setDueDateLabel_activeOverdueByOneDay_showsDPlus1() {
        bind(task("A", false, today.minusDays(1)))
        assertShown("D+1")
    }

    @Test
    fun setDueDateLabel_activeDueTomorrow_showsDMinus1() {
        bind(task("A", false, today.plusDays(1)))
        assertShown("D-1")
    }

    // ---- Edge cases: checking / unchecking the same task ----

    @Test
    fun setDueDateLabel_uncheckCompletedWithNoDueDate_hidesClear() {
        // GIVEN - The view shows "CLEAR" for a completed task with no due date
        bind(task("A", true, null))
        assertShown("CLEAR")
        // WHEN - The task is unchecked and rebound
        bind(task("A", false, null))
        // THEN - "CLEAR" is no longer shown
        assertHidden()
    }

    @Test
    fun setDueDateLabel_checkActiveWithDueDate_showsClear() {
        bind(task("A", false, today.plusDays(3)))
        assertShown("D-3")
        bind(task("A", true, today.plusDays(3)))
        assertShown("CLEAR")
    }

    @Test
    fun setDueDateLabel_uncheckCompletedWithDueDate_showsDMinus3() {
        bind(task("A", true, today.plusDays(3)))
        assertShown("CLEAR")
        bind(task("A", false, today.plusDays(3)))
        assertShown("D-3")
    }

    // ---- Edge cases: recycled rows ----

    @Test
    fun setDueDateLabel_reboundFromDueDateToNoDueDate_hidesLabel() {
        bind(task("A", false, today.plusDays(3)))
        assertShown("D-3")
        bind(task("B", false, null))
        assertHidden()
    }

    @Test
    fun setDueDateLabel_reboundToNullTask_hidesLabel() {
        bind(task("A", true, null))
        assertShown("CLEAR")
        bind(null)
        assertHidden()
    }

    @Test
    fun setDueDateLabel_reboundFromHiddenToDueDate_showsDMinus3() {
        bind(task("B", false, null))
        bind(task("A", false, today.plusDays(3)))
        assertShown("D-3")
    }
}
