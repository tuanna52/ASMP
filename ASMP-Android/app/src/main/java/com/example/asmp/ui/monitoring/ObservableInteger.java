package com.example.asmp.ui.monitoring;

public class ObservableInteger {
    private OnIntegerChangeListener listener;

    private long value;

    public void setOnIntegerChangeListener(OnIntegerChangeListener listener)
    {
        this.listener = listener;
    }

    public long get()
    {
        return value;
    }

    public void set(long value)
    {
        this.value = value;

        if(listener != null)
        {
            listener.onIntegerChanged(value);
        }
    }
}
