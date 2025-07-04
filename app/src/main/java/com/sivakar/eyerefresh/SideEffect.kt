package com.sivakar.eyerefresh

data class NotificationOption(
    val label: String,
    val eventToSend: AppEvent
)
sealed class SideEffect {
    data class ScheduleEvent(val event: AppEvent, val timeInMillis: Long) : SideEffect()
    data class ShowNotification(val title: String, val text: String, notificationOptions: NotificationOption[])
}