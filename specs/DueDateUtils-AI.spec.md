# Spec: `DueDateUtils.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `internal fun dueDateLabel(dueDate: LocalDate?, today: LocalDate, isCompleted: Boolean): String`

**Intent**: Build the D-day label shown next to a task in the list: "CLEAR" for a completed task, nothing for an active task without a due date, and otherwise "D-Day", "D-N" (due in N days) or "D+N" (overdue by N days), where N is the total number of calendar days between `today` and `dueDate`, correct across month ends, year ends and leap days.

**Pre-conditions**
- `today` is a valid `LocalDate` (non-null); `dueDate` is a valid `LocalDate` or `null`.
- The function is pure: the result depends only on its three arguments.

**Post-conditions**
- Success cases
  - A completed task with a future due date returns "CLEAR". `[Unit]`
    - Tip: Given dueDate = 2026-10-25, today = 2026-10-20, isCompleted = true / When dueDateLabel is called / Then it returns "CLEAR"
  - A completed task with an overdue due date returns "CLEAR". `[Unit]`
    - Tip: Given dueDate = 2026-10-17, today = 2026-10-20, isCompleted = true / When dueDateLabel is called / Then it returns "CLEAR"
  - A completed task with no due date (optional value left empty) returns "CLEAR". `[Unit]`
    - Tip: Given dueDate = null, today = 2026-10-20, isCompleted = true / When dueDateLabel is called / Then it returns "CLEAR"
  - An active task with no due date (optional value left empty) returns "" (empty string). `[Unit]`
    - Tip: Given dueDate = null, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns ""
  - An active task due today returns "D-Day". `[Unit]`
    - Tip: Given dueDate = 2026-10-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-Day"
  - An active task due within the same month returns "D-N". `[Unit]`
    - Tip: Given dueDate = 2026-10-25, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-5"
  - An active task overdue within the same month returns "D+N". `[Unit]`
    - Tip: Given dueDate = 2026-10-17, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D+3"
  - An active task due more than a month ahead counts every calendar day in between. `[Unit]`
    - Tip: Given dueDate = 2026-12-25, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-66"
  - An active task overdue by more than a year counts every calendar day in between. `[Unit]`
    - Tip: Given dueDate = 2025-10-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D+365"
- Error cases
  - None. `[Unit]`
    - Tip: Given any non-null today, any dueDate (including null) and any isCompleted / When dueDateLabel is called / Then it returns a label without throwing
- Edge cases
  - Boundary between overdue, due today and due later (N = 0). `[Unit]`
    - Tip: Given dueDate = 2026-10-19, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D+1"
    - Tip: Given dueDate = 2026-10-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-Day"
    - Tip: Given dueDate = 2026-10-21, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-1"
  - Boundary between completed and active for the same dates. `[Unit]`
    - Tip: Given dueDate = 2026-10-20, today = 2026-10-20, isCompleted = true / When dueDateLabel is called / Then it returns "CLEAR"
    - Tip: Given dueDate = 2026-10-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-Day"
  - Boundary between null and non-null due date for an active task. `[Unit]`
    - Tip: Given dueDate = null, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns ""
    - Tip: Given dueDate = 2026-10-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-Day"
  - Month end (today on the last day of a month). `[Unit]`
    - Tip: Given dueDate = 2026-10-30, today = 2026-10-31, isCompleted = false / When dueDateLabel is called / Then it returns "D+1"
    - Tip: Given dueDate = 2026-10-31, today = 2026-10-31, isCompleted = false / When dueDateLabel is called / Then it returns "D-Day"
    - Tip: Given dueDate = 2026-11-01, today = 2026-10-31, isCompleted = false / When dueDateLabel is called / Then it returns "D-1"
  - One full calendar month apart (the day count must not reset to the day-of-month difference). `[Unit]`
    - Tip: Given dueDate = 2026-11-19, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-30"
    - Tip: Given dueDate = 2026-11-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-31"
    - Tip: Given dueDate = 2026-11-21, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-32"
    - Tip: Given dueDate = 2026-09-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D+30"
  - Year end. `[Unit]`
    - Tip: Given dueDate = 2026-12-30, today = 2026-12-31, isCompleted = false / When dueDateLabel is called / Then it returns "D+1"
    - Tip: Given dueDate = 2027-01-01, today = 2026-12-31, isCompleted = false / When dueDateLabel is called / Then it returns "D-1"
    - Tip: Given dueDate = 2026-12-31, today = 2027-01-01, isCompleted = false / When dueDateLabel is called / Then it returns "D+1"
    - Tip: Given dueDate = 2027-01-02, today = 2027-01-01, isCompleted = false / When dueDateLabel is called / Then it returns "D-1"
  - Leap day (February in a leap year vs. a non-leap year). `[Unit]`
    - Tip: Given dueDate = 2028-02-29, today = 2028-02-28, isCompleted = false / When dueDateLabel is called / Then it returns "D-1"
    - Tip: Given dueDate = 2028-03-01, today = 2028-02-28, isCompleted = false / When dueDateLabel is called / Then it returns "D-2"
    - Tip: Given dueDate = 2028-03-01, today = 2028-02-29, isCompleted = false / When dueDateLabel is called / Then it returns "D-1"
    - Tip: Given dueDate = 2028-02-28, today = 2028-02-29, isCompleted = false / When dueDateLabel is called / Then it returns "D+1"
    - Tip: Given dueDate = 2027-03-01, today = 2027-02-28, isCompleted = false / When dueDateLabel is called / Then it returns "D-1"
  - One full year apart, with and without a leap day in between. `[Unit]`
    - Tip: Given dueDate = 2027-10-19, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-364"
    - Tip: Given dueDate = 2027-10-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-365"
    - Tip: Given dueDate = 2027-10-21, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-366"
    - Tip: Given dueDate = 2029-02-28, today = 2028-02-28, isCompleted = false / When dueDateLabel is called / Then it returns "D-366"

**Undefined behavior** (optional)
- None: every combination of a non-null `today`, a nullable `dueDate` and a `Boolean` has a defined label.
