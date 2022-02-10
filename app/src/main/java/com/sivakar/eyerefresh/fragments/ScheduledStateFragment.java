package com.sivakar.eyerefresh.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sivakar.eyerefresh.AppDatabase;
import com.sivakar.eyerefresh.MainActivity;
import com.sivakar.eyerefresh.R;
import com.sivakar.eyerefresh.models.State;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScheduledStateFragment extends Fragment {

    private long reminderTimestamp;
    private CountDownTimer timer;
    public ScheduledStateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private void updateTimeRemainingInView() {
        long currentTime = System.currentTimeMillis();
        long secondsRemaining = (reminderTimestamp - currentTime) / 1000;
        String displayTime = String.format("%02d:%02d", secondsRemaining / 60, secondsRemaining % 60);
        TextView scheduledTimeView = (TextView) getView().findViewById(R.id.state_label);
        scheduledTimeView.setText("Next reminder in " + displayTime);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView =  inflater.inflate(R.layout.fragment_scheduled_state, container, false);

        AsyncTask.execute(() -> {
            AppDatabase db = Room.databaseBuilder(getContext(), AppDatabase.class, "eye-refresh-db")
                    .enableMultiInstanceInvalidation()
                    .build();
            reminderTimestamp = db.stateLogDao().getLatestLog().reminderTimestamp;
            getActivity().runOnUiThread(() -> {
                this.timer = new CountDownTimer(reminderTimestamp - System.currentTimeMillis(),1000) {
                    @Override
                    public void onTick(long l) {
                        updateTimeRemainingInView();
                    }

                    @Override
                    public void onFinish() {

                    }
                };
                this.timer.start();
            });

        });
        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.timer != null) {
            this.timer.start();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (this.timer != null) {
            timer.cancel();
        }
    }
}