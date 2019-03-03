package com.anyemi.sleepmate.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

@Database(entities = {LocationDetails.class}, version = 4,exportSchema = true)
public abstract class LocDatabase extends RoomDatabase {

    private static final String LOG_TAG = LocDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "newsorg.db";
    private static LocDatabase sInstance;

    public static LocDatabase getsInstance(Context context){
        if(sInstance == null){
            synchronized (LOCK){
                Log.d(LOG_TAG,"Creating new Database Instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        LocDatabase.class,LocDatabase.DATABASE_NAME)
                        .build();
            }
        }
        Log.d(LOG_TAG,"getting the Database Instance");
        return sInstance;
    }

    public abstract LocationDao locationDao();

}
