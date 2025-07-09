package com.sivakar.eyerefresh.history

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sivakar.eyerefresh.database.AppDatabase
import com.sivakar.eyerefresh.history.CompletedSession
import com.sivakar.eyerefresh.history.Range
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import com.sivakar.eyerefresh.database.EventEntity

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val eventDao = database.eventDao()
    
    // Range selection state - always has a value, defaults to current day
    private val _selectedRange = MutableStateFlow<Range>(getCurrentDayRange())
    val selectedRange: StateFlow<Range> = _selectedRange
    
    val completedSessions: Flow<List<CompletedSession>> = selectedRange.flatMapLatest { range ->
        // Get events within the selected range
        val (startTimestamp, endTimestamp) = Range.getStartTimestampAndEndTimestamp(range)
        eventDao.getEventsInRange(startTimestamp, endTimestamp).map { events ->
            getSessionsFromEvents(events)
        }
    }
    
    private fun getSessionsFromEvents(allEvents: List<EventEntity>): List<CompletedSession> {
        Log.d("HistoryViewModel", "Processing ${allEvents.size} events")
        
        val sessions = mutableListOf<CompletedSession>()
        
        // Separate start and completed events
        val startedEvents = allEvents.filter { it.event == "RefreshStarted" }
        val completedEvents = allEvents.filter { it.event == "MarkRefreshCompleted" }
        
        Log.d("HistoryViewModel", "Started events: ${startedEvents.size}, Completed events: ${completedEvents.size}")
        
        // Create a map of start events by timestamp for quick lookup
        val startEventsMap = startedEvents.associateBy { it.timestamp }
        
        // For each completed event, find the most recent start event before it
        completedEvents.forEach { completedEvent ->
            Log.d("HistoryViewModel", "Processing completed event at ${completedEvent.timestamp}")
            
            val matchingStartEvent = startEventsMap.entries
                .filter { it.key < completedEvent.timestamp }
                .maxByOrNull { it.key }
            
            matchingStartEvent?.let { (startTime, startEvent) ->
                Log.d("HistoryViewModel", "Found matching start event at $startTime")
                
                // Check if there's no other start event between this start and complete
                val hasIntermediateStart = startEventsMap.any { (timestamp, _) ->
                    timestamp > startTime && timestamp < completedEvent.timestamp
                }
                
                if (!hasIntermediateStart) {
                    val session = CompletedSession(
                        sessionId = startEvent.id,
                        startTime = startTime,
                        completeTime = completedEvent.timestamp,
                        durationMs = completedEvent.timestamp - startTime
                    )
                    sessions.add(session)
                    Log.d("HistoryViewModel", "Added session: $session")
                } else {
                    Log.d("HistoryViewModel", "Skipped session due to intermediate start event")
                }
            } ?: Log.d("HistoryViewModel", "No matching start event found for completed event")
        }
        
        val sortedSessions = sessions.sortedByDescending { it.startTime }
        Log.d("HistoryViewModel", "Final sessions count: ${sortedSessions.size}")
        return sortedSessions
    }
    
    private fun filterSessionsByRange(sessions: List<CompletedSession>, range: Range): List<CompletedSession> {
        val (startTimestamp, endTimestamp) = Range.getStartTimestampAndEndTimestamp(range)
        return sessions.filter { session ->
            session.startTime >= startTimestamp && session.startTime <= endTimestamp
        }
    }
    
    private fun getCurrentDayRange(): Range.Day {
        val now = LocalDateTime.now()
        return Range.Day(now.dayOfMonth, now.monthValue, now.year)
    }
    
    // Range selection functions
    fun selectRange(range: Range) {
        _selectedRange.value = range
    }
    
    fun navigateToPreviousRange() {
        val currentRange = _selectedRange.value
        val newRange = when (currentRange) {
            is Range.Day -> {
                val currentDate = LocalDate.of(currentRange.year, currentRange.month, currentRange.day)
                val previousDate = currentDate.minusDays(1)
                Range.Day(previousDate.dayOfMonth, previousDate.monthValue, previousDate.year)
            }
            is Range.Week -> {
                val firstDayOfMonth = LocalDate.of(currentRange.year, currentRange.month, 1)
                val firstDayOfFirstWeek = firstDayOfMonth.minusDays(firstDayOfMonth.dayOfWeek.value.toLong() - 1)
                val startOfCurrentWeek = firstDayOfFirstWeek.plusDays((currentRange.weekNumber - 1) * 7L)
                val startOfPreviousWeek = startOfCurrentWeek.minusDays(7)
                
                val previousWeekNumber = if (currentRange.weekNumber > 1) {
                    currentRange.weekNumber - 1
                } else {
                    val previousMonth = YearMonth.of(currentRange.year, currentRange.month).minusMonths(1)
                    val daysInPreviousMonth = previousMonth.lengthOfMonth()
                    val lastDayOfPreviousMonth = LocalDate.of(previousMonth.year, previousMonth.month, daysInPreviousMonth)
                    val weekOfYear = lastDayOfPreviousMonth.get(java.time.temporal.WeekFields.ISO.weekOfYear())
                    val weekOfMonth = ((weekOfYear - LocalDate.of(previousMonth.year, previousMonth.month, 1)
                        .get(java.time.temporal.WeekFields.ISO.weekOfYear())) + 1).toInt()
                    weekOfMonth
                }
                
                val previousMonth = if (currentRange.weekNumber > 1) currentRange.month else {
                    if (currentRange.month > 1) currentRange.month - 1 else 12
                }
                val previousYear = if (currentRange.month > 1 || currentRange.weekNumber > 1) currentRange.year else currentRange.year - 1
                
                Range.Week(previousWeekNumber, previousMonth, previousYear)
            }
            is Range.Month -> {
                val currentYearMonth = YearMonth.of(currentRange.year, currentRange.month)
                val previousYearMonth = currentYearMonth.minusMonths(1)
                Range.Month(previousYearMonth.monthValue, previousYearMonth.year)
            }
            is Range.Year -> {
                Range.Year(currentRange.year - 1)
            }
            else -> currentRange
        }
        _selectedRange.value = newRange
    }
    
    fun navigateToNextRange() {
        val currentRange = _selectedRange.value
        val newRange = when (currentRange) {
            is Range.Day -> {
                val currentDate = LocalDate.of(currentRange.year, currentRange.month, currentRange.day)
                val nextDate = currentDate.plusDays(1)
                Range.Day(nextDate.dayOfMonth, nextDate.monthValue, nextDate.year)
            }
            is Range.Week -> {
                val firstDayOfMonth = LocalDate.of(currentRange.year, currentRange.month, 1)
                val firstDayOfFirstWeek = firstDayOfMonth.minusDays(firstDayOfMonth.dayOfWeek.value.toLong() - 1)
                val startOfCurrentWeek = firstDayOfFirstWeek.plusDays((currentRange.weekNumber - 1) * 7L)
                val startOfNextWeek = startOfCurrentWeek.plusDays(7)
                
                val nextWeekNumber = if (startOfNextWeek.monthValue == currentRange.month) {
                    currentRange.weekNumber + 1
                } else {
                    1
                }
                
                val nextMonth = if (startOfNextWeek.monthValue == currentRange.month) currentRange.month else {
                    if (currentRange.month < 12) currentRange.month + 1 else 1
                }
                val nextYear = if (currentRange.month < 12 || startOfNextWeek.monthValue == currentRange.month) currentRange.year else currentRange.year + 1
                
                Range.Week(nextWeekNumber, nextMonth, nextYear)
            }
            is Range.Month -> {
                val currentYearMonth = YearMonth.of(currentRange.year, currentRange.month)
                val nextYearMonth = currentYearMonth.plusMonths(1)
                Range.Month(nextYearMonth.monthValue, nextYearMonth.year)
            }
            is Range.Year -> {
                Range.Year(currentRange.year + 1)
            }
            else -> currentRange
        }
        _selectedRange.value = newRange
    }
} 