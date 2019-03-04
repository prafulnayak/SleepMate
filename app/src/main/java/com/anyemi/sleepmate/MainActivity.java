package com.anyemi.sleepmate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.anyemi.sleepmate.Database.LocDatabase;
import com.anyemi.sleepmate.Database.LocationDetails;
import com.anyemi.sleepmate.Services.ServiceUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG_MAIN = MainActivity.class.getSimpleName();
    private static boolean isContineous = false;
    private TextView latLang;

    // RecyclerView to display Location and Idle/Active state of device
    private RecyclerView locationDetailsRv;
    //PagedListAdapte for recycler view to retrieve data from Room database
    private LocationAdapter adapter;

    private SharedPreferenceConfig sharedPreferenceConfig;

    //Recycler view for Idle device time
    private RecyclerView idleTimeRv;
    // List to store the idle device time in details.
    private List<IdleModel> sleepTimeList = new ArrayList<>();
    //Adapter for the idleTimeRv
    private IdleTimeAdapter idleTimeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferenceConfig = new SharedPreferenceConfig(this);
        latLang = findViewById(R.id.latLan);

        locationDetailsRv = findViewById(R.id.sleep_rv);

        idleTimeRv = findViewById(R.id.sleep_rv);
        idleTimeRv.setLayoutManager(new LinearLayoutManager(this));
        idleTimeRv.setHasFixedSize(true);

        idleTimeAdapter = new IdleTimeAdapter(sleepTimeList,this);
        idleTimeRv.setAdapter(idleTimeAdapter);

        //Room Database Initialized
        final LocDatabase mDb = LocDatabase.getsInstance(this);
        updateData(mDb);


        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // reuqest for permission
            int locationRequestCode = 11;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);

        } else {
            // already permission granted

            //Check the GPS from hardware. Why this required ?
            //Required or android version less then oreo
            // Some time the user may turn of the GPS or certain reason. It will enable the device GPS automatically when opened.
            EnableGPSAutoMatically();

        }
    }

    private void EnableGPSAutoMatically() {
        GoogleApiClient googleApiClient = null;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(MainActivity.this)
                    .addOnConnectionFailedListener(MainActivity.this).build();
            googleApiClient.connect();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//            locationRequest.setInterval(30 * 1000);
//            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result
                            .getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            Toast.makeText(MainActivity.this, "success on", Toast.LENGTH_SHORT).show();
                            // All location settings are satisfied.
                            //Schedule the job
                            scheduleJob();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            Toast.makeText(MainActivity.this, "GPS is not on", Toast.LENGTH_SHORT).show();
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(MainActivity.this, 1000);

                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Toast.makeText(MainActivity.this, "Setting change not allowed", Toast.LENGTH_SHORT).show();
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    // Schedule Background job
    private void scheduleJob() {

        ServiceUtils serviceUtils = new ServiceUtils();
        serviceUtils.scheduleTask(this);
    }

    // update data ti UI from Room database
    private void updateData(final LocDatabase mDb) {
//        sharedPreferenceConfig.writeLocation("no");
//        latLang.setText(sharedPreferenceConfig.readLocation());

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                List<LocationDetails> lDetails = mDb.locationDao().allDetails();
                LocationDetails startLoc = null;
                for(LocationDetails ld : lDetails){
                    Log.e(TAG_MAIN,""+ld.getDistanceP());

                    // check idle state of the device
                    // 0 for idle state 1 for active state (User is interaction with the device when the Background job ran)
                    if(ld.getIdle() == 0){

                        if(!isContineous){
                            startLoc = ld;
                        }

                        isContineous = true;
                        //idle state
                        double distance = Double.parseDouble(ld.getDistanceP());
                        // The device is in idle state
                        // Here two senario arises:
                        //1. Device is idle But the user is driving keeping the mobile in pocket.
                        //2. Device is idle and The user is not travelling: Hence we can assume the device is in idle state
                        //   and in one location

                        // case 2 condition satisfies
                        if(distance>=0 && distance<=30){
                            // idle state and in one location
                            // Here two posibilities arises"
                            // Device is in idle state and in one location but
                            // 1.the device is only idle for short period of time and
                            // 2.the device is idle for long period of time

                            //calculate time to find out for how long the device is idle
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                            try {
                                Date endD = sdf.parse(ld.getDateTime());
                                Date startD = sdf.parse(startLoc.getDateTime());
                                long different = startD.getTime() - endD.getTime();
//                                String diff = Helper.getDifference(endD,startD);
                                long secondsInMilli = 1000;
                                long minutesInMilli = secondsInMilli * 60;
                                long hoursInMilli = minutesInMilli * 60;
                                long daysInMilli = hoursInMilli * 24;


                                // No of Days
                                long elapsedDays = different / daysInMilli;
                                different = different % daysInMilli;
                                // How many Hours
                                long elapsedHours = different / hoursInMilli;
                                different = different % hoursInMilli;
                                // Minitues
                                long elapsedMinutes = different / minutesInMilli;
                                different = different % minutesInMilli;
                                // Seconds
                                long elapsedSeconds = different / secondsInMilli;
                                String diff = ""+elapsedDays+" "+elapsedHours+" "+elapsedMinutes+" "+elapsedSeconds;

                                // You can check with Days/ Hours / Minutes/ Seconds
                                // For example: if you need idle time list which is greater then 2 hours, then
                                // if(elapsedHours >=2) will work for you
                                if(elapsedMinutes>=1){

                                    updateIdleTimeForUI(startLoc,ld,elapsedDays,elapsedHours,elapsedMinutes,elapsedSeconds);
                                }

                                Log.e(TAG_MAIN,"diff: "+diff);

                            } catch (ParseException ex) {
                                Log.e("Exception", ex.getLocalizedMessage());
                            }catch (NullPointerException ex){
                                Log.e("Exception", ex.getLocalizedMessage());
                            }

                        }else {
                            //travelling but device is idle: Ignore the state
                            //Here we can calculate the distance the user travelled as well as where the user is going.
                        }
                    }else {
                        startLoc = null;
                        isContineous = false;
                    }
                }
            }
        });

        locationDetailsRv.setLayoutManager(new LinearLayoutManager(this));
        locationDetailsRv.setHasFixedSize(true);
        //View Model: Responsible for retrieving data from Room database in pagination way.
        // Observes if there is some changes occured in the database and update the UI accordingly
        LocViewModel viewModel = ViewModelProviders.of(this).get(LocViewModel.class);

        adapter = new LocationAdapter(this);

        viewModel.getLocationListLiveData().observe(this, new Observer<PagedList<LocationDetails>>() {
            @Override
            public void onChanged(@Nullable PagedList<LocationDetails> locationDetailList) {
                //clear the adapter
                adapter.submitList(null);
                //submit LocationDetails list to adapter
                adapter.submitList(locationDetailList);
                locationDetailsRv.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                //move to top of the recycler view
                locationDetailsRv.smoothScrollToPosition(0);
            }
        });

    }

    // Update the idleModel list and update the UI
    private void updateIdleTimeForUI(LocationDetails startLoc, LocationDetails ld, long elapsedDays, long elapsedHours, long elapsedMinutes, long elapsedSeconds) {


        IdleModel idleModel = new IdleModel(startLoc.getDateTime(),
                ld.getDateTime(),elapsedDays,elapsedHours,elapsedMinutes,elapsedSeconds);

        for(IdleModel model: sleepTimeList){
            if(idleModel.getIdleStartTime().equals(model.getIdleStartTime())){
                sleepTimeList.remove(model);
            }
        }

        sleepTimeList.add(idleModel);

        idleTimeAdapter= new IdleTimeAdapter(sleepTimeList,this);
        idleTimeRv.setAdapter(idleTimeAdapter);
        idleTimeAdapter.notifyDataSetChanged();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 11:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location Granted. Schedule the background Job.
                    scheduleJob();

                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1000) {
            if(resultCode == Activity.RESULT_OK){
                // Once the GPS on again. Schedule the background job
                scheduleJob();

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
