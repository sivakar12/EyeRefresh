package com.sivakar.eyerefresh.models;

import java.io.Serializable;

public enum Action implements Serializable {
    START_SCHEDULING,
    PAUSE_SCHEDULING,
    TIME_UP,
    SNOOZE,
    ACT_ON_REMINDER,
    REFRESH_TIME_UP,
    STOP_REFRESH_MIDWAY,
    OPEN_APP,
    SEND_NOTIFICATION
}
