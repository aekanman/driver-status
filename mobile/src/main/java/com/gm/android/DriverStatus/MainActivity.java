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

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.text.DateFormat;
import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.gm.android.DriverStatus.logger.LogView;
import com.gm.android.DriverStatus.logger.LogWrapper;
import com.gm.android.DriverStatus.logger.MessageOnlyLogFilter;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
//implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
    private TextView textView;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private TextView textView6;
    private TextView textView7;
    private GoogleApiClient mGoogleApiClient;
    private static final String LOG_TAG = "DriverStatus";
    private static final String START_ACTIVITY = "/start_activity";

    public static final String TAG = "SleepHistory";
    private static final int REQUEST_OAUTH = 1;
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";

    /**
     *  Track whether an authorization activity is stacking over the current activity, i.e. when
     *  a known auth error is being resolved, such as showing the account chooser or presenting a
     *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private static boolean authInProgress = false;

    public static GoogleApiClient mClient = null;

    private static ArrayList<Session> sessionsSleep = null;
    private static ArrayList<SleepData> sleepList = null;
    private static ArrayList<SleepInfo> sleepInfoList = null;
    private static Date lastSleepSession = null;

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

//        setContentView(R.layout.activity_main);
        // This method sets up our custom logger, which will print all log messages to the device
        // screen, as well as to adb logcat.
        initializeLogging();

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        buildFitnessClient();
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

    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_NUTRITION_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                com.gm.android.DriverStatus.logger.Log.i(TAG, "Connected!!!");
                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Insert - Commented out
                                new InsertAndVerifyDataTask().execute();

//                                DataReadRequest readRequest = querySleepData();

                                // [START read_dataset]
                                // Invoke the History API to fetch the data with the query and await the result of
                                // the read request.
//                                DataReadResult dataReadResult =
//                                        Fitness.HistoryApi.readData(mClient, readRequest).await(0, TimeUnit.MINUTES);

//                                printData(dataReadResult);
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    com.gm.android.DriverStatus.logger.Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    com.gm.android.DriverStatus.logger.Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        com.gm.android.DriverStatus.logger.Log.i(TAG, "Google Play services connection failed. Cause: " +
                                result.toString());
                        Snackbar.make(
                                MainActivity.this.findViewById(R.id.main_activity_view),
                                "Exception while connecting to Google Play services: " +
                                        result.getErrorMessage(),
                                Snackbar.LENGTH_INDEFINITE).show();
                    }
                })
                .build();
    }

    /**
     *  Create a {@link DataSet} to insert data into the History API, and
     *  then create and execute a {@link DataReadRequest} to verify the insertion succeeded.
     *  By using an {@link AsyncTask}, we can schedule synchronous calls, so that we can query for
     *  data after confirming that our insert was successful. Using asynchronous calls and callbacks
     *  would not guarantee that the insertion had concluded before the read request was made.
     *  An example of an asynchronous call using a callback can be found in the example
     *  on deleting data below.
     */
    private class InsertAndVerifyDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            // Create a new dataset and insertion request.
            DataSet dataSet = insertFitnessData();

            // [START insert_dataset]
            // Then, invoke the History API to insert the data and await the result, which is
            // possible here because of the {@link AsyncTask}. Always include a timeout when calling
            // await() to prevent hanging that can occur from the service being shutdown because
            // of low memory or other conditions.
//            Log.i(TAG, "Inserting the dataset in the History API.");
//            com.google.android.gms.common.api.Status insertStatus =
//                    Fitness.HistoryApi.insertData(mClient, dataSet)
//                            .await(1, TimeUnit.MINUTES);

            // Before querying the data, check to see if the insertion succeeded.
//            if (!insertStatus.isSuccess()) {
//                Log.i(TAG, "There was a problem inserting the dataset.");
//                return null;
//            }

            // At this point, the data has been inserted and can be read.
//            Log.i(TAG, "Data insert was successful!");
            // [END insert_dataset]

            // Begin by creating the query.
            DataReadRequest readRequestForSleep = querySleepData();
            DataReadRequest readRequestForActivity = queryActivityData();

            // [START read_dataset]
            // Invoke the History API to fetch the data with the query and await the result of
            // the read request.
            DataReadResult dataReadResultForSleep =
                    Fitness.HistoryApi.readData(mClient, readRequestForSleep).await(1, TimeUnit.MINUTES);
            DataReadResult dataReadResultForActivity =
                    Fitness.HistoryApi.readData(mClient, readRequestForActivity).await(1, TimeUnit.MINUTES);

            // [END read_dataset]

            // For the sake of the sample, we'll print the data so we can see what we just added.
            // In general, logging fitness information should be avoided for privacy reasons.
            printData(dataReadResultForSleep);
            printData(dataReadResultForActivity);

            return null;
        }
    }

    /**
     * Create and return a {@link DataSet} of step count data for insertion using the History API.
     */
    private DataSet insertFitnessData() {
//        Log.i(TAG, "Creating a new data insert request.");

        // [START build_insert_data_request]
        // Set a start and end time for our data, using a start time of 1 hour before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        // Create a data source
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setStreamName(TAG + " - STEP")
                .setType(DataSource.TYPE_RAW)
                .build();

        // Create a data set
        int stepCount = 3500;
        DataSet dataSet = DataSet.create(dataSource);
        // For each data point, specify a start time, end time, and the data value -- in this case,
        // the number of new steps.
        DataPoint dataPoint = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_STEPS).setInt(stepCount);
        dataSet.add(dataPoint);
        // [END build_insert_data_request]

        return dataSet;
    }

    /**
     * Return a {@link DataReadRequest} for all sleep duration changes in the past night.
     */
    public static DataReadRequest querySleepData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        com.gm.android.DriverStatus.logger.Log.i(TAG, "Range: " + dateFormat.format(startTime) + " - " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        return readRequest;
    }

    /**
     * Return a {@link DataReadRequest} for all step count changes in the past week.
     */
    public static DataReadRequest queryActivityData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        com.gm.android.DriverStatus.logger.Log.i(TAG, "Range: " + dateFormat.format(startTime) + " - " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        return readRequest;
    }

    /**
     * Log a record of the query result. It's possible to get more constrained data sets by
     * specifying a data source or data type, but for demonstrative purposes here's how one would
     * dump all the data. In this sample, logging also prints to the device screen, so we can see
     * what the query returns, but your app should not log fitness information as a privacy
     * consideration. A better option would be to dump the data you receive to a local data
     * directory to avoid exposing it to other applications.
     */
    public static void printData(DataReadResult dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
        // [END parse_read_data_result]
    }

    // [START parse_dataset]
    private static void dumpDataSet(DataSet dataSet) {
        //Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();
        float sleepHours = 0;
        boolean sleepFlag = false;
        String activityType = "";
        for (DataPoint dp : dataSet.getDataPoints()) {
            //Log.i(TAG, dp.getOriginalDataSource().getStreamIdentifier().toString());

            for(Field field : dp.getDataType().getFields()) {
                try {
                    activityType = dp.getOriginalDataSource().getAppPackageName().toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(activityType.contains("sleep") && field.getName().contains("duration")){
                    sleepFlag = true;
                    Value value = dp.getValue(field);
                    sleepHours  = (float) (Math.round((value.asInt() * 2.778 * 0.0000001*10.0))/10.0);
                    com.gm.android.DriverStatus.logger.Log.i(TAG, "Data point:");
                    com.gm.android.DriverStatus.logger.Log.i(TAG, "\tType: " + dp.getDataType().getName());
                    com.gm.android.DriverStatus.logger.Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                    com.gm.android.DriverStatus.logger.Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                    com.gm.android.DriverStatus.logger.Log.i(TAG, "\tField: Sleep duration in h " + sleepHours);
                    com.gm.android.DriverStatus.logger.Log.i(TAG, "\tField: " + field.getName() +
                            " Value: " + dp.getValue(field));
                }
                else if(field.getName().contains("steps")){
                    com.gm.android.DriverStatus.logger.Log.i(TAG, "Data point:");
                    com.gm.android.DriverStatus.logger.Log.i(TAG, "\tType: " + dp.getDataType().getName());
                    com.gm.android.DriverStatus.logger.Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                    com.gm.android.DriverStatus.logger.Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                    com.gm.android.DriverStatus.logger.Log.i(TAG, "\tField: " + field.getName() +
                            " Value: " + dp.getValue(field));
                }
                if(sleepFlag){
                    com.gm.android.DriverStatus.logger.Log.i(TAG, "\tSleep data not found for last night");
                }
            }
        }
    }
    // [END parse_dataset]

    /**
     * Delete a {@link DataSet} from the History API. In this example, we delete all
     * step count data for the past 24 hours.
     */
    private void deleteData() {
        com.gm.android.DriverStatus.logger.Log.i(TAG, "Deleting today's step count data.");

        // [START delete_dataset]
        // Set a start and end time for our data, using a start time of 1 day before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        //  Create a delete request object, providing a data type and a time interval
        DataDeleteRequest request = new DataDeleteRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .build();

        // Invoke the History API with the Google API client object and delete request, and then
        // specify a callback that will check the result.
        Fitness.HistoryApi.deleteData(mClient, request)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            com.gm.android.DriverStatus.logger.Log.i(TAG, "Successfully deleted today's step count data.");
                        } else {
                            // The deletion will fail if the requesting app tries to delete data
                            // that it did not insert.
                            com.gm.android.DriverStatus.logger.Log.i(TAG, "Failed to delete today's step count data.");
                        }
                    }
                });
        // [END delete_dataset]
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_data) {
            deleteData();
            return true;
        } else if (id == R.id.action_update_data){
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            MainActivity.this.startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *  Initialize a custom log class that outputs both to in-app targets and logcat.
     */
    private void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        com.gm.android.DriverStatus.logger.Log.setLogNode(logWrapper);
        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);
        // On screen logging via a customized TextView.
        LogView logView = (LogView) findViewById(R.id.sample_logview);

        // Fixing this lint error adds logic without benefit.
        //noinspection AndroidLintDeprecation
        logView.setTextAppearance(this, R.style.Log);

        logView.setBackgroundColor(Color.WHITE);
        msgFilter.setNext(logView);
        com.gm.android.DriverStatus.logger.Log.i(TAG, "Ready.");
    }
}


