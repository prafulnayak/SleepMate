package com.anyemi.sleepmate;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import com.anyemi.sleepmate.Database.LocDatabase;
import com.anyemi.sleepmate.Database.LocationDetails;

public class LocViewModel extends AndroidViewModel {

    private LiveData<PagedList<LocationDetails>> locationListLiveData;
    private Application application;

    public LocViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public LiveData<PagedList<LocationDetails>> getLocationListLiveData() {

        locationListLiveData = null;
        DataSource.Factory<Integer,LocationDetails> factory = LocDatabase.getsInstance(application).locationDao().allLocationDetails();
        //config the pagedList
        //setPageSize(2) retrieves 2 sets of location object in single instance
        PagedList.Config pagConfig = new PagedList.Config.Builder().setPageSize(2).setEnablePlaceholders(false).build();
        LivePagedListBuilder<Integer, LocationDetails> pagedListBuilder = new LivePagedListBuilder(factory,pagConfig);
        locationListLiveData = pagedListBuilder.build();
        return locationListLiveData;
    }
}
