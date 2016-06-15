package com.gm.android.DriverStatus;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;

import android.os.IBinder;
import android.util.Log;
import android.content.Context;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import android.os.PowerManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by uwe on 01.04.15.
 */
public class HeartbeatService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private int HRcurrentValue=0;
    private double ACCcurrentValueX=0;
    private double ACCcurrentValueY=0;
    private double ACCcurrentValueZ=0;
    private double GYROcurrentValueX=0;
    private double GYROcurrentValueY=0;
    private double GYROcurrentValueZ=0;
    private double newValueX = 0;
    private double newValueY = 0;
    private double newValueZ = 0;
    private static final String LOG_TAG = "DriverStatus";
    private IBinder binder = new HeartbeatServiceBinder();
    private OnChangeListener onChangeListener;
    private GoogleApiClient mGoogleApiClient;
    private static final int BATCH_LATENCY_10s = 10000000;
    private static final int BATCH_LATENCY_5s = 5000000;
    private static final int BATCH_LATENCY_1s = 1000000;
    private double[] accDataX = new double[150];
    private double[] accDataY = new double[150];
    private double[] accDataZ = new double[150];
    private double[] gyroDataX = new double[5];
    private double[] gyroDataY = new double[5];
    private double[] gyroDataZ = new double[5];
    private int accCountX = 0;
    private int accCountY = 0;
    private int accCountZ = 0;
    private int gyroCountX = 0;
    private int gyroCountY = 0;
    private int gyroCountZ = 0;
    private double average = 0;
    protected static PowerManager mPowerManager;
    protected static PowerManager.WakeLock mWakeLock1;

    // interface to pass a heartbeat value to the implementing class
    public interface OnChangeListener {
        void onValueChanged(String str, double newValue);
    }

    /**
     * Binder for this service. The binding activity passes a listener we send the heartbeat to.
     */
    public class HeartbeatServiceBinder extends Binder {
        public void setChangeListener(OnChangeListener listener) {
            onChangeListener = listener;
            // return currently known value
            listener.onValueChanged("HR", HRcurrentValue);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // register us as a sensor listener
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        Sensor accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        boolean resHR = mSensorManager.registerListener(this, mHeartRateSensor,  SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(LOG_TAG, " sensor registered: " + (resHR ? "yes" : "no"));
        boolean resACC = mSensorManager.registerListener(this, accelerometerSensor, 5000000, BATCH_LATENCY_10s);
        Log.d(LOG_TAG, " sensor registered: " + (resACC ? "yes" : "no"));
        boolean resGYRO = mSensorManager.registerListener(this, gyroscopeSensor,  SensorManager.SENSOR_DELAY_NORMAL, BATCH_LATENCY_1s);
        Log.d(LOG_TAG, " sensor registered: " + (resGYRO ? "yes" : "no"));
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Vital Tracker");
        builder.setContentText("Collecting sensor data..");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        startForeground(1, builder.build());
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mGoogleApiClient.connect();

        mPowerManager = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock1(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
        Log.d(LOG_TAG," sensor unregistered");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        String msgString = "";
        // is this a heartbeat event and does it have data?

        JSONObject data = new JSONObject();

        if(sensorEvent.sensor.getType()==Sensor.TYPE_HEART_RATE && sensorEvent.values.length>0 ) {
            int newValue = Math.round(sensorEvent.values[0]);

            // only do something if the value differs from the value before and the value is not 0.
            if(HRcurrentValue != newValue && newValue!=0) {
                HRcurrentValue = newValue;
                // send the value to the listener
                if(onChangeListener!=null) {
                    try {
                        data.put("hr", HRcurrentValue);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    onChangeListener.onValueChanged("HR", newValue);
                }
            }
        }
        else if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER ) {
            newValueX = Math.round((sensorEvent.values[0]*100.0))/100.0;
            newValueY = Math.round((sensorEvent.values[1]*100.0))/100.0;
            newValueZ = Math.round((sensorEvent.values[2]*100.0))/100.0;

            //to check the buffer size
            //newValueX = sensorEvent.sensor.getFifoReservedEventCount();

            // only do something if the value differs from the value before and the value is not 0.
            if(ACCcurrentValueX != newValueX && newValueX!=0) {
                accDataX[accCountX] = newValueX;
                accCountX++;

                ACCcurrentValueX = newValueX;
                // send the value to the listener
                if(onChangeListener!=null && accCountX>(accDataX.length - 1)) {
                    int sum = 0;
                    for (double d : accDataX) sum += Math.abs(d);
                    average = Math.round(((1.0d * sum) * 100.0)/accDataX.length)/100.0;
                    try {
                        data.put("accX", average);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    accCountX = 0;
                    onChangeListener.onValueChanged("AX", average);
                }
                else{
                    msgString = msgString.concat("panic1");
                }
            }
            if(ACCcurrentValueY != newValueY && newValueY!=0) {
                accDataY[accCountY] = newValueY;
                accCountY++;
                ACCcurrentValueY = newValueY;
                // send the value to the listener
                if(onChangeListener!=null && accCountY>(accDataY.length - 1)) {
                    int sum = 0;
                    for (double d : accDataY) sum += Math.abs(d);
                    average = Math.round(((1.0d * sum) * 100.0)/accDataY.length)/100.0;
                    try {
                        data.put("accY", average);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    accCountY = 0;
                    onChangeListener.onValueChanged("AY", average);
                }
                else{
                    msgString = msgString.concat("panic1");
                }
            }
            if(ACCcurrentValueZ != newValueZ && newValueZ!=0) {
                accDataZ[accCountZ] = newValueY;
                accCountZ++;
                ACCcurrentValueZ = newValueZ;
                // send the value to the listener
                if(onChangeListener!=null && accCountZ>(accDataZ.length - 1)) {
                    int sum = 0;
                    for (double d : accDataZ) sum += Math.abs(d);
                    average = Math.round(((1.0d * sum) * 100.0)/accDataZ.length)/100.0;
                    try {
                        data.put("accZ", average);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    accCountZ = 0;
                    onChangeListener.onValueChanged("AZ", average);
                }
                else{
                    msgString = msgString.concat("panic1");
                }
            }
        }

        else if(sensorEvent.sensor.getType()==Sensor.TYPE_GYROSCOPE ) {
            newValueX = sensorEvent.values[0];
            newValueY = sensorEvent.values[1];
            newValueZ = sensorEvent.values[2];
            // only do something if the value differs from the value before and the value is not 0.
            if (GYROcurrentValueX != newValueX && newValueX != 0) {
                gyroDataX[gyroCountX] = newValueX;
                gyroCountX++;
                GYROcurrentValueX = newValueX;
                // send the value to the listener
                if (onChangeListener != null && gyroCountX > (gyroDataX.length - 1)) {
                    int sum = 0;
                    for (double d : gyroDataX) sum += Math.abs(d);
                    average = ((1.00d * sum)) / gyroDataX.length;
                    try {
                        data.put("gyroX", average);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    gyroCountX = 0;
                    onChangeListener.onValueChanged("GX", average);
                }
            }
            if(GYROcurrentValueY != newValueY && newValueY!=0) {
                gyroDataY[gyroCountY] = newValueY;
                gyroCountY++;
                GYROcurrentValueY = newValueY;
                // send the value to the listener
                if (onChangeListener != null && gyroCountY > (gyroDataY.length - 1)) {
                    int sum = 0;
                    for (double d : gyroDataY) sum += Math.abs(d);
                    average = (1.00d * sum) / gyroDataY.length;
                    try {
                        data.put("gyroY", average);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    gyroCountY = 0;
                    onChangeListener.onValueChanged("GY", average);
                }
            }
            if(GYROcurrentValueZ != newValueZ && newValueZ!=0) {
                gyroDataZ[gyroCountZ] = newValueZ;
                gyroCountZ++;
                GYROcurrentValueZ = newValueZ;
                // send the value to the listener
                if (onChangeListener != null && gyroCountZ > (gyroDataZ.length - 1)) {
                    int sum = 0;
                    for (double d : gyroDataZ) sum += Math.abs(d);
                    average = (1.00d * sum) / gyroDataZ.length;
                    try {
                        data.put("gyroZ", average);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    gyroCountZ = 0;
                    onChangeListener.onValueChanged("GZ", average);
                }
            }
        }
        if (!data.toString().equals("{}") ) {
            sendMessageToHandheld(data.toString());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * sends a string message to the connected handheld using the google api client (if available)
     * @param message
     */
    private void sendMessageToHandheld(final String message) {

        if (mGoogleApiClient == null)
            return;

        Log.d(LOG_TAG,"sending a message to handheld: "+message);

        // use the api client to send the heartbeat value to our handheld
        final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                final List<Node> nodes = result.getNodes();
                if (nodes != null) {
                    for (int i=0; i<nodes.size(); i++) {
                        final Node node = nodes.get(i);
                        Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), message, null);
                    }
                }
            }
        });

    }
    public static void wakeLock1(boolean up) {
        if (up) {
            mWakeLock1 = mPowerManager.newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "WakeLock:Accelerometer"
            );
            mWakeLock1.acquire();
        } else {
            if (mWakeLock1 != null) {
                if (mWakeLock1.isHeld()) {
                    mWakeLock1.release();
                }
                mWakeLock1 = null;
            }
        }
    }

}