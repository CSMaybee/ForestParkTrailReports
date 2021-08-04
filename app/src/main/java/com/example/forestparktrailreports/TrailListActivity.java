package com.example.forestparktrailreports;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;

public class TrailListActivity extends AppCompatActivity {

    private RecyclerView trailsRecView;
    private RecyclerView obstructionsRecView;
    trailsRecViewAdapter trailAdapter;
    obstructionsRecViewAdapter obstructionAdapter;
    ArrayList<Trail> trails;
    ArrayList<Obstruction> allObstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trail_list);

        trailsRecView = findViewById(R.id.trailsRecView);
        obstructionsRecView = findViewById(R.id.obstructionsRecView);

        createListOfFiles();


        trailAdapter = new trailsRecViewAdapter(this);
        trailAdapter.setTrails(trails);
        try {
            obstructionAdapter = new obstructionsRecViewAdapter(this);
            obstructionAdapter.setObstructions(allObstructions);
        } catch (ParseException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }


        trailsRecView.setAdapter(trailAdapter);
        trailsRecView.setLayoutManager(new GridLayoutManager(this, 1));
        obstructionsRecView.setAdapter(obstructionAdapter);
        obstructionsRecView.setLayoutManager(new GridLayoutManager(this, 1));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.refresh_menu,menu);
        inflater.inflate(R.menu.sort_menu,menu);
        inflater.inflate(R.menu.list_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_aToz:
                Collections.sort(trails,Trail.TrailNameAZ);
                trailAdapter.notifyDataSetChanged();
                Collections.sort(allObstructions,Obstruction.ObstructionTypeAZ);
                obstructionAdapter.notifyDataSetChanged();
                return true;
            case R.id.menu_zToa:
                Collections.sort(trails,Trail.TrailNameZA);
                trailAdapter.notifyDataSetChanged();
                Collections.sort(allObstructions,Obstruction.ObstructionTypeZA);
                obstructionAdapter.notifyDataSetChanged();
                return true;
            case R.id.menu_Newest:
                Collections.sort(trails,Trail.TrailLastHikedNewest);
                trailAdapter.notifyDataSetChanged();
                Collections.sort(allObstructions,Obstruction.ObstructionNewest);
                obstructionAdapter.notifyDataSetChanged();
                return true;
            case R.id.menu_Oldest:
                Collections.sort(trails,Trail.TrailLastHikedOldest);
                trailAdapter.notifyDataSetChanged();
                Collections.sort(allObstructions,Obstruction.ObstructionOldest);
                obstructionAdapter.notifyDataSetChanged();
                return true;
            case R.id.menu_trails:
                trailsRecView.setVisibility(View.VISIBLE);
                obstructionsRecView.setVisibility(View.GONE);
                setTitle("List of Trails");
                return true;
            case R.id.menu_obstructions:
                trailsRecView.setVisibility(View.GONE);
                obstructionsRecView.setVisibility(View.VISIBLE);
                setTitle("List of Obstructions");
                return true;
            case R.id.menu_refresh:
                Intent intent = new Intent(TrailListActivity.this, TrailListActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void createListOfFiles () {
        try {
            String[] files = getAssets().list("");
            DataBaseHelperObstructions db = new DataBaseHelperObstructions(this);
            trails = db.getAllTrails();
            allObstructions = db.getAllObstructions();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

}
