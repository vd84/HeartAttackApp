package com.example.douglashammarstam.heartattackapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private MarkerOptions place1, place2, kistaDestinationTest, kistaDestinationTest2;
    private Polyline currentPolyline;


    private TextView latitude;
    private TextView longitude;
    private TextView altitude;
    private TextView accuracy;
    private TextView speed;
    private TextView sensorType;
    private TextView updatesOnOff;
    private ToggleButton switchToGpsBalanced;
    private ToggleButton locationOnOff;


    Dialog myDialog;
    private GoogleMap mMap;
    DataBaseHelper heartBreakDB;
    List<Aed> aedsOnMap = new ArrayList<>();
    List<Event> eventsOnMap = new LinkedList<>();
    //LatLng currentLocation;
    LatLng clickedLocation;
    String altitudeOfCurrentLocation;
    String accuracyOfCurrentLocation;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private LocationCallback locationCallback;
    private boolean updateOn = true;
    private boolean followMyPosition = true;
    private boolean onTask = false;
    private Marker currentUserLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int Request_User_Location_Code = 101;
    private double helpRadius = 0.0177 * 1;


    private Button larmaKnapp;
    private Button sosKnapp;
    private Event currentEvent;
    private LatLng currentEventLocation;
    private LatLng currentLocation = new LatLng(59.4173, 17.9344); //Default location as emulator don't have last location


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        latitude = (TextView) findViewById(R.id.lat);
        longitude = (TextView) findViewById(R.id.lon);
        altitude = (TextView) findViewById(R.id.alt);
        speed = (TextView) findViewById(R.id.speed);
        accuracy = (TextView) findViewById(R.id.acc);
        sensorType = (TextView) findViewById(R.id.sensorType);
        updatesOnOff = (TextView) findViewById(R.id.updateOnOff);
        switchToGpsBalanced = (ToggleButton) findViewById(R.id.switchGPS);
        locationOnOff = findViewById(R.id.locationOnOff);
        sosKnapp = findViewById(R.id.sosButton);
        larmaKnapp = findViewById(R.id.alarmButton);

        myDialog = new Dialog(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkUserLocationPermission();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        preLoadDatabase();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(7500); //use value 15000 for real app
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        place1 = new MarkerOptions().position(new LatLng(27.658143, 85.3199503)).title("Location 1");
        place2 = new MarkerOptions().position(new LatLng(27.667491, 85.3208583)).title("Location 2");


        sosKnapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                watchYoutubeVideo("vNVMTwEencw");


            }
        });


        switchToGpsBalanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchToGpsBalanced.isChecked()) {
                    sensorType.setText("GPS");
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


                } else {
                    sensorType.setText("Cell Tower and WiFi");
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                }

            }
        });

        locationOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationOnOff.isChecked()) {
                    updatesOnOff.setText("On");
                    updateOn = true;
                    followMyPosition = true;
                    startLocationUpdates();

                } else {
                    updatesOnOff.setText("off");
                    updateOn = false;
                    followMyPosition = false;

                    stopLocationUpdates();

                }

            }
        });


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitude.setText(String.valueOf(location.getLatitude()));
                        longitude.setText(String.valueOf(location.getLongitude()));
                        accuracy.setText(String.valueOf(location.getAccuracy()));
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));

                        accuracyOfCurrentLocation = String.valueOf(location.getAccuracy());

                        if (location.hasAltitude()) {
                            altitudeOfCurrentLocation = String.valueOf(location.getAltitude());
                            altitude.setText(String.valueOf(location.getAltitude()));

                        } else {
                            altitudeOfCurrentLocation = "does not have altitude";
                            altitude.setText("does not have altitude");
                        }

                        if (location.hasSpeed()) {
                            System.out.println("has speed");

                        } else {
                            System.out.println("does not have speed");
                        }


                    }

                }
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);


        }

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latitude.setText(String.valueOf(location.getLatitude()));
                        longitude.setText(String.valueOf(location.getLongitude()));
                        accuracy.setText(String.valueOf(location.getAccuracy()));
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (followMyPosition) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));

                        }

                        accuracyOfCurrentLocation = String.valueOf(location.getAccuracy());

                        if (location.hasAltitude()) {
                            altitudeOfCurrentLocation = String.valueOf(location.getAltitude());
                        } else {
                            altitudeOfCurrentLocation = "does not have altitude";
                        }

                        if (location.hasSpeed()) {
                            System.out.println("has speed");

                        } else {
                            System.out.println("does not have speed");
                        }


                    }

                }
            }
        };


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        plotAedToMap();
        plotEventOnMap();
        mMap.addMarker(place1);
        mMap.addMarker(place2);


        googleMap.getUiSettings().setZoomControlsEnabled(true);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);


        } else {

            mMap.setMyLocationEnabled(true);
            mMap.setInfoWindowAdapter(new CustomMarkerInfoWindowAdapter(MapsActivity.this));
            mMap.getUiSettings().setMapToolbarEnabled(false);
        }


    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        String str_destination = "destination=" + dest.latitude + "," + dest.longitude;

        String mode = "mode=" + directionMode;

        String parameters = str_origin + "&" + str_destination + "&" + mode;

        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyBbn1GtJKOrz0a4bwoqhhAOlBAlEqNSTEA";

        return url;


    }


    public boolean checkUserLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);

            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Request_User_Location_Code:

                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast myToast = Toast.makeText(getApplicationContext(), "Appen kräver att få komma åt din platsinfomramtion", Toast.LENGTH_SHORT);
                    myToast.show();
                    finish();
                }
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (updateOn) startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);


        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public void btnSettings_onClick(View view) {

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }


    public void preLoadDatabase() {

        heartBreakDB = new DataBaseHelper(this);
        heartBreakDB.getWritableDatabase();
        heartBreakDB.deleteAllRowsInTable("aed");
        heartBreakDB.deleteAllRowsInTable("event");

        List<Aed> preloadAeds = new ArrayList<>();
        preloadAeds.add(new Aed(1, "HS_1", "59.428482", "17.949823", "Reception Företag 1", 1, null));
        preloadAeds.add(new Aed(1, "HS_2", "59.410405", "17.927588", "Reception Företag 2", 1, null));
        preloadAeds.add(new Aed(1, "HS_3", "59.421933", "17.923724", "Kassan COOP", 1, null));
        preloadAeds.add(new Aed(1, "HS_4", "59.407347", "17.948781", "Kassan ICA", 1, null));
        preloadAeds.add(new Aed(1, "HS_5", "59.417563", "17.963034", "Reception Företag 3", 1, null));

        for (Aed x : preloadAeds) {
            heartBreakDB.insertDataToAed(x.getName(), x.getLat(), x.getLon(), x.getDescription(), x.getAvailableForUse());
        }
        heartBreakDB.insertNewEvent("59.418732", "17.939394");
        heartBreakDB.insertNewEvent("59.402702", "17.959634");
    }

    private void plotAedToMap() {
        double latCoord;
        double lonCoord;
        Marker marker;

        if (aedsOnMap.size() != 0) {
            for (Aed e : aedsOnMap) {
                removeMarker(e.getMarker());
            }
        }

        Cursor aedsFromDatabase = heartBreakDB.getAllData("aed");
        if (aedsFromDatabase.getCount() == 0) {
            //No data i database
            return;
        }
        while (aedsFromDatabase.moveToNext()) {
            latCoord = Double.parseDouble(aedsFromDatabase.getString(2));
            lonCoord = Double.parseDouble(aedsFromDatabase.getString(3));

            double belowLat = (currentLocation.latitude + helpRadius);
            double aboveLat = (currentLocation.latitude - helpRadius);
            double belovwLon = (currentLocation.longitude + helpRadius);
            double aboveLon = (currentLocation.longitude - helpRadius);
            if (latCoord < currentLocation.latitude + helpRadius &&
                    latCoord > currentLocation.latitude - helpRadius &&
                    lonCoord < currentLocation.longitude + helpRadius &&
                    lonCoord > currentLocation.longitude - helpRadius) {
                LatLng markerCoord = new LatLng(latCoord, lonCoord);
                marker = mMap.addMarker(new MarkerOptions().position(markerCoord).title(aedsFromDatabase.getString(4)).
                        title(aedsFromDatabase.getString(4)).
                        snippet("Lat: " + aedsFromDatabase.getString(2) + " Lon: " + aedsFromDatabase.getString(3)).
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.aed_icon)));

                aedsOnMap.add(new Aed(aedsFromDatabase.getInt(0),
                        aedsFromDatabase.getString(1),
                        aedsFromDatabase.getString(2),
                        aedsFromDatabase.getString(3),
                        aedsFromDatabase.getString(4),
                        aedsFromDatabase.getInt(5),
                        marker));
                //LatLng markerCoord = new LatLng(latCoord,lonCoord);

            }

        }
    }


    private void plotEventOnMap() {
        double latCoord;
        double lonCoord;
        Event closestEvent = null;
        Marker marker;

        //Remove markers on map
        if (eventsOnMap.size() != 0) {
            for (Event e : eventsOnMap) {
                removeMarker(e.getMarker());
            }
        }

        Cursor eventsFromDatabase = heartBreakDB.getActiveAlarms();
        if (eventsFromDatabase.getCount() == 0) {
            //No Data found
            return;
        }
        while (eventsFromDatabase.moveToNext()) {
            latCoord = Double.parseDouble(eventsFromDatabase.getString(2));
            lonCoord = Double.parseDouble(eventsFromDatabase.getString(3));


            double belowLat = (currentLocation.latitude + helpRadius) - latCoord;
            double aboveLat = latCoord - (currentLocation.latitude - helpRadius);
            double belovwLon = (currentLocation.longitude + helpRadius) - lonCoord;
            double aboveLon = lonCoord - (currentLocation.longitude - helpRadius);
            if (latCoord < currentLocation.latitude + helpRadius &&
                    latCoord > currentLocation.latitude - helpRadius &&
                    lonCoord < currentLocation.longitude + helpRadius &&
                    lonCoord > currentLocation.longitude - helpRadius) {

                LatLng markerCoord = new LatLng(latCoord, lonCoord);
                String snippet = "Lat: " + eventsFromDatabase.getString(2) +
                        " Lon: " + eventsFromDatabase.getString(3) + "\n" +
                        "AED på plats: " + eventsFromDatabase.getInt(7) + "\n" +
                        "Personer på plats: " + eventsFromDatabase.getInt(4) + "\n" +
                        "Personer på väg: " + eventsFromDatabase.getInt(5);


                // Button button_1 = (Button)findViewById(R.id.directions);
                // button_1.setText("Ta mig dit!");


                marker = mMap.addMarker(new MarkerOptions().position(markerCoord).
                        title(eventsFromDatabase.getString(1)).
                        snippet(snippet).
                        //snippet("Lat: " + eventsFromDatabase.getString(2) + " Lon: " + eventsFromDatabase.getString(3)).
                                icon(BitmapDescriptorFactory.fromResource(R.drawable.heartfailure)));

                eventsOnMap.add(new Event(eventsFromDatabase.getInt(0),
                        eventsFromDatabase.getString(1),
                        eventsFromDatabase.getString(2),
                        eventsFromDatabase.getString(3),
                        eventsFromDatabase.getInt(4),
                        eventsFromDatabase.getInt(5),
                        eventsFromDatabase.getInt(6),
                        eventsFromDatabase.getInt(7),
                        eventsFromDatabase.getString(8),
                        marker));
            }
            closestEvent = calculateClosestEvent();
            if (closestEvent != null) {
                closestEvent.personsOnTheWayIncrement();

                showAlertDialogEvent(closestEvent);

                //Toast myToast = Toast.makeText(getApplicationContext(), "Det finns ett event när dig!" + closestEvent.getID(), Toast.LENGTH_SHORT);
                //myToast.show();
            }
        }

    }

    private Event calculateClosestEvent() {
        Event closestEvent = null;
        double closestDistans = 0;
        double latDistans;
        double lonDistans;

        for (Event e : eventsOnMap) {
            latDistans = currentLocation.latitude - Double.parseDouble(e.getLat());
            lonDistans = currentLocation.longitude - Double.parseDouble(e.getLon());
            double distantToEvent = Math.sqrt((latDistans * latDistans) + (lonDistans * lonDistans));
            if (distantToEvent < closestDistans || closestDistans == 0) {
                closestDistans = distantToEvent;
                closestEvent = e;
            }
        }

        return closestEvent;
    }

    public void watchYoutubeVideo(String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }


    public void showAlertDialogEvent(final Event event) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nytt larm: " + event.getID());
        builder.setMessage("Is aed on site? " + event.getAedOnSite() + "\n" +
                "when did it occur? " + event.getDate_time() + "\n" +
                "People onsite: " + event.getPersonsOnSite() + "\n" +
                "People on the way: " + event.getPersonsOnTheWay());

        // add the buttons
        builder.setPositiveButton("Guida mig dit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onTask = true;
                currentEventLocation = new LatLng(Double.valueOf(event.getLon()), Double.valueOf(event.getLat()));

                guidePersonToEvent();

            }
        });
        builder.setNegativeButton("Avbryt", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void guidePersonToEvent() {
        new FetchURL(MapsActivity.this).execute(getUrl(currentLocation, currentEventLocation, "walking"), "walking");

        mMap.addMarker(new MarkerOptions().position(currentEventLocation).
                title("Current Event").
                icon(BitmapDescriptorFactory.fromResource(R.drawable.heartfailure)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentEventLocation, 12));


    }

    public void removeMarker(Marker marker) {
        marker.remove();
    }

}







