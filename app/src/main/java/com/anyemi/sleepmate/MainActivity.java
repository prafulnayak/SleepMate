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

    private int locationRequestCode = 11;

    private RecyclerView locationDetailsRv;
    private SharedPreferenceConfig sharedPreferenceConfig;

    private LocViewModel viewModel;

    private LocationAdapter adapter;

    private List<IdleModel> sleepTimeList = new ArrayList<>();

    private RecyclerView idleTimeRv;

    private IdleTimeAdapter idleTimeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        sharedPreferenceConfig = new SharedPreferenceConfig(this);

        latLang = findViewById(R.id.latLan);
        locationDetailsRv = findViewById(R.id.loc_rv);



        idleTimeRv = findViewById(R.id.sleep_rv);
        idleTimeRv.setLayoutManager(new LinearLayoutManager(this));
        idleTimeRv.setHasFixedSize(true);

        idleTimeAdapter = new IdleTimeAdapter(sleepTimeList,this);

        idleTimeRv.setAdapter(idleTimeAdapter);

        final LocDatabase mDb = LocDatabase.getsInstance(this);
        updateData(mDb);


        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // reuqest for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);

        } else {
            // already permission granted
            //Check the GPS from hardware
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
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            // **************************
            builder.setAlwaysShow(true); // this is the key ingredient
            // **************************

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
//                            toast("Setting change not allowed");
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

    private void scheduleJob() {
//        ComponentName serviceName = new ComponentName(this.getPackageName(),
//                BackGroundServices.class.getName());
//        JobInfo jobInfo;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
//            jobInfo = new JobInfo.Builder(11,serviceName)
//                    .setPersisted(true)
//                    .setMinimumLatency(UPDATE_INTERVAL_IN_MILLISECONDS)
//                    .build();
//        }else {
//            jobInfo = new JobInfo.Builder(11,serviceName)
//                    .setPersisted(true)
//                    .setPeriodic(UPDATE_INTERVAL_IN_MILLISECONDS).build();
//        }
//
//
////        JobInfo jobInfo =builder.build();
//        jobScheduler.schedule(jobInfo);
//        DeviceUtil deviceUtil = new DeviceUtil();
//        deviceUtil.scheduleTask(this);

        ServiceUtils serviceUtils = new ServiceUtils();
        serviceUtils.scheduleTask(this);
    }


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


                    if(ld.getIdle() == 0){

                        if(!isContineous){
                            startLoc = ld;
                        }

                        isContineous = true;
                        //idle state
                        double distance = Double.parseDouble(ld.getDistanceP());
                        if(distance>0 && distance<30){
                            // idle state and in one location

                            //calculate time
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

                                long elapsedDays = different / daysInMilli;
                                different = different % daysInMilli;

                                long elapsedHours = different / hoursInMilli;
                                different = different % hoursInMilli;

                                long elapsedMinutes = different / minutesInMilli;
                                different = different % minutesInMilli;

                                long elapsedSeconds = different / secondsInMilli;
                                String diff = ""+elapsedDays+" "+elapsedHours+" "+elapsedMinutes+" "+elapsedSeconds;

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
                            //travelling but device is idle
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

        viewModel = ViewModelProviders.of(this).get(LocViewModel.class);

        adapter = new LocationAdapter(this);

        viewModel.getLocationListLiveData().observe(this, new Observer<PagedList<LocationDetails>>() {
            @Override
            public void onChanged(@Nullable PagedList<LocationDetails> locationDetailList) {
                //cleare the adapter
                adapter.submitList(null);
                //submit news list to adapter
                Log.e(TAG_MAIN,"Size : "+locationDetailList.snapshot().size());
                adapter.submitList(locationDetailList);
                locationDetailsRv.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                //move to top of the recycler view
                locationDetailsRv.smoothScrollToPosition(0);
            }
        });

    }

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
