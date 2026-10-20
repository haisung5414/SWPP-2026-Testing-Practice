# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `@BindingAdapter("app:dueDateLabel") fun setDueDateLabel(textView: TextView, task: Task?)`

**Intent**: Show the task's D-day label (the value of `dueDateLabel(task.dueDate, LocalDate.now(), task.isCompleted)`) in the list item's `due_date_text` view, and hide that view when the label is empty. The adapter is called again every time the same row is rebound (a recycled row, or the same task after its checkbox is checked or unchecked), so each call must fully decide the view's state from the current task: the PR requires that "the label should be updated when checking or unchecking a completed task", and a label from a previous binding must never stay on screen.

**Pre-conditions**
- Called on the main thread with a real `TextView`. In `task_item.xml`, `due_date_text` has no initial `visibility`, so it starts `View.VISIBLE` with empty text.
- "Today" is the device's current date, `LocalDate.now()`; tests build due dates relative to it (e.g. `LocalDate.now().plusDays(3)`), so the expected strings below are exact regardless of the run date.
- "Shown" means `visibility == View.VISIBLE` and `text` equals the label. "Hidden" means `visibility == View.GONE` (no space is reserved in the row); the text of a hidden view is not part of the contract.

**Post-conditions**
- Success cases
  - An active task due today shows "D-Day". `[Integration]`
    - Tip (`setDueDateLabel_activeDueToday_showsDDay`): Given a new TextView and Task("A", "", false, dueDate = LocalDate.now()) / When setDueDateLabel is called / Then text is "D-Day" and visibility is View.VISIBLE
  - An active task due in the future shows "D-N". `[Integration]`
    - Tip (`setDueDateLabel_activeDueInThreeDays_showsDMinus3`): Given a new TextView and Task("A", "", false, dueDate = LocalDate.now().plusDays(3)) / When setDueDateLabel is called / Then text is "D-3" and visibility is View.VISIBLE
  - An active overdue task shows "D+N". `[Integration]`
    - Tip (`setDueDateLabel_activeOverdueByTwoDays_showsDPlus2`): Given a new TextView and Task("A", "", false, dueDate = LocalDate.now().minusDays(2)) / When setDueDateLabel is called / Then text is "D+2" and visibility is View.VISIBLE
  - A completed task with a due date shows "CLEAR". `[Integration]`
    - Tip (`setDueDateLabel_completedWithDueDate_showsClear`): Given a new TextView and Task("A", "", true, dueDate = LocalDate.now().plusDays(3)) / When setDueDateLabel is called / Then text is "CLEAR" and visibility is View.VISIBLE
  - A completed task with no due date (optional value left empty) shows "CLEAR". `[Integration]`
    - Tip (`setDueDateLabel_completedWithNoDueDate_showsClear`): Given a new TextView and Task("A", "", true, dueDate = null) / When setDueDateLabel is called / Then text is "CLEAR" and visibility is View.VISIBLE
  - An active task with no due date (optional value left empty) shows no label. `[Integration]`
    - Tip (`setDueDateLabel_activeWithNoDueDate_hidesLabel`): Given a new TextView and Task("A", "", false, dueDate = null) / When setDueDateLabel is called / Then visibility is View.GONE
  - A null task (binding variable not set yet) shows no label. `[Integration]`
    - Tip (`setDueDateLabel_nullTask_hidesLabel`): Given a new TextView and task = null / When setDueDateLabel is called / Then visibility is View.GONE
- Error cases
  - None. Every `Task?` value, including `null`, is valid input.
- Edge cases
  - Due-today boundary (N = 0) and right next to it on both sides. `[Integration]`
    - Tip (`setDueDateLabel_activeOverdueByOneDay_showsDPlus1`): Given a new TextView and Task("A", "", false, dueDate = LocalDate.now().minusDays(1)) / When setDueDateLabel is called / Then text is "D+1" and visibility is View.VISIBLE
    - Tip (`setDueDateLabel_activeDueTomorrow_showsDMinus1`): Given a new TextView and Task("A", "", false, dueDate = LocalDate.now().plusDays(1)) / When setDueDateLabel is called / Then text is "D-1" and visibility is View.VISIBLE
  - Unchecking a completed task with no due date (label → no label on the same view): "CLEAR" must disappear. `[Integration]`
    - Tip (`setDueDateLabel_uncheckCompletedWithNoDueDate_hidesClear`): Given a TextView on which setDueDateLabel was called with Task("A", "", true, dueDate = null) so it shows "CLEAR" / When setDueDateLabel is called again with Task("A", "", false, dueDate = null) / Then visibility is View.GONE
  - Checking and unchecking a task with a due date (label → different label on the same view). `[Integration]`
    - Tip (`setDueDateLabel_checkActiveWithDueDate_showsClear`): Given a TextView on which setDueDateLabel was called with Task("A", "", false, dueDate = LocalDate.now().plusDays(3)) so it shows "D-3" / When setDueDateLabel is called again with Task("A", "", true, dueDate = LocalDate.now().plusDays(3)) / Then text is "CLEAR" and visibility is View.VISIBLE
    - Tip (`setDueDateLabel_uncheckCompletedWithDueDate_showsDMinus3`): Given a TextView on which setDueDateLabel was called with Task("A", "", true, dueDate = LocalDate.now().plusDays(3)) so it shows "CLEAR" / When setDueDateLabel is called again with Task("A", "", false, dueDate = LocalDate.now().plusDays(3)) / Then text is "D-3" and visibility is View.VISIBLE
  - Recycled row: a view showing a label is rebound to a task with no label, or to null. `[Integration]`
    - Tip (`setDueDateLabel_reboundFromDueDateToNoDueDate_hidesLabel`): Given a TextView on which setDueDateLabel was called with Task("A", "", false, dueDate = LocalDate.now().plusDays(3)) so it shows "D-3" / When setDueDateLabel is called again with Task("B", "", false, dueDate = null) / Then visibility is View.GONE
    - Tip (`setDueDateLabel_reboundToNullTask_hidesLabel`): Given a TextView on which setDueDateLabel was called with Task("A", "", true, dueDate = null) so it shows "CLEAR" / When setDueDateLabel is called again with task = null / Then visibility is View.GONE
  - Recycled row: a hidden view is rebound to a task that has a label (no label → label). `[Integration]`
    - Tip (`setDueDateLabel_reboundFromHiddenToDueDate_showsDMinus3`): Given a TextView on which setDueDateLabel was called with Task("B", "", false, dueDate = null) / When setDueDateLabel is called again with Task("A", "", false, dueDate = LocalDate.now().plusDays(3)) / Then text is "D-3" and visibility is View.VISIBLE
  - End-to-end in the real task list: the label follows the checkbox through data binding, RecyclerView and the repository. `[Integration]`
    - Tip (`tasksList_checkThenUncheckTaskWithNoDueDate_clearAppearsThenDisappears`): Given the repository holds only Task("TITLE1", "DESC1", false, "id1", dueDate = null) and TasksFragment is launched / When the row's complete_checkbox is clicked, then clicked again / Then after the first click the row's due_date_text is displayed with text "CLEAR", and after the second click the row's due_date_text is not displayed
    - Tip (`tasksList_activeTaskWithDueDateInThreeDays_showsDMinus3`): Given the repository holds only Task("TITLE1", "DESC1", false, "id1", dueDate = LocalDate.now().plusDays(3)) and TasksFragment is launched / When the list is shown / Then the row's due_date_text is displayed with text "D-3"

**Undefined behavior** (optional)
- A test that runs across midnight may see `LocalDate.now()` change between building the task and the call; such timing is left undefined, and tests should avoid running at midnight.
