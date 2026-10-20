package com.example.swpp.tododapp.tasks

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.swpp.tododapp.R
import com.example.swpp.tododapp.ServiceLocator
import com.example.swpp.tododapp.data.Task
import com.example.swpp.tododapp.data.source.FakeAndroidTestRepository
import com.example.swpp.tododapp.data.source.TasksRepository
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * List-level Tips in specs/TasksListBindings.spec.md: the label follows the checkbox
 * through data binding, the RecyclerView and the repository.
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
class TasksFragmentDueDateTest {

    private lateinit var repository: TasksRepository

    @Before
    fun initRepository() {
        repository = FakeAndroidTestRepository()
        ServiceLocator.tasksRepository = repository
    }

    @After
    fun cleanupDb() = runTest {
        ServiceLocator.resetRepository()
    }

    private fun rowView(id: Int, title: String) = onView(allOf(withId(id), hasSibling(withText(title))))

    @Test
    fun tasksList_checkThenUncheckTaskWithNoDueDate_clearAppearsThenDisappears() = runTest {
        // GIVEN - One active task with no due date, shown in the task list
        repository.saveTask(Task("TITLE1", "DESC1", false, "id1", dueDate = null))
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)

        // WHEN - Check the task
        rowView(R.id.complete_checkbox, "TITLE1").perform(click())

        // THEN - "CLEAR" is shown
        rowView(R.id.due_date_text, "TITLE1").check(matches(isDisplayed()))
        rowView(R.id.due_date_text, "TITLE1").check(matches(withText("CLEAR")))

        // WHEN - Uncheck the task
        rowView(R.id.complete_checkbox, "TITLE1").perform(click())

        // THEN - No label is shown any more
        rowView(R.id.due_date_text, "TITLE1").check(matches(not(isDisplayed())))
    }

    @Test
    fun tasksList_activeTaskWithDueDateInThreeDays_showsDMinus3() = runTest {
        // GIVEN - One active task due in 3 days
        repository.saveTask(Task("TITLE1", "DESC1", false, "id1", dueDate = LocalDate.now().plusDays(3)))

        // WHEN - The task list is shown
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)

        // THEN - "D-3" is shown in the row
        rowView(R.id.due_date_text, "TITLE1").check(matches(isDisplayed()))
        rowView(R.id.due_date_text, "TITLE1").check(matches(withText("D-3")))
    }
}
