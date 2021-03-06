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

import com.anyemi.sleepmate.Database.LocDatabase;
import com.anyemi.sleepmate.Database.LocationDetails;
import com.anyemi.sleepmate.MainActivity;
import com.anyemi.sleepmate.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

import static android.content.Context.NOTIFICATION_SERVICE;

class BackGroundUtils {

    private Context context;
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
    private int isIntracting = 0;

    public BackGroundUtils(BackGroundServices myService) {
        this.context = myService;

    }

    @SuppressLint("MissingPermission")
    public void getLocationUpdates() {
        pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);

        // User is intraction with the device
        if(pm.isInteractive()){
            Log.v(TAG_SERVICE,"yes interacting");
            isIntracting = 1;
        }else {
            Log.v(TAG_SERVICE, "not interacting");
            isIntracting = 0;
        }



        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Use Location CallBack if fusedLocation returns null
        // This is required because, fusedLocationProviderClient gives result few time in an hour on devices higher then Nouget
        // in background
        mLocationCallback = new LocationCallback(){
            @SuppressLint("MissingPermission")
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        lat = location.getLatitude();
                        lan = location.getLongitude();

                        // Insert Location details in Room database
                        final LocDatabase mDb = LocDatabase.getsInstance(context);
                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                insertLocationToDb(lat,lan,mDb);
                            }
                        });


                    }else {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());

                    }
                }
                // remove once the location received
                // No need to run it in interval
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);

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

                    final LocDatabase mDb = LocDatabase.getsInstance(context);
                    Executors.newSingleThreadExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            insertLocationToDb(lat,lan,mDb);
                        }
                    });


//                                latLang.setText(String.format(Locale.getDefault(), "%s -- %s", lat, lan));
                }else {

                    // When the location is null, There may be a chance that the user might have turned off the GPS manually.
                    //Show notification to user that the GPS might be off

                    //for oreo +
                    createNotificationChannel();

                    //Set up the notification content intent to launch the app when clicked
                    PendingIntent contentPendingIntent = PendingIntent.getActivity
                            (context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);


                    final NotificationCompat.Builder builder = new NotificationCompat.Builder
                            (context, PRIMARY_CHANNEL_ID)
                            .setContentTitle("GPS Location")
                            .setContentText("Check Your GPS Status For Better Result")
                            .setContentIntent(contentPendingIntent)
                            .setSmallIcon(R.drawable.ic_location)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setAutoCancel(true);

                    mNotifyManager.notify(0, builder.build());

                    // Since the lcation is null.
                    //Request to update the location
                    //Location callback is called to get the location
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());
//                        jobFinished(jobParameters,false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG_SERVICE,"Fused Location failed exception: "+e.toString());
            }
        });

    }


    /**
     * Creates a Notification channel, for OREO and higher.
     */
    public void createNotificationChannel() {

        // Define notification manager object.
        mNotifyManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

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

                    Location startPoint = new Location("locationB");
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
}
