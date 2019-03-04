package com.anyemi.sleepmate.Services;

import android.annotation.SuppressLint;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
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

    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    // Notification channel ID.
    private static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";
    NotificationManager mNotifyManager;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private SharedPreferenceConfig sharedPreferenceConfig;

    private static double lat, lan;

    // Power manager to know the device status
    PowerManager pm;
    int isIntracting = 0;

    @SuppressLint("MissingPermission")
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.e(TAG_SERVICE, "on start job called");
        pm = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);

        // User is intraction with the device
        if(pm.isInteractive()){
            Log.e(TAG_SERVICE,"yes intracting");
            isIntracting = 1;
        }else {
            Log.e(TAG_SERVICE, "not intracting");
            isIntracting = 0;
        }
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(BackGroundServices.this);
            sharedPreferenceConfig = new SharedPreferenceConfig(BackGroundServices.this);

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // Use Location CallBack if fusedLocation returns null
            // This is required because, fusedLocationProviderClient gives result few time in an hour on devices higher then Nouget
            // in background
            mLocationCallback = new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    // location is received
                    if (locationResult == null) {
                        Log.e(TAG_SERVICE,"Location result"+String.format(Locale.getDefault(), "%s -- %s", lat, lan));
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            lat = location.getLatitude();
                            lan = location.getLongitude();
                            Log.e(TAG_SERVICE,"Loaction Call Back"+String.format(Locale.getDefault(), "%s -- %s", lat, lan));

                            // Insert Location details in Room database
                            final LocDatabase mDb = LocDatabase.getsInstance(BackGroundServices.this);
                            Executors.newSingleThreadExecutor().execute(new Runnable() {
                                @Override
                                public void run() {
                                    insertLocationToDb(lat,lan,mDb);
                                }
                            });

                            jobFinished(jobParameters,true);
                            // remove once the location received
                            // No need to run it in interval
                            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        }else {
                            Log.e(TAG_SERVICE,"Loaction Call Back null"+String.format(Locale.getDefault(), "%s -- %s", lat, lan));
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());
                            jobFinished(jobParameters,true);
                        }
                    }

                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    super.onLocationAvailability(locationAvailability);
                }
            };




            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {

                        lat = location.getLatitude();
                        lan = location.getLongitude();
                        Toast.makeText(BackGroundServices.this, "Fused Location success", Toast.LENGTH_SHORT).show();
                        Log.e(TAG_SERVICE,"Fused Location success: "+String.format(Locale.getDefault(), "%s -- %s", lat, lan));

                        final LocDatabase mDb = LocDatabase.getsInstance(BackGroundServices.this);
                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                insertLocationToDb(lat,lan,mDb);
                            }
                        });

                        sharedPreferenceConfig.writeLocation(sharedPreferenceConfig.readLocation().concat(""+lat+" / "+lan));

                        jobFinished(jobParameters,true);
//                                latLang.setText(String.format(Locale.getDefault(), "%s -- %s", lat, lan));
                    }else {
                        Toast.makeText(BackGroundServices.this, "Fused Location null", Toast.LENGTH_SHORT).show();


                        // When the location is null, There may be a chance that the user might have turned off the GPS manually.
                        //Show notiication to user that the GPS might be off

                        //for oreo +
                        createNotificationChannel();

                        //Set up the notification content intent to launch the app when clicked
                        PendingIntent contentPendingIntent = PendingIntent.getActivity
                                (BackGroundServices.this, 0, new Intent(BackGroundServices.this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);


                        final NotificationCompat.Builder builder = new NotificationCompat.Builder
                                (BackGroundServices.this, PRIMARY_CHANNEL_ID)
                                .setContentTitle("GPS Location")
                                .setContentText("Check Your GPS Status For Better Result")
                                .setContentIntent(contentPendingIntent)
                                .setSmallIcon(R.drawable.ic_location)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setDefaults(NotificationCompat.DEFAULT_ALL)
                                .setAutoCancel(true);

                        mNotifyManager.notify(0, builder.build());

                        Log.e(TAG_SERVICE,"Fused Location null: "+String.format(Locale.getDefault(), "%s -- %s", lat, lan));
                        sharedPreferenceConfig.writeLocation(sharedPreferenceConfig.readLocation().concat("null "+lat+" / "+lan));
                        // Since the lcation is null.
                        //Request to update the location
                        //Location callback is called to get the location
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());
                        jobFinished(jobParameters,true);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(BackGroundServices.this, "Fused Location failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG_SERVICE,"Fused Location failed: "+String.format(Locale.getDefault(), "%s -- %s", lat, lan));
                    Log.e(TAG_SERVICE,"Fused Location failed exception: "+e.toString());
                }
            });


        return true;
    }


    /**
     * Creates a Notification channel, for OREO and higher.
     */
    public void createNotificationChannel() {

        // Define notification manager object.
        mNotifyManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel
                    (PRIMARY_CHANNEL_ID,
                            "Job Service notification",
                            NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription
                    ("Notifications from Job Service");

            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    private void insertLocationToDb(final double lat, final double lan, final LocDatabase mDb) {


        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                double dist = 0.00;
                LocationDetails locationDetails = mDb.locationDao().lastLocationDetails();
                if(locationDetails != null){
                    Location endPoint = new Location("locationA");
                    endPoint.setLatitude(Double.parseDouble(locationDetails.getLatitude()));
                    endPoint.setLongitude(Double.parseDouble(locationDetails.getLongitude()));

                    Location startPoint = new Location("locationA");
                    startPoint.setLatitude(lat);
                    startPoint.setLongitude(lan);
                    // Distance between the current location and the last location
                    dist= startPoint.distanceTo(endPoint);

                    int id = mDb.locationDao().getLastId();
                    id++;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    String currentDateandTime = sdf.format(new Date());
                    final LocationDetails singleLoc = new LocationDetails(id,isIntracting,
                            String.valueOf(lat),
                            String.valueOf(lan),currentDateandTime,String.valueOf(dist));
                    mDb.locationDao().insert(singleLoc);

                }else {
                    int id = mDb.locationDao().getLastId();
                    id++;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    String currentDateandTime = sdf.format(new Date());
                    final LocationDetails singleLoc = new LocationDetails(id,isIntracting,
                            String.valueOf(lat),
                            String.valueOf(lan),currentDateandTime,String.valueOf(dist));
                    mDb.locationDao().insert(singleLoc);
                }

            }
        });

    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e(TAG_SERVICE, "on stop called");
        sharedPreferenceConfig.writeLocation(sharedPreferenceConfig.readLocation().concat(" on stop"));
        return true;
    }
}
