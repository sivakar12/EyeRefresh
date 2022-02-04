package com.sivakar.eyerefresh.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sivakar.eyerefresh.R;

public class ReminderSentStateFragment extends Fragment {


    public ReminderSentStateFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder_sent_state, container, false);
    }
}