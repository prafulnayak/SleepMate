package com.anyemi.sleepmate.Services;


import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class DeviceActiveService extends JobService {
    private static final String TAG_SERVICE = DeviceActiveService.class.getName();


    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        PowerManager pm = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);

        if(pm.isInteractive()){
            Log.e(TAG_SERVICE,"yes intracting");
        }else {
            Log.e(TAG_SERVICE,"no intracting");
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
