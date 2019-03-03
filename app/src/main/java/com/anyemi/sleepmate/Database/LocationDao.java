package com.anyemi.sleepmate.Database;

import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface LocationDao {

    @Query("SELECT * FROM LocationDetails ORDER BY id DESC")
    DataSource.Factory<Integer, LocationDetails> allLocationDetails();

    @Query("SELECT * FROM LocationDetails WHERE latitude = :titleDesc")
    LocationDetails getSingleNews(String titleDesc);

    @Query("SELECT * FROM LocationDetails ORDER BY id DESC LIMIT 1")
    LocationDetails lastLocationDetails();

    @Query("SELECT id FROM LocationDetails ORDER BY id DESC LIMIT 1;")
    int getLastId();

    @Query("SELECT * FROM LocationDetails ORDER BY id DESC")
    List<LocationDetails> allDetails();


    @Insert
    void insert(List<LocationDetails> news);

    @Insert
    void insert(LocationDetails news);

    @Delete
    void delete(LocationDetails news);
}
