package com.anyemi.sleepmate;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.AsyncDifferConfig;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anyemi.sleepmate.Database.LocationDetails;

public class LocationAdapter extends PagedListAdapter<LocationDetails,LocationAdapter.LocViewHolder> {


    private Context mCtx;

    public LocationAdapter(Context mCtx) {
        super(DIFF_CALLBACK);
        this.mCtx = mCtx;
    }
    private static DiffUtil.ItemCallback<LocationDetails> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<LocationDetails>() {
                @Override
                public boolean areItemsTheSame(@NonNull LocationDetails oldItem, @NonNull LocationDetails newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull LocationDetails oldItem, @NonNull LocationDetails newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public LocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.location_item, parent, false);
        return new LocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocViewHolder holder, int position) {
        final LocationDetails locationD = getItem(position);
        if(locationD != null){
            holder.latitude.setText("Latitude: ".concat(String.valueOf(locationD.getLatitude())));
            holder.longitude.setText("Longitude: ".concat(String.valueOf(locationD.getLongitude())));
            String mode;
            if(locationD.getIdle() == 0){
                mode = "Idle";
            }else {
                mode = "Active";
            }
            String positionAndDateTime = String.valueOf("Sl: "+locationD.getId()).concat(". "+mode+". "+Helper.changeDateFormate(locationD.getDateTime()));
            holder.dateTime.setText(positionAndDateTime);
            holder.distance.setText("Distance:".concat(String.valueOf(locationD.getDistanceP())));
        }

    }

    public class LocViewHolder extends RecyclerView.ViewHolder{
        TextView latitude, longitude,dateTime,distance;
        public LocViewHolder(@NonNull View itemView) {
            super(itemView);

            latitude = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);
            dateTime = itemView.findViewById(R.id.date_time);
            distance = itemView.findViewById(R.id.last_distance);
        }
    }
}
