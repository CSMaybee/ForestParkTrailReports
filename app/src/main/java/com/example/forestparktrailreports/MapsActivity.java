package com.example.forestparktrailreports;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.forestparktrailreports.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.WayPoint;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final int ACCESS_LOCATION_REQUEST_CODE = 10001;
    private final int zoom = 12;

    //Variable declaration
    private EditText description;
    private TextView obstructionType;
    private TextView obstructionDescription;
    private TextView obstructionDate;
    private RelativeLayout addObstructionPanel;
    private Button obstructionDelete;
    private Spinner obstacleSpinner;
    private ArrayList<String> spinnerObstructionTypes;

    private static ArrayList<Trail> trails;
    private String[] files;
    private Boolean singlePath = false;

    private RelativeLayout descriptionPanel;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.forestparktrailreports.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        //Descriptions of these methods are listed just above their declaration
        addButtonListeners();
        prepareSpinner();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


         //Descriptions of these methods are listed just above their declaration
        requestPermission();

        createTrailsList();

        addMarkerTitleClickListener();

        /* TODO Create a better way of deleting obstructions
        *  addMarkerTitleLongClickListener can be deleted once this has been implemented*/
        addMarkerTitleLongClickListenter();

        /*
        try catch included because the dateReported parsed from a string
        SQLite local database cannot store the Date format so it is converted to string and then back to Date
         */
        try {
            drawAllObstructions();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /*
    Adds the given GPX file to the map display
    The trail will be a polyline with a marker added at the first point in the GPX file
    setCamera will move the cameras starting position the marker
     */
    public void addTrail(String fileName, String color, Boolean setCamera, int zoom) {
        ArrayList<LatLng> coords = new ArrayList<>();
        List<WayPoint> listOfWayPoints;
        String trailName = "Error";
        try {
            AssetManager assetManager = getAssets();
            InputStream input;
            input = assetManager.open(fileName);
            final GPX gpx = GPX.reader().read(input);
            listOfWayPoints = (gpx.getTracks().get(0).getSegments().get(0).getPoints());
            trailName = gpx.getTracks().get(0).getName().toString();
            trailName = trailName.substring(9, trailName.length() - 1);
            String lat, lon;
            for (int i = 0; i < listOfWayPoints.size(); i++) {
                lat = listOfWayPoints.get(i).getLatitude().toString();
                lon = listOfWayPoints.get(i).getLongitude().toString();
                coords.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)));
            }

        } catch (IOException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
        int hexColorCode;
        switch (color) {
            case "green":
                hexColorCode = 0xFF00FF00;
                break;
            case "red":
                hexColorCode = 0xFFFF0000;
                break;
            case "yellow":
                hexColorCode = 0xFFFFFF00;
                break;
            case "blue":
                hexColorCode = 0xFF0000FF;
                break;
            default:
                hexColorCode = 0xFF000000;
        }
        mMap.addPolyline(new PolylineOptions()
                .addAll(coords).width(10).color(hexColorCode).zIndex((color == "red") ? 2 : (color == "yellow") ? 1 : 0));
        mMap.addMarker(new MarkerOptions().position(coords.get(0)).title(trailName).icon(getMarkerIcon(color)));
        if (setCamera) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coords.get(0), zoom));
        }
    }

    /*
    A helper method for addTrail
    This returns the BitmapDescriptor format of the given color
    This is needed for setting the color of map markers
     */
    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    /*
    Returns the title of the GPX file.
    The title is not the same as the file name
    "Error" is returned if no GPX file exists
     */
    public String readGPXName(String fileName) {
        String trailName = "Error";
        try {
            AssetManager assetManager = getAssets();
            InputStream input;
            input = assetManager.open(fileName);
            final GPX gpx = GPX.reader().read(input);
            trailName = gpx.getTracks().get(0).getName().toString();
            trailName = trailName.substring(9, trailName.length() - 1);

        } catch (IOException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
        return trailName;
    }

    /*
    Clears all the polylines and markers from the map
    then redraws all the trails and markers
     */
    public void redrawMap(int zoom) throws ParseException {
        mMap.clear();
        drawAllTrails(zoom);
        drawAllObstructions();
    }

    /*
    Converts Vector image to bitmap
    Bitmap is the type of image markers use
    Method used for custom obstruction map icons
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_warning);
        assert vectorDrawable != null;
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /*
    Adds all markers from database to the map
     */
    public void drawAllObstructions() throws ParseException {
        DataBaseHelperObstructions db = new DataBaseHelperObstructions(this);
        ArrayList<Obstruction> allObstructions = db.getAllObstructions();
        for (int i = 0; i < allObstructions.size(); i++) {
            mMap.addMarker(new MarkerOptions().position(allObstructions.get(i).getLocation()).title(allObstructions.get(i).getType()).icon(bitmapDescriptorFromVector(this)));
        }
    }

    /*
    Adds all trails from the trails arrayList to the map
     */
    public void drawAllTrails(int zoom) {
        for (int i = 0; i < trails.size(); i++) {
            String DisplayColor;
            if (trails.get(i).getLastHikedInt() < 14) DisplayColor = "green";
            else if (trails.get(i).getLastHikedInt() < 28) DisplayColor = "yellow";
            else DisplayColor = "red";
            addTrail(files[i], DisplayColor, false, zoom);
        }
    }

    /*
    Used in onCreate
    Adds listeners for buttons in this activity
     */
    public void addButtonListeners() {
        ImageButton toList = (ImageButton) findViewById(R.id.btnList);
        toList.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, TrailListActivity.class);
            startActivity(intent);
        });

        ImageButton addObstruction = (ImageButton) findViewById(R.id.btnAddObstruction);
        addObstruction.setOnClickListener(v -> addObstruction());

        ImageButton refresh = (ImageButton) findViewById(R.id.btnRefresh);
        refresh.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        Button submit = (Button) findViewById(R.id.btnSubmit);
        submit.setOnClickListener(v -> {
            try {
                submitObstruction();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    /*
    Opens the interface for adding an obstruction
     */
    public void addObstruction() {
        addObstructionPanel = findViewById(R.id.addObstructionPanel);
        descriptionPanel = findViewById(R.id.obstructionDescriptionPanel);

        if(addObstructionPanel.getVisibility()==View.VISIBLE) {
            addObstructionPanel.setVisibility(View.GONE);
            return;
        }
        addObstructionPanel.setVisibility(View.VISIBLE);
        descriptionPanel.setVisibility(View.GONE);
    }

    /*
    Takes all the user input values from the report interface and adds it to the SQLite database for obstructions
    It also adds adds the obstruction to the map
    It also reloads the activity centered on the reported obstruction
    I had issues with the displaying the newly reported obstructions if the activity was not reloaded
     */
    public void submitObstruction() throws ParseException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Consider calling
            // ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        LatLng deviceLocation = new LatLng(latitude,longitude);

        Obstruction obstruction;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:s", Locale.ENGLISH);
        try {
            obstruction = new Obstruction(
                    obstacleSpinner.getSelectedItem().toString(),
                    description.getText().toString(),
                    "null",
                    formatter.format(new Date()),
                    deviceLocation
            );
        }
        catch (Exception e) {
            Toast.makeText(MapsActivity.this, "Error creating obstruction", Toast.LENGTH_SHORT).show();
            obstruction = new Obstruction("error","error","error",formatter.format(new Date()),deviceLocation);
        }

        DataBaseHelperObstructions db = new DataBaseHelperObstructions(MapsActivity.this);
        db.addOneObstruction(obstruction);



        mMap.addMarker(new MarkerOptions().position(deviceLocation).title(obstacleSpinner.getSelectedItem().toString()).icon(bitmapDescriptorFromVector(this)));
        RelativeLayout addObstructionPanel;
        addObstructionPanel = (RelativeLayout) findViewById(R.id.addObstructionPanel);
        addObstructionPanel.setVisibility(View.GONE);


        Context context = this;
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra("Obstruction Location", obstruction.getLocation());
        intent.putExtra("Type","Obstruction");
        intent.putExtra("Latitude", obstruction.getLocation().latitude);
        intent.putExtra("Longitude", obstruction.getLocation().longitude);
        context.startActivity(intent);

    }

    /*
    Sets the adapter for the dropdown menu in the obstruction report interface
    More obstruction types can be added by using spinnerObstructionTypes.add()
     */
    public void prepareSpinner() {
        description = findViewById(R.id.edtDesc);
        obstructionType = findViewById(R.id.txtObstructionType);
        obstructionDescription = findViewById(R.id.txtObstructionDescription);
        obstructionDate = findViewById(R.id.txtObstructionDateReported);
        addObstructionPanel = findViewById(R.id.addObstructionPanel);
        obstructionDelete = findViewById(R.id.btnDeleteObstruction);
        obstacleSpinner = findViewById(R.id.spinnerType);
        spinnerObstructionTypes = new ArrayList<>();
        spinnerObstructionTypes.add("Downed Tree");
        spinnerObstructionTypes.add("Trail Washout");
        spinnerObstructionTypes.add("Rock Slide");
        spinnerObstructionTypes.add("Bolder");
        spinnerObstructionTypes.add("Other");
        // Add more types here

        ArrayAdapter<String> obstructionAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                spinnerObstructionTypes
        );
        obstacleSpinner.setAdapter(obstructionAdapter);
    }

    /*
    Suppressed because if statements ensure permission is granted
     */
    @SuppressLint("MissingPermission")
    /*
    A requirement for location tracking
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                //We can show a dialog that permission is not granted
            }
        }
    }

    /*
    Another requirement for location tracking
     */
    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager
                .PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We can show user a dialog why this permission is necessary
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
            }
            else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
            }
        }
    }

    /*
    Populates the "trails" arraylist
    Intent included for entering the MapsActivity from ListActivity
    Intent used to select what trail or obstruction was taped in ListActivity
     */
    public void createTrailsList() {
        Intent intent = getIntent();
        String TrailName = intent.getStringExtra("Trail Name");
        LatLng ObstructionMarkerPosition = new LatLng(intent.getDoubleExtra("Latitude",0),intent.getDoubleExtra("Longitude",0));
        LatLng DefaultLatLng = new LatLng(0,0);

        DataBaseHelperObstructions db = new DataBaseHelperObstructions(MapsActivity.this);
        try {
            trails = db.getAllTrails();
        } catch (ParseException e) {
            Toast.makeText(this, "Error when getting trail info", Toast.LENGTH_SHORT).show();
        }
        try {
            files = getAssets().list("");

            for (int i = 0; i < files.length -2; i++) {
                if(!containsTrailName(trails, readGPXName(files[i]))){
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:s", Locale.ENGLISH);
                    Trail newTrail = new Trail(readGPXName(files[i]),formatter.format(new Date()));
                    db.addTrail(newTrail);
                    trails.add(newTrail);
                }
            }

            String DisplayColor;
            boolean setCam = true;
            if (TrailName == null) {
                if(ObstructionMarkerPosition.latitude != DefaultLatLng.latitude && ObstructionMarkerPosition.longitude != DefaultLatLng.longitude) {
                    setCam = false;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ObstructionMarkerPosition, 18));
                }
                for (int i = 0; i < trails.size(); i++) {
                    if (trails.get(i).getLastHikedInt() < 14) DisplayColor = "green";
                    else if (trails.get(i).getLastHikedInt() < 28) DisplayColor = "yellow";
                    else DisplayColor = "red";
                    addTrail(files[i], DisplayColor, setCam, zoom);
                    setCam = false;
                }
            } else {
                addTrail(TrailName + ".gpx", "blue", true, zoom);
                singlePath = true;
            }
        } catch (IOException | ParseException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
        db.close();
    }

    /*
    Used to determine if a trail is not in the database
     */
    public boolean containsTrailName(final ArrayList<Trail> list, final String name){
        return list.stream().anyMatch(o -> o.getName().equals(name));
    }

    /*
    Determines if the marker is an obstruction by seeing if the markers title exists in the
    spinners list of trail types
    If it is an obstruction, it desplayes the details of the obstruction matching the location of the marker
    If it is a trail, it clears the map and displays only the trail tapped
     */
    public void addMarkerTitleClickListener() {
        DataBaseHelperObstructions db = new DataBaseHelperObstructions(MapsActivity.this);
        mMap.setOnInfoWindowClickListener(marker -> {
            if (spinnerObstructionTypes.contains(marker.getTitle())) {
                //Display the data on the obstruction

                //get database on obstructions
                ArrayList<Obstruction> obstructionArrayList = null;
                try {
                    obstructionArrayList = db.getAllObstructions();
                } catch (ParseException e) {
                    Toast.makeText(MapsActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }

                Obstruction obstructionFound = null;
                for(int i = 0; i< Objects.requireNonNull(obstructionArrayList).size(); i++) {
                    if(marker.getPosition().toString().equals(obstructionArrayList.get(i).getLocation().toString())) {
                        obstructionFound = obstructionArrayList.get(i);
                        break;
                    }
                }
                if (obstructionFound == null) {
                    Toast.makeText(MapsActivity.this, "Error, Try refreshing", Toast.LENGTH_SHORT).show();
                    return;
                }
                descriptionPanel = findViewById(R.id.obstructionDescriptionPanel);
                if(descriptionPanel.getVisibility()== View.GONE) {
                    descriptionPanel.setVisibility(View.VISIBLE);
                    addObstructionPanel.setVisibility(View.GONE);
                }
                else descriptionPanel.setVisibility(View.GONE);
                obstructionDelete.setVisibility(View.GONE);
                obstructionType.setText("Type: " + obstructionFound.getType());
                obstructionDescription.setText("Description: " + obstructionFound.getDescription());
                obstructionDate.setText("Date: " + obstructionFound.getTimeReported());
                return;
            }

            if (!singlePath) {
                mMap.clear();
                addTrail(marker.getTitle() + ".gpx", "blue", false, zoom);
                try {
                    drawAllObstructions();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                singlePath = true;
            } else {
                try {
                    redrawMap(zoom);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                singlePath = false;
            }

        });
        db.close();
    }

    /**
    Exactly the same as addMarkerTitleClickListener except the Delete button is visable in the obstruction description panel
    This can be safely deleted once a better way of deleting obstructions is implememnted
     */
    public void addMarkerTitleLongClickListenter() {
        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener(){
            @Override
            public void onInfoWindowLongClick(@NonNull Marker marker) {
                if (spinnerObstructionTypes.contains(marker.getTitle())) {
                    //Display the data on the obstruction

                    //get database on obstructions
                    DataBaseHelperObstructions db = new DataBaseHelperObstructions(MapsActivity.this);
                    ArrayList<Obstruction> obstructionArrayList = null;
                    try {
                        obstructionArrayList = db.getAllObstructions();
                    } catch (ParseException e) {
                        Toast.makeText(MapsActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }

                    Obstruction obstructionFound = null;
                    for(int i = 0; i< Objects.requireNonNull(obstructionArrayList).size(); i++) {
                        if(marker.getPosition().toString().equals(obstructionArrayList.get(i).getLocation().toString())) {
                            obstructionFound = obstructionArrayList.get(i);
                            break;
                        }
                    }
                    if (obstructionFound == null) {
                        Toast.makeText(MapsActivity.this, "Error, Try refreshing", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    RelativeLayout descriptionPanel;
                    descriptionPanel = findViewById(R.id.obstructionDescriptionPanel);
                    if(descriptionPanel.getVisibility()==View.GONE) {
                        descriptionPanel.setVisibility(View.VISIBLE);
                        addObstructionPanel.setVisibility(View.GONE);
                    }
                    else descriptionPanel.setVisibility(View.GONE);
                    obstructionDelete.setVisibility(View.VISIBLE);
                    obstructionType.setText("Type: " + obstructionFound.getType());
                    obstructionDescription.setText("Description: " + obstructionFound.getDescription());
                    obstructionDate.setText("Date: " + obstructionFound.getTimeReported());

                    Obstruction finalObstructionFound = obstructionFound;
                    obstructionDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            db.deleteOne(finalObstructionFound);
                            try {
                                redrawMap(zoom);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            descriptionPanel.setVisibility(View.GONE);
                            Toast.makeText(MapsActivity.this, "Deleted" + finalObstructionFound.getTimeReported(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /*
    Only used to pass the trails arrayList to the trialsRecViewAdapter
    */
    public static ArrayList<Trail> getTrailsArray() {
        return trails;
    }
}