package com.sivakar.eyerefresh.models;

import java.io.Serializable;


// TODO: Somet of these need to be cleaned up
public enum Action implements Serializable {
    START_SCHEDULING,
    PAUSE_SCHEDULING,
    TIME_UP,
    SNOOZE,
    ACT_ON_REMINDER,
    REFRESH_TIME_UP,
    STOP_REFRESH_MIDWAY,
    OPEN_APP,
    SEND_NOTIFICATION,
    REFRESH_DONE,
    SEND_REFRESH_TIME_UP_NOTIFICATION
}
