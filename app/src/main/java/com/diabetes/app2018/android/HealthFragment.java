package com.diabetes.app2018.android;


import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HealthFragment extends Fragment {
    // Views
    private TextView stepsTV;
    private Button stepsButton;

    // BroadcastReceiver for PedometerService
    public static final String RECEIVE_SERVICE = "com.diabetes.app2018.android.RECEIVE_SERVICE";
    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RECEIVE_SERVICE)) {
                int numSteps = intent.getIntExtra("numSteps", -1);
                stepsTV.setText(String.valueOf(numSteps));
            }
        }
    };
    private LocalBroadcastManager bManager;
    private boolean pedometerRunning = false;

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // Important
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        // set up broadcast receiver
        bManager = LocalBroadcastManager.getInstance(mContext);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_SERVICE);
        bManager.registerReceiver(bReceiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_health, container, false);

        // find the views
        stepsTV = (TextView) view.findViewById(R.id.steps_tv);
        stepsButton = (Button) view.findViewById(R.id.steps_button);
        stepsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pedometerRunning) {
                    startPedometer();
                    pedometerRunning = true;
                    stepsButton.setText("Stop Steps");
                } else {
                    stopPedometer();
                    pedometerRunning = false;
                    stepsButton.setText("Start Steps");
                }
            }
        });
        if (isMyServiceRunning(PedometerService.class)) {
            pedometerRunning = true;
            stepsButton.setText("Stop Steps");
        }

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bManager.unregisterReceiver(bReceiver);
    }

    private void startPedometer() {
        Intent intent = new Intent(mContext, PedometerService.class);
        mContext.startService(intent);
    }

    private void stopPedometer() {
        Intent intent = new Intent(mContext, PedometerService.class);
        mContext.stopService(intent);
        // todo save number of steps
    }


    public HealthFragment() {
        // Required empty public constructor
    }
}
