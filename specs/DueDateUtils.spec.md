# Spec: `DueDateUtils.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `internal fun dueDateLabel(dueDate: LocalDate?, today: LocalDate, isCompleted: Boolean): String`

**Intent**: Build the D-day label shown next to a task in the task list (PR "Add due date feature"). A completed task shows "CLEAR" whether or not it has a due date; an active task with no due date shows no label (""); an active task with a due date shows "D-Day" when it is due today, "D-N" when it is due in N days, and "D+N" when it is overdue by N days. N is the **total** number of calendar days between `today` and `dueDate` (the same value as `ChronoUnit.DAYS.between(today, dueDate)`, sign removed), never only the day-of-month part of the difference, so it stays correct across month ends, year ends and leap days.

**Pre-conditions**
- `today` is a non-null `LocalDate`; `dueDate` is a `LocalDate` or `null`.
- The function is pure: the result depends only on its three arguments (it never reads the system clock).
- Label format is exact: uppercase "D", an ASCII hyphen-minus `-` or plus `+`, then N in decimal with no sign, no zero padding and no spaces (e.g. "D-5", "D-66", "D+365"); "D-Day" and "CLEAR" are written exactly with this capitalization.

**Post-conditions**
- Success cases
  - A completed task with a future due date returns "CLEAR". `[Unit]`
    - Tip (`dueDateLabel_completedWithFutureDueDate_returnsClear`): Given dueDate = 2026-10-25, today = 2026-10-20, isCompleted = true / When dueDateLabel is called / Then it returns "CLEAR"
  - A completed task with an overdue due date returns "CLEAR". `[Unit]`
    - Tip (`dueDateLabel_completedWithOverdueDueDate_returnsClear`): Given dueDate = 2026-10-17, today = 2026-10-20, isCompleted = true / When dueDateLabel is called / Then it returns "CLEAR"
  - A completed task with no due date (optional value left empty) returns "CLEAR". `[Unit]`
    - Tip (`dueDateLabel_completedWithNoDueDate_returnsClear`): Given dueDate = null, today = 2026-10-20, isCompleted = true / When dueDateLabel is called / Then it returns "CLEAR"
  - An active task with no due date (optional value left empty) returns "". `[Unit]`
    - Tip (`dueDateLabel_activeWithNoDueDate_returnsEmpty`): Given dueDate = null, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns ""
  - An active task due later in the same month returns "D-N". `[Unit]`
    - Tip (`dueDateLabel_activeDueInFiveDays_returnsDMinus5`): Given dueDate = 2026-10-25, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-5"
  - An active task overdue within the same month returns "D+N". `[Unit]`
    - Tip (`dueDateLabel_activeOverdueByThreeDays_returnsDPlus3`): Given dueDate = 2026-10-17, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D+3"
  - An active task due several months ahead counts every calendar day in between (11 + 30 + 25 = 66). `[Unit]`
    - Tip (`dueDateLabel_activeDueInTwoMonths_returnsDMinus66`): Given dueDate = 2026-12-25, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-66"
  - An active task overdue by exactly one non-leap year counts every calendar day in between. `[Unit]`
    - Tip (`dueDateLabel_activeOverdueByOneYear_returnsDPlus365`): Given dueDate = 2025-10-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D+365"
- Error cases
  - None. Every combination of a non-null `today`, a nullable `dueDate` and a `Boolean` is valid input.
- Edge cases
  - Due-today boundary (N = 0) and right next to it on both sides. `[Unit]`
    - Tip (`dueDateLabel_activeOverdueByOneDay_returnsDPlus1`): Given dueDate = 2026-10-19, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D+1"
    - Tip (`dueDateLabel_activeDueToday_returnsDDay`): Given dueDate = 2026-10-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-Day"
    - Tip (`dueDateLabel_activeDueTomorrow_returnsDMinus1`): Given dueDate = 2026-10-21, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-1"
  - Completed/active boundary on the same date: completion wins over "D-Day". `[Unit]`
    - Tip (`dueDateLabel_completedDueToday_returnsClear`): Given dueDate = 2026-10-20, today = 2026-10-20, isCompleted = true / When dueDateLabel is called / Then it returns "CLEAR"
  - Day-of-month boundary: the dates are whole months apart, so the day-of-month difference is 0 but the total day count is not (must not return "D-Day"). `[Unit]`
    - Tip (`dueDateLabel_activeDueInThirtyDays_returnsDMinus30`): Given dueDate = 2026-11-19, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-30"
    - Tip (`dueDateLabel_activeDueInOneMonth_returnsDMinus31`): Given dueDate = 2026-11-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-31"
    - Tip (`dueDateLabel_activeDueInThirtyTwoDays_returnsDMinus32`): Given dueDate = 2026-11-21, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-32"
    - Tip (`dueDateLabel_activeOverdueByOneMonth_returnsDPlus30`): Given dueDate = 2026-09-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D+30"
  - Month end: `today` is the last day of a month and the due date is right before / right after it. `[Unit]`
    - Tip (`dueDateLabel_monthEndOverdueByOneDay_returnsDPlus1`): Given dueDate = 2026-10-30, today = 2026-10-31, isCompleted = false / When dueDateLabel is called / Then it returns "D+1"
    - Tip (`dueDateLabel_monthEndDueNextMonthFirstDay_returnsDMinus1`): Given dueDate = 2026-11-01, today = 2026-10-31, isCompleted = false / When dueDateLabel is called / Then it returns "D-1"
  - Year end: `dueDate` and `today` are in different years one day apart, in both directions. `[Unit]`
    - Tip (`dueDateLabel_yearEndDueNewYearsDay_returnsDMinus1`): Given dueDate = 2027-01-01, today = 2026-12-31, isCompleted = false / When dueDateLabel is called / Then it returns "D-1"
    - Tip (`dueDateLabel_newYearsDayOverdueFromLastYear_returnsDPlus1`): Given dueDate = 2026-12-31, today = 2027-01-01, isCompleted = false / When dueDateLabel is called / Then it returns "D+1"
  - Leap day: February 29 in a leap year counts as a day; a non-leap February goes straight to March 1. `[Unit]`
    - Tip (`dueDateLabel_leapYearDueLeapDay_returnsDMinus1`): Given dueDate = 2028-02-29, today = 2028-02-28, isCompleted = false / When dueDateLabel is called / Then it returns "D-1"
    - Tip (`dueDateLabel_leapYearDueMarchFirst_returnsDMinus2`): Given dueDate = 2028-03-01, today = 2028-02-28, isCompleted = false / When dueDateLabel is called / Then it returns "D-2"
    - Tip (`dueDateLabel_leapDayDueMarchFirst_returnsDMinus1`): Given dueDate = 2028-03-01, today = 2028-02-29, isCompleted = false / When dueDateLabel is called / Then it returns "D-1"
    - Tip (`dueDateLabel_nonLeapYearDueMarchFirst_returnsDMinus1`): Given dueDate = 2027-03-01, today = 2027-02-28, isCompleted = false / When dueDateLabel is called / Then it returns "D-1"
  - One year apart: right before / on / right after the anniversary, and a year that contains a leap day. `[Unit]`
    - Tip (`dueDateLabel_activeDueInOneYearMinusOneDay_returnsDMinus364`): Given dueDate = 2027-10-19, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-364"
    - Tip (`dueDateLabel_activeDueInOneYear_returnsDMinus365`): Given dueDate = 2027-10-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-365"
    - Tip (`dueDateLabel_activeDueInOneYearPlusOneDay_returnsDMinus366`): Given dueDate = 2027-10-21, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns "D-366"
    - Tip (`dueDateLabel_activeDueInOneYearOverLeapDay_returnsDMinus366`): Given dueDate = 2029-02-28, today = 2028-02-28, isCompleted = false / When dueDateLabel is called / Then it returns "D-366"

**Undefined behavior** (optional)
- None: every valid input has a defined label.
