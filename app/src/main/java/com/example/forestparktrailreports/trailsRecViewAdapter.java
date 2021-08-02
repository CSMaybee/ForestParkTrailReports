package com.example.forestparktrailreports;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class trailsRecViewAdapter extends RecyclerView.Adapter<trailsRecViewAdapter.ViewHolder>{

    ArrayList<Trail> trails = MapsActivity.getTrailsArray();

    private Context context;


    public trailsRecViewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trail_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull  ViewHolder holder, int position) {
        holder.txtName.setText(trails.get(position).getName());
        holder.txtLastHiked.setText("Last Hiked: "+trails.get(position).getDaysFromLastHike()+" days ago.");
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("Trail Name", trails.get(position).getName());
                intent.putExtra("Type","Trail");
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return trails.size();
    }

    public void setTrails(ArrayList<Trail> trails) {
        this.trails = trails;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtName, txtLastHiked;
        private CardView parent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            parent = itemView.findViewById(R.id.parent);
            txtLastHiked = itemView.findViewById(R.id.txtLastHiked);
        }
    }
}
