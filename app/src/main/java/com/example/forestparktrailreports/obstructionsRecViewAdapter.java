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

import java.text.ParseException;
import java.util.ArrayList;

public class obstructionsRecViewAdapter extends RecyclerView.Adapter<obstructionsRecViewAdapter.ViewHolder>{
    private Context context;
    Context staticContext = App.getContext();
    DataBaseHelperObstructions db = new DataBaseHelperObstructions(staticContext);
    ArrayList<Obstruction> allObstructions = db.getAllObstructions();

    public obstructionsRecViewAdapter(Context context) throws ParseException {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.obstruction_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull  ViewHolder holder, int position) {
        holder.txtType.setText(allObstructions.get(position).getType());
        holder.txtDaysFromReport.setText("Reported: "+allObstructions.get(position).getDaysFromReport()+" days ago.");
        holder.txtDescription.setText("Description: "+allObstructions.get(position).getDescription());
        holder.parent.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapsActivity.class);
            intent.putExtra("Obstruction Location", allObstructions.get(position).getLocation());
            intent.putExtra("Type","Obstruction");
            intent.putExtra("Latitude", allObstructions.get(position).getLocation().latitude);
            intent.putExtra("Longitude", allObstructions.get(position).getLocation().longitude);
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return allObstructions.size();
    }

    public void setObstructions(ArrayList<Obstruction> allObstructions) {
        this.allObstructions = allObstructions;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtType, txtDaysFromReport, txtDescription;
        private CardView parent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtType = itemView.findViewById(R.id.txtType);
            parent = itemView.findViewById(R.id.parent);
            txtDaysFromReport = itemView.findViewById(R.id.txtDaysFromReport);
            txtDescription = itemView.findViewById(R.id.txtObstructionItemDescription);
        }
    }
}
