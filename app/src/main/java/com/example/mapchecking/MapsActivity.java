package com.example.mapchecking;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mMap;
    double gpsLongitude;
    double gpsLatitude;
    double apiLongitude;
    double apiLatitude;
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 200;
    private View view;
    LocationTrack locationTrack;
    Button submitButton, googleLocation;
    ProgressDialog progressBar;
    Boolean isGPS = true;
    FloatingActionButton refresh;




    private Location location;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        progressBar = new ProgressDialog(MapsActivity.this);
        submitButton = (Button) findViewById(R.id.submitButton);
        googleLocation = (Button) findViewById(R.id.googleLocation);
        FirebaseApp.initializeApp(this);
        view = getWindow().getDecorView().getRootView();
        startService(new Intent(this, LocationTrack.class));
        locationTrack = new LocationTrack(MapsActivity.this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                locationDetect();
            } else {
                requestLocationPermission();
            }
        } else {
            locationDetect();
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGPS = true;
                showChangeLangDialog(false);
            }
        });

        googleLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGPS = true;
                showChangeLangDialog(true);
            }
        });
        refresh = (FloatingActionButton) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_button_animation));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkLocationPermission()) {
                        locationDetect();
                    } else {
                        requestLocationPermission();
                    }
                } else {
                    locationDetect();
                }
            }
        });

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();
    }

    public void showChangeLangDialog(final Boolean isFused) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        final EditText name = (EditText) dialogView.findViewById(R.id.name);
        final EditText farmerName = (EditText) dialogView.findViewById(R.id.farmerName);

        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String from = "";
                if (!farmerName.getText().toString().equals("")) {
                    if (!name.getText().toString().equals("")) {
                        double latitude = 0;
                        double longitude = 0;
                        String addressName = null;
                        String postalCode = null;
                        String country = null;
                        String state = null;
                        String city = null;
                        try {
                            Geocoder geocoder;
                            List<Address> addresses;

                            if (isFused) {
                                from = "Google API";
                                latitude = apiLatitude;
                                longitude = apiLongitude;
                            } else {
                                from = "Location Manager";
                                latitude = gpsLatitude;
                                longitude = gpsLongitude;
                            }
                            geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            addressName = addresses.get(0).getAddressLine(0);
                            city = addresses.get(0).getLocality();
                            state = addresses.get(0).getAdminArea();
                            country = addresses.get(0).getCountryName();
                            postalCode = addresses.get(0).getPostalCode();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Date today = new Date();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference();
                        if (addressName == null) addressName = "Unknown Place";
                        if (city == null) city = "City Name";
                        if (state == null) state = "State Name";
                        if (country == null) country = "Country Name";
                        if (postalCode == null) postalCode = "Postal Code";
                        ModelValues modelValues = new ModelValues(
                                longitude,
                                latitude,
                                from,
                                today.toString(),
                                farmerName.getText().toString(),
                                name.getText().toString(),
                                addressName,
                                city,
                                state,
                                country,
                                postalCode);
                        try {
                            myRef.child("Locations").child(name.getText().toString()).setValue(modelValues);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getApplicationContext(), "Thanks for your support...", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    } else {
                        farmerName.setError("Please enter user name");
                        Snackbar.make(view, "User name missing", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    farmerName.setError("Please enter farmer name");
                    Snackbar.make(view, "Farmer name missing", Snackbar.LENGTH_LONG).show();

                }
            }
        });

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(gpsLatitude, gpsLongitude);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Your place"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14.0f));
        if (progressBar != null) {
            if (progressBar.isShowing()) {
                progressBar.dismiss();
            }
        }
    }


    private boolean checkLocationPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        Snackbar.make(view, "Permission Granted, Now you can access location.", Snackbar.LENGTH_LONG).show();
                        locationDetect();
                    } else {
                        Snackbar.make(view, "Permission Denied, You cannot access location.", Snackbar.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access the location permission",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION},
                                                            PERMISSION_LOCATION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MapsActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    public void locationDetect() {
        if (locationTrack.canGetLocation()) {
            progressBar.setCancelable(true);
            progressBar.setMessage("Please wait...");
            progressBar.show();
            gpsLongitude = locationTrack.getLongitude();
            gpsLatitude = locationTrack.getLatitude();
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(gpsLongitude) + "\nLatitude:" + Double.toString(gpsLatitude), Toast.LENGTH_SHORT).show();

        } else {
            locationTrack.showSettingsAlert();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {
            apiLatitude = location.getLatitude();
            apiLongitude = location.getLongitude();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            apiLatitude = location.getLatitude();
            apiLongitude = location.getLongitude();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkLocationPermission()) {
                requestLocationPermission();
            }
        }
        if (!checkPlayServices()) {
            Toast.makeText(this, ("You need to install Google Play Services to use the App properly"), Toast.LENGTH_SHORT).show();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }




}