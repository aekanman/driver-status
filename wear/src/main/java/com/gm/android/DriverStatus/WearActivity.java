package com.gm.android.DriverStatus;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi.MessageListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wearable.Wearable;
import com.gm.android.DriverStatus.HeartbeatService;

public class WearActivity extends Activity implements HeartbeatService.OnChangeListener {


    private static final String LOG_TAG = "DriverStatus";

    private TextView mTextView;
    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;
    private TextView mTextView4;
    private TextView mTextView5;
    private TextView mTextView6;

    private int HRcurrentValue=0;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        // inflate layout depending on watch type (round or square)
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                // as soon as layout is there...
                mTextView = (TextView) stub.findViewById(R.id.heartbeat);
                mTextView1 = (TextView) stub.findViewById(R.id.Gx);
                mTextView2 = (TextView) stub.findViewById(R.id.Gy);
                mTextView3 = (TextView) stub.findViewById(R.id.Gz);
                mTextView4 = (TextView) stub.findViewById(R.id.Ax);
                mTextView5 = (TextView) stub.findViewById(R.id.Ay);
                mTextView6 = (TextView) stub.findViewById(R.id.Az);
                // bind to our service.
                bindService(new Intent(WearActivity.this, HeartbeatService.class), new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName componentName, IBinder binder) {
                        Log.d(LOG_TAG, "connected to service.");
                        // set our change listener to get change events
                        ((HeartbeatService.HeartbeatServiceBinder)binder).setChangeListener(WearActivity.this);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName) {

                    }
                }, Service.BIND_AUTO_CREATE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void onValueChanged(String Str, double newValue){
        // will be called by the service whenever the heartbeat value changes

        switch (Str){
            case "HR":
                mTextView.setText(Double.toString((int)newValue));
                break;
            case "AX":
                mTextView4.setText(Double.toString(newValue));
                break;
            case "AY":
                mTextView5.setText(Double.toString(newValue));
                break;
            case "AZ":
                mTextView6.setText(Double.toString(newValue));
                break;
            case "GX":
                mTextView1.setText(Double.toString(newValue));
                break;
            case "GY":
                mTextView2.setText(Double.toString(newValue));
                break;
            case "GZ":
                mTextView3.setText(Double.toString(newValue));
                break;
        }
    }
}
