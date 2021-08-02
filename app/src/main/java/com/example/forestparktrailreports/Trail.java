package com.example.forestparktrailreports;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Trail {
    private String name;
    private int daysFromLastHike;
    private String date;

    public static Comparator<Trail> TrailLastHikedNewest = new Comparator<Trail>() {
        @Override
        public int compare(Trail o1, Trail o2) {
            return o1.getLastHikedInt() - o2.getLastHikedInt();
        }
    };

    public static Comparator<Trail> TrailLastHikedOldest = new Comparator<Trail>() {
        @Override
        public int compare(Trail o1, Trail o2) {
            return o2.getLastHikedInt() - o1.getLastHikedInt();
        }
    };

    public static Comparator<Trail> TrailNameAZ = new Comparator<Trail>() {
        @Override
        public int compare(Trail o1, Trail o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    public static Comparator<Trail> TrailNameZA = new Comparator<Trail>() {
        @Override
        public int compare(Trail o1, Trail o2) {
            return o2.getName().compareTo(o1.getName());
        }
    };


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Trail(String name, String date) throws ParseException {
        this.name = name;
        this.date = date;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:s", Locale.ENGLISH);
        Date realDate = formatter.parse(date);
        long diffInMillies = Math.abs(Calendar.getInstance().getTime().getTime() - realDate.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        this.daysFromLastHike = (int) Math.floor(diff);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDaysFromLastHike() {
        return Integer.toString(daysFromLastHike);
    }

    public int getLastHikedInt() {
        return daysFromLastHike;
    }

    public void setDaysFromLastHike(int daysFromLastHike) {
        this.daysFromLastHike = daysFromLastHike;
    }

    @Override
    public String toString() {
        return "Trail{" +
                "name='" + name + '\'' +
                ", lastHiked=" + daysFromLastHike +
                '}';
    }
}
