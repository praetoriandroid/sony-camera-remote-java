package com.praetoriandroid.cameraremote.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.praetoriandroid.cameraremote.LiveViewFetcher;
import com.praetoriandroid.cameraremote.rpc.ActTakePictureRequest;
import com.praetoriandroid.cameraremote.rpc.ActTakePictureResponse;
import com.praetoriandroid.cameraremote.rpc.SetSelfTimerRequest;
import com.praetoriandroid.cameraremote.rpc.SimpleResponse;
import com.praetoriandroid.widget.RadialSelector;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity implements Rpc.ConnectionListener {
    @Bean Rpc rpc;

    @ViewById LiveView liveView;
    @ViewById Button shot;
    @ViewById View progress;
    @ViewById View connectionErrorDialog;
    @ViewById TextView progressLabel;
    @ViewById SelfTimerSelector selfTimer;

    @AfterViews
    void init() {
        progressLabel.setText(R.string.connection_label);
        selfTimer.setOnValueSelectedListener(new RadialSelector.OnValueSelectedListener<Integer>() {
            @Override
            public void onValueSelected(final Integer timer) {
                onSelfTimerSelected(timer);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        rpc.registerInitCallback(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        rpc.unregisterInitCallback(this);
        rpc.stopLiveView();
    }

    @Click
    void shotClicked() {
        shot.setEnabled(false);
        rpc.sendRequest(new ActTakePictureRequest(), shot, new Rpc.ResponseHandler<ActTakePictureResponse>() {
            @Override
            public void onSuccess(ActTakePictureResponse response) {
                shot.setEnabled(true);
            }

            @Override
            public void onFail(Throwable e) {
                Log.e("@@@@@", "Shot failed", e);
                shot.setEnabled(true);
            }
        });
    }

    @Override
    @UiThread (propagation = UiThread.Propagation.REUSE)
    public void onConnected() {
        dismissProgress();

        if (isFinishing()) {
            return;
        }

        shot.setEnabled(true);
        rpc.startLiveView(new Rpc.LiveViewCallback() {
            @Override
            public void onNextFrame(LiveViewFetcher.Frame frame) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(frame.getBuffer(), 0, frame.getSize());
                liveView.putFrame(bitmap);
            }

            @Override
            public void onError(Throwable e) {
                Log.e("@@@@@", "Live view error: " + e);
                rpc.stopLiveView();
                showConnectionErrorDialog();
            }
        });
    }

    @Override
    @UiThread (propagation = UiThread.Propagation.REUSE)
    public void onConnectionFailed(Throwable e) {
        dismissProgress();
        showConnectionErrorDialog();
    }

    @Click
    void wiFiSettingsClicked() {
        dismissConnectionErrorDialog();
        try {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showConnectionErrorDialog();
            Toast.makeText(this, R.string.error_no_wi_fi_settings_activity, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Click
    void reconnectClicked() {
        dismissConnectionErrorDialog();
        showProgress();
        rpc.connect();
    }

    @UiThread (propagation = UiThread.Propagation.REUSE)
    void showProgress() {
        progress.setVisibility(View.VISIBLE);
    }

    @UiThread (propagation = UiThread.Propagation.REUSE)
    void dismissProgress() {
        progress.setVisibility(View.INVISIBLE);
    }

    @UiThread (propagation = UiThread.Propagation.REUSE)
    void showConnectionErrorDialog() {
        connectionErrorDialog.setVisibility(View.VISIBLE);
    }

    @UiThread (propagation = UiThread.Propagation.REUSE)
    void dismissConnectionErrorDialog() {
        connectionErrorDialog.setVisibility(View.INVISIBLE);
    }

    @Click
    void zoomClicked() {
        Toast.makeText(this, "zoom", Toast.LENGTH_SHORT).show();
    }

    private void onSelfTimerSelected(final int timerValue) {
        rpc.sendRequest(new SetSelfTimerRequest(timerValue), selfTimer, new Rpc.ResponseHandler<SimpleResponse>() {
            @Override
            public void onSuccess(SimpleResponse response) {
                Toast.makeText(MainActivity.this, "Self timer was set to " + timerValue,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(Throwable e) {
                Toast.makeText(MainActivity.this, "Failed to set self timer", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

}
