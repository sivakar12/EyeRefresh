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
import com.sivakar.eyerefresh.EventHandlers;
import com.sivakar.eyerefresh.R;

public class RefreshHappeningStateFragment extends Fragment {
    private CountDownTimer timer;
    private TextView label;
    private View buttonGroup;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_refresh_happening_state, container, false);
        label = view.findViewById(R.id.state_label);
        buttonGroup = view.findViewById(R.id.group_action_buttons);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AsyncTask.execute(() -> {
            AppDatabase db = Room.databaseBuilder(getContext(), AppDatabase.class, "eye-refresh-db")
                    .enableMultiInstanceInvalidation()
                    .build();
            long startTimestamp = db.stateLogDao().getLastRefreshHappeningEntry().timestamp;
            long refreshDuration = EventHandlers.getRefreshDurationInMillis(getContext());
            long timeTillRefreshFinish = refreshDuration - (System.currentTimeMillis() - startTimestamp);
            getActivity().runOnUiThread(() -> {
                if (timeTillRefreshFinish < 0) {
                    buttonGroup.setVisibility(View.VISIBLE);
                } else {
                    this.timer = new CountDownTimer(timeTillRefreshFinish, 1000) {
                        @Override
                        public void onTick(long l) {
                            long secondsTillRefreshFinish = (refreshDuration -
                                    (System.currentTimeMillis() - startTimestamp)) / 1000;
                            label.setText("Look away for " + secondsTillRefreshFinish + " seconds");
                        }

                        @Override
                        public void onFinish() {
                            buttonGroup.setVisibility(View.VISIBLE);
                            label.setText("Refresh time up!");
                        }
                    };
                    this.timer.start();
                }
            });
        });
    }
    @Override
    public void onPause() {
        super.onPause();
        if (this.timer != null) {
            timer.cancel();
        }
    }
}