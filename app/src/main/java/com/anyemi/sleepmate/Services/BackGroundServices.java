package com.anyemi.sleepmate.Services;

import android.annotation.SuppressLint;


import android.os.AsyncTask;


import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;


public class BackGroundServices extends JobService {
    private static final String TAG_SERVICE = BackGroundServices.class.getName();


    @SuppressLint("MissingPermission")
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
    Log.e(TAG_SERVICE,"on start Job");
//        Toast.makeText(this, "on start job", Toast.LENGTH_SHORT).show();
        @SuppressLint("StaticFieldLeak")
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
