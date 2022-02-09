package com.sivakar.eyerefresh.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.room.Room;

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


    public ScheduledStateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView =  inflater.inflate(R.layout.fragment_scheduled_state, container, false);

        AsyncTask.execute(() -> {
            AppDatabase db = Room.databaseBuilder(getContext(), AppDatabase.class, "eye-refresh-db")
                    .enableMultiInstanceInvalidation()
                    .build();
            long reminderTimestamp = db.stateLogDao().getLatestLog().reminderTimestamp;
            Format format = new SimpleDateFormat("HH:mm:ss"); // TODO: Have to be refactored
            String timeString = format.format(new Date(reminderTimestamp));
            TextView scheduledTimeView = (TextView) inflatedView.findViewById(R.id.state_label);
            scheduledTimeView.setText("Next reminder at " + timeString);
        });
        return inflatedView;
    }
}