package com.example.asmp.ui.monitoring;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.example.asmp.MainActivity;
import com.example.asmp.R;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MonitoringFragment extends Fragment {
    String CHANNEL_ID = "asmp_channel";

    int notificationId0 = 0;
    int notificationId1 = 1;
    int notificationId2 = 2;

    private TextView mTemp, mHumid, mCO2, mTime, mDate, mCurrentRoom;

    protected TextWatcher roomChanged;

    FirebaseFirestore db;

    ListenerRegistration registration;

    final ObservableDouble obsTemp = new ObservableDouble();
    final ObservableDouble obsHumid = new ObservableDouble();
    final ObservableInteger obsCO2 = new ObservableInteger();

    double tempObs = 0, humidObs = 0;
    long co2Obs = 0;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_monitoring, container, false);
        createNotificationChannel();

        mCurrentRoom = getActivity().findViewById(R.id.current_room);
        mTemp = root.findViewById(R.id.temperature_value);
        mHumid = root.findViewById(R.id.humidity_value);
        mCO2 = root.findViewById(R.id.co2_value);
        mTime = root.findViewById(R.id.time);
        mDate = root.findViewById(R.id.date);

        db = FirebaseFirestore.getInstance();

        registration = db.collection("data"+mCurrentRoom.getText().charAt(5)).orderBy("timestamp").limitToLast(1).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("SnapshotFailed", "Listen failed.", e);
                    return;
                }
                for (QueryDocumentSnapshot doc : snapshot) {
                    tempObs = doc.getDouble("temperature");
                    humidObs = doc.getDouble("humidity");
                    co2Obs = doc.getLong("co2");

                    mTemp.setText(Double.toString(tempObs));
                    mHumid.setText(Double.toString(humidObs));
                    mCO2.setText(Long.toString(co2Obs));
                    mTime.setText(doc.get("timestamp").toString().substring(13));
                    mDate.setText(doc.get("timestamp").toString().substring(0,10));

                    obsTemp.set(tempObs);
                    obsHumid.set(humidObs);
                    obsCO2.set(co2Obs);
                }
            }
        });

        roomChanged = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                registration.remove();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                registration = db.collection("data"+s.charAt(5)).orderBy("timestamp").limitToLast(1).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("SnapshotFailed", "Listen failed.", e);
                            return;
                        }
                        for (QueryDocumentSnapshot doc : snapshot) {
                            tempObs = doc.getDouble("temperature");
                            humidObs = doc.getDouble("humidity");
                            co2Obs = doc.getLong("co2");

                            mTemp.setText(Double.toString(tempObs));
                            mHumid.setText(Double.toString(humidObs));
                            mCO2.setText(Long.toString(co2Obs));
                            mTime.setText(doc.get("timestamp").toString().substring(13));
                            mDate.setText(doc.get("timestamp").toString().substring(0,10));

                            obsTemp.set(tempObs);
                            obsHumid.set(humidObs);
                            obsCO2.set(co2Obs);
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        // Check when temperature has changed
        obsTemp.setOnDoubleChangeListener(new OnDoubleChangeListener()
        {
            @Override
            public void onDoubleChanged(double newValue)
            {
                if (obsTemp.get() <= 20 || obsTemp.get() >= 35) addNotification0(mCurrentRoom.getText().toString());
            }
        });

        // Check when temperature has changed
        obsHumid.setOnDoubleChangeListener(new OnDoubleChangeListener()
        {
            @Override
            public void onDoubleChanged(double newValue)
            {
                if (obsHumid.get() <= 60 || obsHumid.get() >= 90) addNotification1(mCurrentRoom.getText().toString());
            }
        });

        // Check when co2 concentration has changed
        obsCO2.setOnIntegerChangeListener(new OnIntegerChangeListener()
        {
            @Override
            public void onIntegerChanged(long newValue)
            {
                if (obsCO2.get() <= 800 || obsCO2.get() >= 2000) addNotification2(mCurrentRoom.getText().toString());
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        mCurrentRoom.addTextChangedListener(roomChanged);
    }

    @Override
    public void onPause() {
        super.onPause();
        registration.remove();
        mCurrentRoom.removeTextChangedListener(roomChanged);
    }

    // Create the channel for notification
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Create a notification when the temperature exceed 35°C or below 20°C
    private void addNotification0(String room) {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.warning)
                .setContentTitle("Warning: " + room)
                .setContentText("The temperature is in dangerous zone for mushroom production")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId0, mBuilder.build());
    }

    // Create a notification when the humidity exceed 90% or below 60%
    private void addNotification1(String room) {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.warning)
                .setContentTitle("Warning: " + room)
                .setContentText("The humidity is in dangerous zone for mushroom production")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId1, mBuilder.build());
    }

    // Create a notification when the co2 concentration exceed 2000 ppm or below 800ppm
    private void addNotification2(String room) {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.warning)
                .setContentTitle("Warning: " + room)
                .setContentText("The co2 concentration is in dangerous zone for mushroom production")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId2, mBuilder.build());
    }
}
