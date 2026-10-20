# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `@BindingAdapter("app:dueDateLabel") fun setDueDateLabel(textView: TextView, task: Task?)`

**Intent**: Show the task's D-day label (as built by `dueDateLabel` for the device's current date) in the list item's due-date `TextView`, and hide that view when there is no label, so a recycled list item never keeps a stale label from a previously bound task.

**Pre-conditions**
- Called on the main thread with a real `TextView` (e.g. `due_date_text` from `task_item.xml`, which starts `VISIBLE`).
- "Today" is the device's current date, `LocalDate.now()`; tests build due dates relative to it (e.g. `LocalDate.now().plusDays(3)`), so the expected strings below are exact regardless of the run date.

**Post-conditions**
- Success cases
  - An active task due today shows "D-Day" and the view is visible. `[Integration]`
    - Tip: Given a TextView and Task(title = "A", dueDate = LocalDate.now(), isCompleted = false) / When setDueDateLabel is called / Then text is "D-Day" and visibility is View.VISIBLE
  - An active task due in the future shows "D-N" and the view is visible. `[Integration]`
    - Tip: Given a TextView and Task(title = "A", dueDate = LocalDate.now().plusDays(3), isCompleted = false) / When setDueDateLabel is called / Then text is "D-3" and visibility is View.VISIBLE
  - An active overdue task shows "D+N" and the view is visible. `[Integration]`
    - Tip: Given a TextView and Task(title = "A", dueDate = LocalDate.now().minusDays(2), isCompleted = false) / When setDueDateLabel is called / Then text is "D+2" and visibility is View.VISIBLE
  - A completed task with a due date shows "CLEAR" and the view is visible. `[Integration]`
    - Tip: Given a TextView and Task(title = "A", dueDate = LocalDate.now().plusDays(3), isCompleted = true) / When setDueDateLabel is called / Then text is "CLEAR" and visibility is View.VISIBLE
  - A completed task with no due date (optional value left empty) shows "CLEAR" and the view is visible. `[Integration]`
    - Tip: Given a TextView and Task(title = "A", dueDate = null, isCompleted = true) / When setDueDateLabel is called / Then text is "CLEAR" and visibility is View.VISIBLE
  - An active task with no due date (optional value left empty) shows nothing: the text is empty and the view is gone. `[Integration]`
    - Tip: Given a TextView and Task(title = "A", dueDate = null, isCompleted = false) / When setDueDateLabel is called / Then text is "" and visibility is View.GONE
  - A null task (binding not yet set) shows nothing: the text is empty and the view is gone. `[Integration]`
    - Tip: Given a TextView and task = null / When setDueDateLabel is called / Then text is "" and visibility is View.GONE
- Error cases
  - None. `[Integration]`
    - Tip: Given a TextView and task = null / When setDueDateLabel is called / Then no exception is thrown
- Edge cases
  - Boundary around due today (N = 0). `[Integration]`
    - Tip: Given a TextView and Task(title = "A", dueDate = LocalDate.now().minusDays(1), isCompleted = false) / When setDueDateLabel is called / Then text is "D+1" and visibility is View.VISIBLE
    - Tip: Given a TextView and Task(title = "A", dueDate = LocalDate.now(), isCompleted = false) / When setDueDateLabel is called / Then text is "D-Day" and visibility is View.VISIBLE
    - Tip: Given a TextView and Task(title = "A", dueDate = LocalDate.now().plusDays(1), isCompleted = false) / When setDueDateLabel is called / Then text is "D-1" and visibility is View.VISIBLE
  - Boundary between a label and no label on a reused (recycled) view: a view that shows a label is rebound to an active task without a due date. `[Integration]`
    - Tip: Given a TextView already bound to Task(title = "A", dueDate = LocalDate.now().plusDays(3), isCompleted = false) showing "D-3" / When setDueDateLabel is called with Task(title = "B", dueDate = null, isCompleted = false) / Then text is "" and visibility is View.GONE
  - A view that shows a label is rebound to a null task. `[Integration]`
    - Tip: Given a TextView already bound to Task(title = "A", dueDate = null, isCompleted = true) showing "CLEAR" / When setDueDateLabel is called with task = null / Then text is "" and visibility is View.GONE
  - A hidden view is rebound to a task that has a label. `[Integration]`
    - Tip: Given a TextView already bound to Task(title = "B", dueDate = null, isCompleted = false) with visibility View.GONE / When setDueDateLabel is called with Task(title = "A", dueDate = LocalDate.now().plusDays(3), isCompleted = false) / Then text is "D-3" and visibility is View.VISIBLE
  - A completed/active toggle on the same task with no due date. `[Integration]`
    - Tip: Given a TextView already bound to Task(title = "A", dueDate = null, isCompleted = true) showing "CLEAR" / When setDueDateLabel is called with Task(title = "A", dueDate = null, isCompleted = false) / Then text is "" and visibility is View.GONE

**Undefined behavior** (optional)
- A test that runs across midnight may see `LocalDate.now()` change between building the task and the call; such timing is left undefined, and tests should avoid running at midnight.
