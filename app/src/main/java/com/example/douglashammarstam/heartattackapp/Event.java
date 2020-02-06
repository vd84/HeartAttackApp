package com.example.douglashammarstam.heartattackapp;

import com.google.android.gms.maps.model.Marker;

public class Event  {

    private int ID;
    private String date_time;
    private String Lon;
    private String Lat;
    private int personsOnSite;
    private int personsOnTheWay;
    private int activeAlarm;
    private int aedOnSite;
    private String alarmSentToSOS;
    private Marker marker;


    public Event(int ID, String date_time, String lon, String lat, int personsOnSite, int personsOnTheWay, int activeAlarm, int aedOnSite, String alarmSentToSOS, Marker marker) {
        this.ID = ID;
        this.date_time = date_time;
        Lon = lon;
        Lat = lat;
        this.personsOnSite = personsOnSite;
        this.personsOnTheWay = personsOnTheWay;
        this.activeAlarm = activeAlarm;
        this.aedOnSite = aedOnSite;
        this.alarmSentToSOS = alarmSentToSOS;
        this.marker = marker;
    }

    public int getID() {
        return ID;
    }

    public String getDate_time() {
        return date_time;
    }

    public String getLon() {
        return Lon;
    }

    public String getLat() {
        return Lat;
    }

    public int getPersonsOnSite() {
        return personsOnSite;
    }

    public int getPersonsOnTheWay() {
        return personsOnTheWay;
    }

    public int getActiveAlarm() {
        return activeAlarm;
    }

    public int getAedOnSite() {
        return aedOnSite;
    }

    public String getAlarmSentToSOS() {
        return alarmSentToSOS;
    }

    public Marker getMarker() {
        return marker;
    }

    public void personsOnTheWayIncrement(){
        this.personsOnTheWay++;
    }
}



