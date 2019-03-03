package com.anyemi.sleepmate;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Helper {

    public static String getDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        Log.e("startDate : ",""+ startDate);
        Log.e("endDate : ",""+ endDate);
        Log.e("different : ","" + different);

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

        return diff;

    }

    public static String changeDateFormate(String date) {
        SimpleDateFormat comingFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

        SimpleDateFormat returnFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        String formatedDate = null;
        try {
            Date date1 = comingFormat.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            Date newDate = calendar.getTime();
            formatedDate = returnFormat.format(newDate);
            Log.e("next day a", "" + formatedDate + " p day a: " + date);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return formatedDate;
    }

}
