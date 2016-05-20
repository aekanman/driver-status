package com.gm.android.DriverStatus;

import android.os.Handler;
import android.util.Log;
import android.os.Message;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by uwe on 23.03.15.
 */
public class DataLayerListenerService extends WearableListenerService {

    private static final String LOG_TAG = "WearableListener";
    public static final String HEARTBEAT = "HEARTBEAT";

    private static Handler handler;
    private static String currentValue = "";

    public static Handler getHandler() {
        return handler;
    }

    public static void setHandler(Handler handler) {
        DataLayerListenerService.handler = handler;
        // send current value as initial value.
        if(handler!=null);
            //handler.sendEmptyMessage(currentValue);
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        String id = peer.getId();
        String name = peer.getDisplayName();

        Log.d(LOG_TAG, "Connected peer name & ID: " + name + "|" + id);
    }
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Message msg = Message.obtain(); // Creates an new Message instance
        super.onMessageReceived(messageEvent);
        Log.d(LOG_TAG, "received a message from wear: " + messageEvent.getPath());
        // save the new heartbeat value
        msg.obj = (messageEvent.getPath());
        if(handler!=null) {
            // if a handler is registered, send the value as new message
            handler.sendMessage(msg);
        }
    }


}
