package com.gm.android.DriverStatus;

import com.google.android.gms.wearable.WearableListenerService;
import com.google.android.gms.wearable.MessageEvent;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import com.gm.android.DriverStatus.WearActivity;

/**
 * Created by 2908User on 5/18/16.
 */
public class DataLayerListenerService extends WearableListenerService {

    private static final String START_ACTIVITY = "/start_activity";
    private static final String WEAR_MESSAGE_PATH = "/message";
    private static final String LOG_TAG = "phonetowearable";
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Message msg = Message.obtain(); // Creates an new Message instance
        Log.d(LOG_TAG, "received a message from wear: " + messageEvent.getPath());
        // save the new heartbeat value
        msg.obj = (messageEvent.getPath());
        if( messageEvent.getPath().equalsIgnoreCase( START_ACTIVITY ) ) {
            Intent intent = new Intent( this, WearActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );
        } else {
            super.onMessageReceived( messageEvent );
        }
    }
}