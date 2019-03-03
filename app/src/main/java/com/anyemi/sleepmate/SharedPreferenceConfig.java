package com.anyemi.sleepmate;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

public class SharedPreferenceConfig {
    private SharedPreferences sharedPreferences;
    private Context context;

    public SharedPreferenceConfig(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.service_prferance), Context.MODE_PRIVATE);

    }


    public void writeLocation(String name){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getResources().getString(R.string.loc_preference), name);
        Log.i("SharedPreferanceWrite: ",""+name);
        editor.apply();
    }

    public String readLocation(){
        String name;
        name = sharedPreferences.getString(context.getResources().getString(R.string.loc_preference),"no");
        Log.i("SharedPreferanceRead: ",""+name);
        return name;
    }

}
