package com.example.douglashammarstam.heartattackapp;

import com.google.android.gms.maps.model.Marker;

public class Aed {
    private int ID;
    private String name;
    private String lat;
    private String lon;
    private String description;
    private int availableForUse;
    private Marker marker;

    public Aed(int ID, String name, String lat, String lon, String description, int availableForUse, Marker marker){

        this.ID = ID;
        this.name = name;
        this.lon = lon;
        this.lat = lat;
        this.availableForUse = availableForUse;
        this.description = description;
        this.marker = marker;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getLon() {
        return lon;
    }

    public String getLat() {
        return lat;
    }

    public String getDescription() {
        return description;
    }

    public int getAvailableForUse() {
        return availableForUse;
    }

    public Marker getMarker() {
        return marker;
    }
}