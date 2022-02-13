package com.sivakar.eyerefresh.models;

import java.io.Serializable;

public enum Event implements Serializable {
    SCHEDULING_TURNED_ON,
    SCHEDULING_PAUSED,
    REMINDER_DUE,
    SNOOZE_REQUESTED,
    REFRESH_STARTED,
    REFRESH_TIME_UP,
    REFRESH_COMPLETED,
    REFRESH_CANCELLED,
    OPEN_APP
}
