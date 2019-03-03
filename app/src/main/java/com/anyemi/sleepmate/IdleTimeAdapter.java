package com.anyemi.sleepmate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class IdleTimeAdapter extends RecyclerView.Adapter<IdleTimeAdapter.IdleViewHolder> {

    private List<IdleModel> listModel;
    private Context context;

    public IdleTimeAdapter(List<IdleModel> listModel, Context context) {
        this.listModel = listModel;
        this.context = context;
    }

    @NonNull
    @Override
    public IdleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.location_item, parent, false);
        return new IdleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IdleViewHolder holder, int position) {
        final IdleModel model = listModel.get(position);
        holder.startTime.setText(Helper.changeDateFormate(model.getIdleStartTime()));
        holder.endTime.setText(Helper.changeDateFormate(model.getIdleEndTime()));
        holder.dayHour.setText(String.valueOf(model.getDays()).concat(" Days ").concat(String.valueOf(model.getHours()).concat(" Hours")));
        holder.minSec.setText(String.valueOf(model.getMin()).concat(" Minutes ").concat(String.valueOf(model.getSec()).concat(" Second")));
        Log.e("test",""+model.getIdleEndTime());
    }

    @Override
    public int getItemCount() {
        return listModel.size();
    }

    public class IdleViewHolder extends RecyclerView.ViewHolder{
        TextView startTime, endTime,dayHour,minSec;
        public IdleViewHolder(@NonNull View itemView) {
            super(itemView);
            startTime = itemView.findViewById(R.id.latitude);
            endTime = itemView.findViewById(R.id.longitude);
            dayHour = itemView.findViewById(R.id.date_time);
            minSec = itemView.findViewById(R.id.last_distance);
        }
    }
}
