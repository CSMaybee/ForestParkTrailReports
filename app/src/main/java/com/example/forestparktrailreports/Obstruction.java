package com.example.forestparktrailreports;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Obstruction {
    private String type, description, imageSrc;
    private String timeReported;
    private LatLng location;

    public int getDaysFromReport() {
        return daysFromReport;
    }

    public void setDaysFromReport(int daysFromReport) {
        this.daysFromReport = daysFromReport;
    }

    private int daysFromReport;

    @Override
    public String toString() {
        return "Obstruction{" +
                "type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", imageSrc='" + imageSrc + '\'' +
                ", timeReported=" + timeReported +
                ", location=" + location +
                '}';
    }

    public Obstruction(String type, String description, String imageSrc, String timeReported, LatLng location) throws ParseException {
        this.type = type;
        this.description = description;
        this.imageSrc = imageSrc;
        this.timeReported = timeReported;
        this.location = location;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:s", Locale.ENGLISH);
        Date realDate = formatter.parse(timeReported);
        long diffInMillies = Math.abs(Calendar.getInstance().getTime().getTime() - realDate.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        this.daysFromReport = (int) Math.floor(diff);
    }

    public static Comparator<Obstruction> ObstructionNewest = new Comparator<Obstruction>() {
        @Override
        public int compare(Obstruction o1, Obstruction o2) {
            return o1.getDaysFromReport() - o2.getDaysFromReport();
        }
    };

    public static Comparator<Obstruction> ObstructionOldest = new Comparator<Obstruction>() {
        @Override
        public int compare(Obstruction o1, Obstruction o2) {
            return o2.getDaysFromReport() - o1.getDaysFromReport();
        }
    };

    public static Comparator<Obstruction> ObstructionTypeAZ = new Comparator<Obstruction>() {
        @Override
        public int compare(Obstruction o1, Obstruction o2) {
            return o1.getType().compareTo(o2.getType());
        }
    };

    public static Comparator<Obstruction> ObstructionTypeZA = new Comparator<Obstruction>() {
        @Override
        public int compare(Obstruction o1, Obstruction o2) {
            return o2.getType().compareTo(o1.getType());
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        if(description == null) return "No Description";
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }

    public String getTimeReported() {
        return timeReported;
    }

    public void setTimeReported(String timeReported) {
        this.timeReported = timeReported;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
