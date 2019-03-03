package com.anyemi.sleepmate.Database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class LocationDetails {

    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    private int idle = 0;
    private String latitude;
    private String longitude;
    private String dateTime;
    private String distanceP;

    @Ignore
    public LocationDetails(int idle, String latitude, String longitude, String dateTime, String distanceP) {
        this.idle = idle;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;
        this.distanceP = distanceP;
    }

    public LocationDetails(int id, int idle, String latitude, String longitude, String dateTime, String distanceP) {
        this.id = id;
        this.idle = idle;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;
        this.distanceP = distanceP;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdle() {
        return idle;
    }

    public void setIdle(int idle) {
        this.idle = idle;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDistanceP() {
        return distanceP;
    }

    public void setDistanceP(String distanceP) {
        this.distanceP = distanceP;
    }
}
