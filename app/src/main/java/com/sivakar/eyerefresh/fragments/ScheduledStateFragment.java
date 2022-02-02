package com.sivakar.eyerefresh.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sivakar.eyerefresh.MainActivity;
import com.sivakar.eyerefresh.R;
import com.sivakar.eyerefresh.models.State;

public class ScheduledStateFragment extends Fragment {


    public ScheduledStateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scheduled_state, container, false);
    }
}