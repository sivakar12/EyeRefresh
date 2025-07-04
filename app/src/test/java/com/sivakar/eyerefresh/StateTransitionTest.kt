package com.sivakar.eyerefresh

import org.junit.Test
import org.junit.Assert.*

class StateTransitionTest {
    
    @Test
    fun testPausedToScheduledTransition() {
        val currentState = AppState.RemindersPaused
        val event = AppEvent.NotificationsTurnedOn
        val transition = transition(currentState, event)
        
        assertTrue(transition.newState is AppState.ReminderScheduled)
        assertTrue(transition.sideEffect is SideEffect.ScheduleEvent)
        
        val scheduledState = transition.newState as AppState.ReminderScheduled
        assertTrue(scheduledState.timeInMillis > System.currentTimeMillis())
        
        val sideEffect = transition.sideEffect as SideEffect.ScheduleEvent
        assertTrue(sideEffect.event is AppEvent.NotificationDue)
    }
    
    @Test
    fun testScheduledToPausedTransition() {
        val currentState = AppState.ReminderScheduled(System.currentTimeMillis() + 60000L)
        val event = AppEvent.NotificationsPaused
        val transition = transition(currentState, event)
        
        assertTrue(transition.newState is AppState.RemindersPaused)
        assertTrue(transition.sideEffect == null)
    }
    
    @Test
    fun testScheduledToSentTransition() {
        val currentState = AppState.ReminderScheduled(System.currentTimeMillis() + 60000L)
        val event = AppEvent.NotificationDue
        val transition = transition(currentState, event)
        
        assertTrue(transition.newState is AppState.ReminderSent)
        assertTrue(transition.sideEffect is SideEffect.ShowNotification)
        
        val sideEffect = transition.sideEffect as SideEffect.ShowNotification
        assertEquals("Time for Eye Refresh!", sideEffect.title)
        assertEquals(3, sideEffect.notificationOptions.size)
    }
    
    @Test
    fun testSentToRefreshTransition() {
        val currentState = AppState.ReminderSent
        val event = AppEvent.RefreshStarted
        val transition = transition(currentState, event)
        
        assertTrue(transition.newState is AppState.RefreshHappening)
        assertTrue(transition.sideEffect is SideEffect.ScheduleEvent)
        
        val sideEffect = transition.sideEffect as SideEffect.ScheduleEvent
        assertTrue(sideEffect.event is AppEvent.RefreshTimeDone)
    }
    
    @Test
    fun testRefreshToScheduledTransition() {
        val currentState = AppState.RefreshHappening
        val event = AppEvent.RefreshMarkedComplete
        val transition = transition(currentState, event)
        
        assertTrue(transition.newState is AppState.ReminderScheduled)
        assertTrue(transition.sideEffect is SideEffect.ScheduleEvent)
        
        val sideEffect = transition.sideEffect as SideEffect.ScheduleEvent
        assertTrue(sideEffect.event is AppEvent.NotificationDue)
    }
} 