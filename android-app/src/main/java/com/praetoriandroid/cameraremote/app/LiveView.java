package com.praetoriandroid.cameraremote.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EView;

@EView
public class LiveView extends SurfaceView {

    @Bean
    Rpc rpc;

    public LiveView(Context context) {
        super(context);
    }

    public LiveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LiveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}
