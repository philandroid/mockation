/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.location.philippweiher.test;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;


public class SendMockLocationService extends Service implements
        ConnectionCallbacks, OnConnectionFailedListener {

    private double latitude;
    private double longitude;

    private class TestParam {

        public final String TestAction;
        public final int TestPause;
        public final int InjectionPause;

        public TestParam(String action, int testPause, int injectionPause) {

            TestAction = action;
            TestPause = testPause;
            InjectionPause = injectionPause;
        }
    }

    LocationClient mLocationClient;

    HandlerThread mWorkThread;

    private boolean mTestStarted;

    private LocalBroadcastManager mLocalBroadcastManager;

    private Looper mUpdateLooper;

    private UpdateHandler mUpdateHandler;

    private int mPauseInterval;

    private int mInjectionInterval;

    private String mTestRequest;

    public class UpdateHandler extends Handler {


        public UpdateHandler(Looper inputLooper) {

            super(inputLooper);

        }

        @Override
        public void handleMessage(Message msg) {

            Location mockLocation = new Location(MapsActivity.LOCATION_PROVIDER);

            long elapsedTimeNanos;
            long currentTime;

            int pauseInterval = 1;
            int injectionInterval = 10;

            mTestStarted = true;

            mLocationClient.setMockMode(true);


            try {
                Thread.sleep((long) (pauseInterval * 1000));
            } catch (InterruptedException e) {
                return;
            }

            elapsedTimeNanos = SystemClock.elapsedRealtimeNanos();
            currentTime = System.currentTimeMillis();

            mockLocation.setElapsedRealtimeNanos(elapsedTimeNanos);
            mockLocation.setTime(currentTime);

            mockLocation.setAccuracy(3.0f);
            mockLocation.setLatitude(latitude);
            mockLocation.setLongitude(longitude);

            mLocationClient.setMockLocation(mockLocation);

            try {
                Thread.sleep((long) (injectionInterval * 1000));
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /*
     * At startup, load the static mock location data from MockLocationConstants.java, then
     * create a HandlerThread to inject the locations and start it.
     */
    @Override
      public void onCreate() {
        /*
         * Prepare to send status updates back to the main activity.
         * Get a local broadcast manager instance; broadcast intents sent via this
         * manager are only available within the this app.
         */
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        /*
         * Create a new background thread with an associated Looper that processes Message objects
         * from a MessageQueue. The Looper allows test Activities to send repeated requests to
         * inject mock locations from this Service.
         */
        mWorkThread = new HandlerThread("UpdateThread", Process.THREAD_PRIORITY_BACKGROUND);

        /*
         * Start the thread. Nothing actually runs until the Looper for this thread dispatches a
         * Message to the Handler.
         */
        mWorkThread.start();

        // Get the Looper for the thread
        mUpdateLooper = mWorkThread.getLooper();

        /*
         * Create a Handler object and pass in the Looper for the thread.
         * The Looper can now dispatch Message objects to the Handler's handleMessage() method.
         */
        mUpdateHandler = new UpdateHandler(mUpdateLooper);

        // Indicate that testing has not yet started
        mTestStarted = false;
      }

    /*
     * Since onBind is a static method, any subclass of Service must override it.
     * However, since this Service is not designed to be a bound Service, it returns null.
     */
    @Override
    public IBinder onBind(Intent inputIntent) {
        return null;
    }

    /*
     * Respond to an Intent sent by startService. onCreate() is called before this method,
     * to take care of initialization.
     *
     * This method responds to requests from the main activity to start testing.
     */
    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        // Get the type of test to run
        mTestRequest = startIntent.getAction();

        latitude = startIntent.getDoubleExtra(MapsActivity.LAT_FOR_POINT, 0);
        longitude = startIntent.getDoubleExtra(MapsActivity.LON_FOR_POINT, 0);

        /*
         * If the incoming Intent was a request to run a one-time or continuous test
         */
        if (TextUtils.equals(mTestRequest, MapsActivity.ACTION_START)) {

            // Get the pause interval and injection interval
            mPauseInterval = startIntent.getIntExtra(MapsActivity.EXTRA_PAUSE_VALUE, 2);
            mInjectionInterval = startIntent.getIntExtra(MapsActivity.EXTRA_SEND_INTERVAL, 1);

            // Create a location client
            mLocationClient = new LocationClient(this, this, this);

            // Start connecting to Location Services
            mLocationClient.connect();

        } else if (TextUtils.equals(mTestRequest, MapsActivity.ACTION_STOP_TEST)) {
            // Send a message back to the main activity that the test is stopping
            sendBroadcastMessage();

            // Stop this Service
            stopSelf();
        }

        /*
         * Tell the system to keep the Service alive, but to discard the Intent that
         * started the Service
         */
        return Service.START_STICKY;
    }

    /*
     * Invoked by Location Services if a connection could not be established.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Send connection failure broadcast to main activityl
        sendBroadcastMessage();

        // Shut down. Testing can't continue until the problem is fixed.
        stopSelf();
    }

    /**
     * Send a broadcast message back to the main Activity, indicating a change in status.
     *
     */
    private void sendBroadcastMessage() {
      // Create a new Intent to send back to the main Activity
      Intent sendIntent = new Intent(MapsActivity.ACTION_SERVICE_MESSAGE);

      // Send the Intent
      mLocalBroadcastManager.sendBroadcast(sendIntent);

    }

    /*
     * When the client is connected, Location Services calls this method, which in turn
     * starts the testing cycle by sending a message to the Handler that injects the test locations.
     */
    @Override
    public void onConnected(Bundle arg0) {
        // Send message to main activity
        sendBroadcastMessage();
        // Start injecting mock locations into Location Services
        // Get the HandlerThread's Looper and use it for our Handler
        mUpdateLooper = mWorkThread.getLooper();
        mUpdateHandler = new UpdateHandler(mUpdateLooper);

        // Get a message object from the global pool
        Message msg = mUpdateHandler.obtainMessage();

        TestParam testParams = new TestParam(mTestRequest, mPauseInterval, mInjectionInterval);

        msg.obj = testParams;

        // Fire off the injection loop
        mUpdateHandler.sendMessage(msg);
    }

    /*
     * If the client becomes disconnected without a call to LocationClient.disconnect(), Location
     * Services calls this method. If the test didn't finish, send a message to the main Activity.
     */
    @Override
    public void onDisconnected() {
        // If testing didn't finish, send an error message
        if (mTestStarted) {
            sendBroadcastMessage();
        }
    }

}
