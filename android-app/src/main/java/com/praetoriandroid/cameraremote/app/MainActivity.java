package com.praetoriandroid.cameraremote.app;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.praetoriandroid.cameraremote.rpc.ActTakePictureRequest;
import com.praetoriandroid.cameraremote.rpc.ActTakePictureResponse;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends ActionBarActivity implements Rpc.InitCallback {

    @ViewById
    LiveView liveView;

    @ViewById
    Button shot;

    @Bean
    Rpc rpc;

    @AfterViews
    void init() {
        rpc.registerInitCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rpc.unregisterInitCallback(this);
    }

    @OptionsItem
    void settingsSelected() {
        Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
    }

    @Click
    void shotClicked() {
        Toast.makeText(this, "shot", Toast.LENGTH_SHORT).show();
        shot.setEnabled(false);
        rpc.sendRequest(new ActTakePictureRequest(), shot, new Rpc.ResponseHandler<ActTakePictureResponse>() {
            @Override
            public void onSuccess(ActTakePictureResponse response) {
                shot.setEnabled(true);
            }

            @Override
            public void onErrorResponse(int errorCode) {
                Log.e("@@@@@", "Shot failed: " + errorCode);
                Toast.makeText(MainActivity.this, "Shot failed: " + errorCode, Toast.LENGTH_SHORT)
                        .show();
                shot.setEnabled(true);
            }

            @Override
            public void onFail(Throwable e) {
                Log.e("@@@@@", "Shot failed", e);
                Toast.makeText(MainActivity.this, "Shot failed: " + e.toString(), Toast.LENGTH_SHORT)
                        .show();
                shot.setEnabled(true);
            }
        });
    }

    @Override
    public void onRpcInitSucceeded() {
        shot.setEnabled(true);
    }

    @Override
    public void onRpcInitFailed(Throwable e) {
        Toast.makeText(this, "RPC initialization failed", Toast.LENGTH_SHORT).show();
    }
}
