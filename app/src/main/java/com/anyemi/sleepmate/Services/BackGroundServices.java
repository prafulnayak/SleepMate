package com.anyemi.sleepmate.Services;

import android.annotation.SuppressLint;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.anyemi.sleepmate.Database.LocDatabase;
import com.anyemi.sleepmate.Database.LocationDetails;
import com.anyemi.sleepmate.MainActivity;
import com.anyemi.sleepmate.R;
import com.anyemi.sleepmate.SharedPreferenceConfig;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class BackGroundServices extends JobService {
    private static final String TAG_SERVICE = BackGroundServices.class.getName();

    // location updates interval - 2 min
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2*60*1000;

    // fastest updates interval - 1 min
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 60*1000;

    // Notification channel ID.
    private static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";
    NotificationManager mNotifyManager;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private static double lat, lan;

    // Power manager to know the device status
    PowerManager pm;
    int isIntracting = 0;

    @SuppressLint("MissingPermission")
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
    Log.e(TAG_SERVICE,"on start Job");
        BackgroundTask backgroundTask = new BackgroundTask(this) {
            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(jobParameters, false);
            }
        };

        backgroundTask.execute();
        return true;



    }



    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e(TAG_SERVICE, "on stop called");
        return true;
    }

    public static class BackgroundTask extends AsyncTask<Void, Void, Void> {

        BackGroundServices myService;


        BackgroundTask(BackGroundServices myService) {
            this.myService = myService;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            BackGroundUtils utils = new BackGroundUtils(myService);
            utils.getLocationUpdates();
            return null;
        }

    }
}
