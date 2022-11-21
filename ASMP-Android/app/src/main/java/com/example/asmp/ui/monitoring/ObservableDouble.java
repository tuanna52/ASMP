package com.example.asmp.ui.monitoring;

public class ObservableDouble {
    private OnDoubleChangeListener listener;

    private double value;

    public void setOnDoubleChangeListener(OnDoubleChangeListener listener)
    {
        this.listener = listener;
    }

    public double get()
    {
        return value;
    }

    public void set(double value)
    {
        this.value = value;

        if(listener != null)
        {
            listener.onDoubleChanged(value);
        }
    }
}
