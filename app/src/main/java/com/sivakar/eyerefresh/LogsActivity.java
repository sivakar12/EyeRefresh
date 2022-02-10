package com.sivakar.eyerefresh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sivakar.eyerefresh.models.StateLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LogsActivity extends AppCompatActivity {

    public class LogsListAdapter extends RecyclerView.Adapter<LogsListAdapter.ViewHolder> {

        private List<StateLog> logs;
        public LogsListAdapter(List<StateLog> logs) {
            this.logs = logs;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StateLog log = logs.get(position);
            String dateFormat = (new SimpleDateFormat("HH:mm:ss dd/MM/yyyy"))
                    .format(new Date(log.timestamp));
            holder.getTime().setText(dateFormat);
            holder.getState().setText(log.state.name());
        }

        @Override
        public int getItemCount() {
            return this.logs.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView time;
            private TextView state;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                time = itemView.findViewById(R.id.time);
                state = itemView.findViewById(R.id.state);
            }

            public TextView getTime() { return time; }
            public TextView getState() { return state; }
        }

    }
    private  List<StateLog> logs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_logs);

        AsyncTask.execute(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "eye-refresh-db")
                    .enableMultiInstanceInvalidation()
                    .build();
            List<StateLog> logs = db.stateLogDao().getLastHundredLogs();
            RecyclerView recyclerView = findViewById(R.id.recycler_view_logs);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new LogsListAdapter(logs));
        });

    }
}