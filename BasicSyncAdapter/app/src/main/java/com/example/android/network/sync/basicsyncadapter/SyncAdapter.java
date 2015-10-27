/*
 * Copyright 2013 The Android Open Source Project
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

package com.example.android.network.sync.basicsyncadapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.example.android.network.sync.basicsyncadapter.provider.FeedContract;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * For us :
 *
 *
 * Log.i(TAG, "Parsing stream as Atom feed");
 * final List<FeedParser.Entry> entries = feedParser.parse(stream);
 * Log.i(TAG, "Parsing complete. Found " + entries.size() + " entries");
 *
 * ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
 *
 * // Build hash table of incoming entries
 * HashMap<String, FeedParser.Entry> entryMap = new HashMap<String, FeedParser.Entry>();
 * for (FeedParser.Entry e : entries) {
 * entryMap.put(e.id, e);
 * }
 *
 * // Get list of all items
 * Log.i(TAG, "Fetching local entries for merge");
 * Uri uri = FeedContract.Entry.CONTENT_URI; // Get all entries
 * Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
 * assert c != null;
 * Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");
 *
 * // Find stale data
 * while (c.moveToNext()) {
 *   Remove if too old
 *
 *    Uri deleteUri = FeedContract.Entry.CONTENT_URI.buildUpon()
 *    .appendPath(Integer.toString(id)).build();
 *    Log.i(TAG, "Scheduling delete: " + deleteUri);
 *    batch.add(ContentProviderOperation.newDelete(deleteUri).build());
 *    syncResult.stats.numDeletes++;
 * }
 * c.close();
 *
 * // Add new item
 * ...
 *
 * ________
 * Define a sync adapter for the app.
 *
 * <p>This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else.
 *
 * <p>The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * SyncService.
 */
class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    /**
     * URL
     */
    private static final String URL_LOGIN = "http://figarovsgorafi.fr/guacamole/API/login.php?user=florent&password=bonjour";
    private static final String URL_UPLOAD = "http://figarovsgorafi.fr/guacamole/API/save.php?key=%1$s&type=dBa&value=%2$s";

    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    /**
     * Project used when querying content provider. Returns all known fields.
     */
    private static final String[] PROJECTION = new String[] {
            FeedContract.Entry._ID,
            FeedContract.Entry.COLUMN_NAME_DECIBEL,
            FeedContract.Entry.COLUMN_NAME_VIBRATION,
            FeedContract.Entry.COLUMN_NAME_REPETITION,
            FeedContract.Entry.COLUMN_NAME_RECAP,
            FeedContract.Entry.COLUMN_NAME_TIME
    };

    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_DECIBEL = 1;
    public static final int COLUMN_VIBRATION = 2;
    public static final int COLUMN_REPETITION = 3;
    public static final int COLUMN_RECAP = 4;
    public static final int COLUMN_TIME = 5;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     .
     *
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to peform blocking I/O here.
     *
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        try {

            Log.i(TAG, "Performing Sound Measurement");

            final SoundMeasurement micro = new SoundMeasurement();
            int dB1 = micro.measureSound();
            String decibel = dB1 + " (dBA)";

            Log.i(TAG, "Closing Sound Measurement");

            Log.i(TAG, "Performing Accelerometers Measurement");

            final MotionMeasurement motion = new MotionMeasurement(getContext());

            long t = 0;
            Calendar date = Calendar.getInstance();
            t = date.getTimeInMillis();
            long end = t+5000;
            while(t < end) {
                Calendar now = Calendar.getInstance();
                t = now.getTimeInMillis();
            }

            String[] motionMeasure = motion.stopStreaming();
            Log.i(TAG, motionMeasure[0]);
            Log.i(TAG, motionMeasure[1]);

            Log.i(TAG, "Closing Accelerometers Measurement");


            // A relire
            final URL locationLogin = new URL(URL_LOGIN);
            InputStream streamLogin = null;
            InputStream streamUpload = null;

            try {
                Log.i(TAG, "Updating database");
                syncResult.stats.numEntries++;
                updateDataBase(decibel, motionMeasure[0], motionMeasure[1]);
                syncResult.stats.numInserts++;

                Log.i(TAG, "Uploading data: " + locationLogin);
                streamLogin = downloadUrl(locationLogin);

                // Login
                String[] loginResult = checkUpload(streamLogin);
                String status = loginResult[0];
                String key = loginResult[1];
                String reason = loginResult[2];

                Log.i(TAG, status);
                Log.i(TAG, key);
                Log.i(TAG, reason);

                if(status.equals("Success")){
                    Log.i(TAG, "Login succeeded");

                    // Upload
                    final URL locationUpload = new URL(String.format(URL_UPLOAD, key, decibel));
                    streamUpload = downloadUrl(locationUpload);
                    String[] uploadResult = checkUpload(streamUpload);
                    Log.i(TAG, "Upload: " + uploadResult[0]);

                } else if(status.equals("Error")){
                    Log.i(TAG, "Error occurred when reading JSON:" + reason);
                } else {
                    Log.i(TAG, "Error occurred when reading JSON");
                }


                // Send data

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (streamLogin != null) {
                    streamLogin.close();
                }
                if (streamUpload != null) {
                    streamUpload.close();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
        } catch (RemoteException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        } catch (OperationApplicationException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        }
        Log.i(TAG, "Network synchronization complete");
    }


    public void updateDataBase(String decibel, String vibration, String repetition) throws RemoteException, OperationApplicationException{
        final ContentResolver contentResolver = getContext().getContentResolver();
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");
        Uri uri = FeedContract.Entry.CONTENT_URI; // Get all entries
        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
        assert c != null;
        Log.i(TAG, "Found " + c.getCount() + " local entries.");

        // Find stale data
        while (c.moveToNext()) {
            int id = c.getInt(COLUMN_ID);
            int time = c.getInt(COLUMN_TIME);
            Log.i("Time", ""+time);

            /*
            // Remove old entries from the database.
            Uri deleteUri = FeedContract.Entry.CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(id)).build();
            Log.i(TAG, "Scheduling delete: " + deleteUri);
            batch.add(ContentProviderOperation.newDelete(deleteUri).build());
            syncResult.stats.numDeletes++;
            */
        }
        c.close();

        long timeMeasure = 0;
        Calendar date = Calendar.getInstance();
        timeMeasure = date.getTimeInMillis();

        String recap = decibel + ", " + vibration + ", " + repetition;
        Log.i(TAG, "Scheduling insert: decibel=" + decibel);
        batch.add(ContentProviderOperation.newInsert(FeedContract.Entry.CONTENT_URI)
                .withValue(FeedContract.Entry.COLUMN_NAME_DECIBEL, decibel)
                .withValue(FeedContract.Entry.COLUMN_NAME_VIBRATION, vibration)
                .withValue(FeedContract.Entry.COLUMN_NAME_REPETITION, repetition)
                .withValue(FeedContract.Entry.COLUMN_NAME_RECAP, recap)
                .withValue(FeedContract.Entry.COLUMN_NAME_TIME, timeMeasure)
                .build());
        Log.i(TAG, "Applying batch update");

        mContentResolver.applyBatch(FeedContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(
                FeedContract.Entry.CONTENT_URI, // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }

    /**
     * Given a string representation of a URL, sets up a connection and gets an input stream.
     */
    private InputStream downloadUrl(final URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    /**
     * Read JSON stream.
     */

    public String[] checkUpload(final InputStream stream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
        try {
            return readJSONObject(reader);
        }
        finally {
            reader.close();
        }
    }

    public String[] readJSONObject(JsonReader reader) throws IOException {
        String status = "NotRead";
        String key = "NotRead";
        String reason = "NotRead";

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("status")) {
                status = reader.nextString();
            } else if (name.equals("key")) {
                key = reader.nextString();
            } else if (name.equals("reason")) {
                reason = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new String[]{status, key, reason};
    }
}
