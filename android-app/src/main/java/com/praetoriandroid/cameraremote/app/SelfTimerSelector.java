package com.praetoriandroid.cameraremote.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.praetoriandroid.widget.RadialSelector;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EView;

@EView
public class SelfTimerSelector extends RadialSelector<TextView, Integer> {

    @App
    ThisApp app;

    @Bean
    Rpc rpc;

    public SelfTimerSelector(Context context) {
        super(context);
    }

    public SelfTimerSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelfTimerSelector(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @AfterInject
    void init() {
        setEnabled(false);
        rpc.registerInitCallback(new Rpc.ConnectionListener() {
            @Override
            public void onConnected() {
                setItemValues(rpc.getAvailableSelfTimers());
                setEnabled(true);
            }

            @Override
            public void onConnectionFailed(Throwable e) {
            }
        });
    }

    @Override
    public TextView createItemView(ViewGroup parent, Integer value) {
        TextView itemView = (TextView) LayoutInflater
                .from(app)
                .inflate(R.layout.selector_item_text, parent, false);
        //noinspection ConstantConditions
        itemView.setText(Integer.toString(value));
        return itemView;
    }
}
