package com.example.asmp.ui.controlling;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.asmp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

public class ControllingFragment extends Fragment {

    private CheckBox mHeater, mCooler, mBumper, mFan;
    private TextView mCurrentRoom;

    protected TextWatcher roomChanged;

    FirebaseFirestore db;

    DocumentReference manTemp, manHumid, manCO2, autoTemp, autoHumid, autoCO2;

    ListenerRegistration lisregTemp, lisregHumid, lisregCO2;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_controlling, container, false);

        mCurrentRoom = getActivity().findViewById(R.id.current_room);
        mHeater = root.findViewById(R.id.heater);
        mCooler = root.findViewById(R.id.cooler);
        mBumper = root.findViewById(R.id.bumper);
        mFan = root.findViewById(R.id.fan);

        db = FirebaseFirestore.getInstance();

        String currentRoom = "manual" + mCurrentRoom.getText().charAt(5);
        manTemp = db.collection(currentRoom).document("temperature");
        manHumid = db.collection(currentRoom).document("humidity");
        manCO2 = db.collection(currentRoom).document("co2");

//        String currentRoom1 = "automatic" + mCurrentRoom.getText().charAt(5);
//        autoTemp = db.collection(currentRoom1).document("temperature");
//        autoHumid = db.collection(currentRoom1).document("humidity");
//        autoCO2 = db.collection(currentRoom1).document("co2");

        lisregTemp = manTemp.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Error:", "Listen failed.", e);
                    return;
                }
                boolean source = (snapshot != null && !snapshot.getMetadata().hasPendingWrites());
                if (snapshot != null && snapshot.exists() && source) {
                    if ((Long)snapshot.get("status") == 1) {
                        mHeater.setChecked((Long) snapshot.get("heater") == 1);
                        mCooler.setChecked((Long)snapshot.get("cooler") == 1);
                    } else {
                        mHeater.setChecked(false);
                        mCooler.setChecked(false);
                    }
                } else {
                    Log.d("Temp", "Change from local");
                }
            }
        });

        lisregHumid = manHumid.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Error:", "Listen failed.", e);
                    return;
                }
                boolean source = (snapshot != null && !snapshot.getMetadata().hasPendingWrites());
                if (snapshot != null && snapshot.exists() && source) {
                    if ((Long)snapshot.get("status") == 1) {
                        mBumper.setChecked((Long) snapshot.get("bumper") == 1);
                    } else {
                        mBumper.setChecked(false);
                    }
                } else {
                    Log.d("Humid", "Change from local");
                }
            }
        });

        lisregCO2 = manCO2.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Error:", "Listen failed.", e);
                    return;
                }
                boolean source = (snapshot != null && !snapshot.getMetadata().hasPendingWrites());
                if (snapshot != null && snapshot.exists() && source) {
                    if ((Long)snapshot.get("status") == 1) {
                        mFan.setChecked((Long) snapshot.get("fan") == 1);
                    } else {
                        mFan.setChecked(false);
                    }
                } else {
                    Log.d("CO2", "Change from local");
                }
            }
        });

        roomChanged = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lisregTemp.remove();
                lisregHumid.remove();
                lisregCO2.remove();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currentRoom = "manual" + s.charAt(5);
                manTemp = db.collection(currentRoom).document("temperature");
                manHumid = db.collection(currentRoom).document("humidity");
                manCO2 = db.collection(currentRoom).document("co2");

                lisregTemp = manTemp.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("Error:", "Listen failed.", e);
                            return;
                        }
                        boolean source = snapshot != null && !snapshot.getMetadata().hasPendingWrites();
                        if (snapshot != null && snapshot.exists() && source) {
                            if ((Long)snapshot.get("status") == 1) {
                                mHeater.setChecked((Long) snapshot.get("heater") == 1);
                                mCooler.setChecked((Long)snapshot.get("cooler") == 1);
                            } else {
                                mHeater.setChecked(false);
                                mCooler.setChecked(false);
                            }
                        } else {
                            Log.d("Temp", "Change from local");
                        }
                    }
                });

                lisregHumid = manHumid.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("Error:", "Listen failed.", e);
                            return;
                        }
                        boolean source = snapshot != null && !snapshot.getMetadata().hasPendingWrites();
                        if (snapshot != null && snapshot.exists() && source) {
                            if ((Long)snapshot.get("status") == 1) {
                                mBumper.setChecked((Long) snapshot.get("bumper") == 1);
                            } else {
                                mBumper.setChecked(false);
                            }
                        } else {
                            Log.d("Humid", "Change from local");
                        }
                    }
                });

                lisregCO2 = manCO2.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("Error:", "Listen failed.", e);
                            return;
                        }
                        boolean source = snapshot != null && !snapshot.getMetadata().hasPendingWrites();
                        if (snapshot != null && snapshot.exists() && source) {
                            if ((Long)snapshot.get("status") == 1) {
                                mFan.setChecked((Long) snapshot.get("fan") == 1);
                            } else {
                                mFan.setChecked(false);
                            }
                        } else {
                            Log.d("CO2", "Change from local");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        mHeater.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    manTemp.update("status", 1);
                    manTemp.update("heater", 1);
                    //autoTemp.update("status",0);
                } else {
                    manTemp.update("heater", 0);
                }
            }
        });

        mCooler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    manTemp.update("status", 1);
                    manTemp.update("cooler", 1);
                    //autoTemp.update("status",0);
                } else {
                    manTemp.update("cooler", 0);
                }
            }
        });

        mBumper.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    manHumid.update("status", 1);
                    manHumid.update("bumper", 1);
                    //autoHumid.update("status",0);
                } else {
                    manHumid.update("bumper", 0);
                }
            }
        });

        mFan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    manCO2.update("status", 1);
                    manCO2.update("fan", 1);
                    //autoCO2.update("status",0);
                } else {
                    manCO2.update("fan", 0);
                }
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
        lisregTemp.remove();
        lisregHumid.remove();
        lisregCO2.remove();
        mCurrentRoom.removeTextChangedListener(roomChanged);
    }
}
