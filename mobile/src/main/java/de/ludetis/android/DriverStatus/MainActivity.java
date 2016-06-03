package com.gm.android.DriverStatus;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wearable.MessageApi;
import org.json.JSONException;
import org.json.JSONObject;
import com.gm.android.DriverStatus.DataLayerListenerService;
import android.content.Intent;
import de.ludetis.android.DriverStatus.sleepHistory;

import java.util.List;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
//implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
    private TextView textView;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private TextView textView6;
    private TextView textView7;
    private GoogleApiClient mGoogleApiClient;
    private static final String LOG_TAG = "MyHeartPhone";
    private static final String START_ACTIVITY = "/start_activity";
    private static final String WEAR_MESSAGE_PATH = "/message";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // message from API client! message from wear! The contents is the heartbeat.

            if(textView!=null) {
                try {
                    JSONObject obj = new JSONObject(msg.obj.toString());
                    textView.setText(obj.getString("hr"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(textView2!=null) {
                try {
                    JSONObject obj = new JSONObject(msg.obj.toString());
                    textView2.setText(obj.getString("accX"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(textView3!=null) {
                try {
                    JSONObject obj = new JSONObject(msg.obj.toString());
                    textView3.setText(obj.getString("accY"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(textView4!=null) {
                try {
                    JSONObject obj = new JSONObject(msg.obj.toString());
                    textView4.setText(obj.getString("accZ"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(textView5!=null) {
                try {
                    JSONObject obj = new JSONObject(msg.obj.toString());
                    textView5.setText(obj.getString("gyroX"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(textView6!=null) {
                try {
                    JSONObject obj = new JSONObject(msg.obj.toString());
                    textView6.setText(obj.getString("gyroY"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(textView7!=null) {
                try {
                    JSONObject obj = new JSONObject(msg.obj.toString());
                    textView7.setText(obj.getString("gyroZ"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.heartRate);
        textView2 = (TextView) findViewById(R.id.accX);
        textView3 = (TextView) findViewById(R.id.accY);
        textView4 = (TextView) findViewById(R.id.accZ);
        textView5 = (TextView) findViewById(R.id.gyroX);
        textView6 = (TextView) findViewById(R.id.gyroY);
        textView7 = (TextView) findViewById(R.id.gyroZ);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mGoogleApiClient.connect();

        Intent myIntent = new Intent(MainActivity.this, sleepHistory.class);
//        myIntent.putExtra("key", value); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register our handler with the DataLayerService. This ensures we get messages whenever the service receives something.
        DataLayerListenerService.setHandler(handler);
    }

    @Override
    protected void onPause() {
        // unregister our handler so the service does not need to send its messages anywhere.
        DataLayerListenerService.setHandler(null);
        sendMessageToWatch( START_ACTIVITY, "" );
        super.onPause();

    }
    @Override
    protected void onStop() {
        super.onStop();
        //sendMessageToWatch( WEAR_MESSAGE_PATH, "Closed2" );
    }

    @Override
    public void onConnected(Bundle bundle) {
        sendMessageToWatch( START_ACTIVITY, "/start_activity" );
    }
    private void sendMessageToWatch( final String path, final String text ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG,"begin activity on wear");
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mGoogleApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, text.getBytes() ).await();
                    Log.d(LOG_TAG,"begin activity on wear");
                }
            }
        }).start();
    }
    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}


