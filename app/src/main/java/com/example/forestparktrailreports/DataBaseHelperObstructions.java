package com.example.forestparktrailreports;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DataBaseHelperObstructions extends SQLiteOpenHelper {

    public static final String OBSTRUCTION_TABLE = "OBSTRUCTION_TABLE";
    public static final String COLUMN_TYPE = "TYPE";
    public static final String COLUMN_DESCRIPTION = "DESCRIPTION";
    public static final String COLUMN_IMG_SRC = "IMGSRC";
    public static final String COLUMN_TIME_REPORTED = "TIME_REPORTED";
    public static final String COLUMN_LATITUDE = "LATITUDE";
    public static final String COLUMN_LONGITUDE = "LONGITUDE";
    public static final String COLUMN_ID = "ID";
    public static final String TRAIL_TABLE = "TRAIL_TABLE";
    public static final String COLUMN_NAME = "COLUMN_NAME";
    public static final String COLUMN_DATE = "COLUMN_DATE";

    public DataBaseHelperObstructions(@Nullable Context context) {
        super(context, "obstruction.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement1 = "CREATE TABLE " + OBSTRUCTION_TABLE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TYPE + " TEXT, " + COLUMN_DESCRIPTION + " TEXT, " + COLUMN_IMG_SRC + " TEXT, " + COLUMN_TIME_REPORTED + " TEXT, " + COLUMN_LATITUDE + " TEXT, " + COLUMN_LONGITUDE + " TEXT )";
        String createTableStatement2 = "CREATE TABLE " + TRAIL_TABLE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME + " TEXT, " + COLUMN_DATE + " TEXT )";
        db.execSQL(createTableStatement1);
        db.execSQL(createTableStatement2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        Log.i("MFrameworkAHP", "Upgrading database to " + newVersion);
        db.execSQL("drop table if exists " + OBSTRUCTION_TABLE);
        db.execSQL("drop table if exists " + TRAIL_TABLE);
        onCreate(db);
        */
    }

    public boolean addOneObstruction(Obstruction obstruction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TYPE, obstruction.getType());
        cv.put(COLUMN_DESCRIPTION, obstruction.getDescription());
        cv.put(COLUMN_IMG_SRC, obstruction.getImageSrc());
        cv.put(COLUMN_TIME_REPORTED, obstruction.getTimeReported());
        cv.put(COLUMN_LATITUDE, obstruction.getLocation().latitude);
        cv.put(COLUMN_LONGITUDE, obstruction.getLocation().longitude);

        long insert = db.insert(OBSTRUCTION_TABLE, null, cv);
        if (insert == -1) {
            db.close();
            return false;
        }
        else {
            db.close();
            return true;
        }
    }

    public boolean addTrail(Trail trail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, trail.getName());
        cv.put(COLUMN_DATE, trail.getDate().toString());

        long insert = db.insert(TRAIL_TABLE, null, cv);
        if (insert == -1) {
            db.close();
            return false;
        }
        else {
            db.close();
            return true;
        }
    }

    public ArrayList<Trail> getAllTrails() throws ParseException {
        ArrayList<Trail> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM " + TRAIL_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()) {
            do {
                int trailID = cursor.getInt(0);
                String trailName = cursor.getString(1);
                String trailDateHiked = cursor.getString(2);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:s", Locale.ENGLISH);
                Trail newTrail = new Trail(trailName, trailDateHiked);
                returnList.add(newTrail);
            } while(cursor.moveToNext());
        }
        else {
            //failure. do not add anything to list.
        }
        cursor.close();
        db.close();
        return returnList;
    }


    public ArrayList<Obstruction> getAllObstructions() throws ParseException {
        ArrayList<Obstruction> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM " + OBSTRUCTION_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()) {
            do {
                int obstructionID = cursor.getInt(0);
                String obstructionType = cursor.getString(1);
                String obstructionDescription = cursor.getString(2);
                String obstructionImgSrc = cursor.getString(3);
                String obstructionDate = cursor.getString( 4);
                float obstructionLatitude = Float.parseFloat(cursor.getString(5));
                float obstructionLongitude = Float.parseFloat(cursor.getString(6));

                Obstruction newObstruction = new Obstruction(obstructionType,obstructionDescription,obstructionImgSrc,obstructionDate, new LatLng(obstructionLatitude,obstructionLongitude));
                returnList.add(newObstruction);
            } while(cursor.moveToNext());
        }
        else {
            //failure. do not add anything to list.
        }

        cursor.close();
        db.close();
        return returnList;
    }

    public boolean deleteOne(Obstruction obstruction) {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + OBSTRUCTION_TABLE + " WHERE " + COLUMN_TIME_REPORTED + " LIKE '" + obstruction.getTimeReported()+"'";

        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst()) {
            db.close();
            return true;
        }
        else {
            db.close();
            return false;
        }
    }

    public void deleteAllObstructions() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(OBSTRUCTION_TABLE, null, null);
    }

    public void deleteAllTrails() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TRAIL_TABLE, null, null);
    }

    public void deleteAllData() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(OBSTRUCTION_TABLE, null, null);
        db.delete(TRAIL_TABLE, null, null);
    }
}
