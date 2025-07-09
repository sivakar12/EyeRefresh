package com.sivakar.eyerefresh.history

import java.time.*
import java.time.temporal.WeekFields
import java.util.*

sealed class Range {
    data class Hour(val hour: Int, val day: Int, val month: Int, val year: Int) : Range()
    data class Day(val day: Int, val month: Int, val year: Int) : Range()
    data class Week(val weekNumber: Int, val month: Int, val year: Int) : Range()
    data class Month(val month: Int, val year: Int) : Range()
    data class Year(val year: Int) : Range()

    companion object {
        fun getStartTimestampAndEndTimestamp(range: Range): Pair<Long, Long> {
            return when (range) {
                is Hour -> {
                    val startOfHour = LocalDateTime.of(range.year, range.month, range.day, range.hour, 0, 0)
                    val endOfHour = LocalDateTime.of(range.year, range.month, range.day, range.hour, 59, 59)
                    Pair(
                        startOfHour.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        endOfHour.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    )
                }
                is Day -> {
                    val startOfDay = LocalDateTime.of(range.year, range.month, range.day, 0, 0, 0)
                    val endOfDay = LocalDateTime.of(range.year, range.month, range.day, 23, 59, 59)
                    Pair(
                        startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    )
                }
                is Week -> {
                    // Start with first day of the month
                    val firstDayOfMonth = LocalDate.of(range.year, range.month, 1)
                    
                    // Go back to find the first day of the first week of this month
                    val firstDayOfFirstWeek = firstDayOfMonth.minusDays(
                        firstDayOfMonth.dayOfWeek.value.toLong() - 1
                    )
                    
                    // Calculate the target week by adding (weekNumber - 1) * 7 days
                    val startOfTargetWeek = firstDayOfFirstWeek.plusDays(
                        (range.weekNumber - 1) * 7L
                    )
                    val endOfTargetWeek = startOfTargetWeek.plusDays(6)
                    
                    val startDateTime = startOfTargetWeek.atStartOfDay()
                    val endDateTime = endOfTargetWeek.atTime(23, 59, 59)
                    
                    Pair(
                        startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    )
                }
                is Month -> {
                    val startOfMonth = LocalDateTime.of(range.year, range.month, 1, 0, 0, 0)
                    val endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1)
                    Pair(
                        startOfMonth.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        endOfMonth.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    )
                }
                is Year -> {
                    val startOfYear = LocalDateTime.of(range.year, 1, 1, 0, 0, 0)
                    val endOfYear = LocalDateTime.of(range.year, 12, 31, 23, 59, 59)
                    Pair(
                        startOfYear.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        endOfYear.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    )
                }
            }
        }

        /**
         * Returns a list of subsection ranges for the given range.
         * Used for drawing graphs and charts.
         * 
         * @param range The range to get subsections for. Must not be an Hour range.
         * @return List of subsection ranges
         * @throws IllegalArgumentException if range is an Hour range
         */
        fun getSubsections(range: Range): List<Range> {
            require(range !is Hour) { "Hour ranges are not supported for grouping" }
            return when (range) {
                is Day -> {
                    // For a day, return all hours (0-23)
                    (0..23).map { hour ->
                        Hour(hour, range.day, range.month, range.year)
                    }
                }
                is Week -> {
                    // For a week, return all days of the week
                    // Start with first day of the month
                    val firstDayOfMonth = LocalDate.of(range.year, range.month, 1)
                    
                    // Go back to find the first day of the first week of this month
                    val firstDayOfFirstWeek = firstDayOfMonth.minusDays(
                        firstDayOfMonth.dayOfWeek.value.toLong() - 1
                    )
                    
                    // Calculate the target week by adding (weekNumber - 1) * 7 days
                    val startOfTargetWeek = firstDayOfFirstWeek.plusDays(
                        (range.weekNumber - 1) * 7L
                    )
                    
                    (0..6).map { dayOffset ->
                        val date = startOfTargetWeek.plusDays(dayOffset.toLong())
                        Day(date.dayOfMonth, date.monthValue, date.year)
                    }
                }
                is Month -> {
                    // For a month, return all days of the month
                    val yearMonth = YearMonth.of(range.year, range.month)
                    val daysInMonth = yearMonth.lengthOfMonth()
                    
                    (1..daysInMonth).map { day ->
                        Day(day, range.month, range.year)
                    }
                }
                is Year -> {
                    // For a year, return all months
                    (1..12).map { month ->
                        Month(month, range.year)
                    }
                }
                is Hour -> {
                    // This should never be reached due to the require statement above
                    // But we need this for the when expression to be exhaustive
                    emptyList()
                }
            }
        }
    }
}