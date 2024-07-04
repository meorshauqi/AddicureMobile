package com.example.medmap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class PharmaciesActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private EditText locationInput;
    private Button searchButton;

    private HashMap<Marker, String> markerDetailsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacies);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationInput = findViewById(R.id.location_input);
        searchButton = findViewById(R.id.search_button);

        searchButton.setOnClickListener(v -> {
            String location = locationInput.getText().toString();
            if (!location.isEmpty()) {
                searchLocation(location);
            } else {
                Toast.makeText(PharmaciesActivity.this, "Please enter a location", Toast.LENGTH_SHORT).show();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable the my-location layer and control on the map.
        enableMyLocation();

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(marker -> {
            String details = markerDetailsMap.get(marker);
            if (details != null) {
                Toast.makeText(PharmaciesActivity.this, details, Toast.LENGTH_LONG).show();
            }
            return false;
        });
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            getDeviceLocation();
        }
    }

    private void getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                // Get the current location of the device and set the position of the map.
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                                mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));

                                // Find nearby pharmacies
                                findNearbyPharmacies(currentLocation);
                            } else {
                                Log.e("PharmaciesActivity", "Current location is null");
                                Toast.makeText(PharmaciesActivity.this, "Unable to determine current location", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("PharmaciesActivity", "Error trying to get last location", e);
                        });
            }
        } catch (SecurityException e) {
            Log.e("PharmaciesActivity", "SecurityException: " + e.getMessage());
        }
    }

    private void findNearbyPharmacies(LatLng location) {
        String apiKey = "AIzaSyBJ332D05Oy5x2thXl3fvBMn1QrHJvWpH8";
        String locationStr = location.latitude + "," + location.longitude;
        int radius = 5000; // 5 km radius
        String type = "pharmacy";
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + locationStr +
                "&radius=" + radius + "&type=" + type + "&key=" + apiKey;

        new GetNearbyPlaces().execute(url);
    }

    private void searchLocation(String location) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(location, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                findNearbyPharmacies(latLng);
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }

    private class GetNearbyPlaces extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpHandler httpHandler = new HttpHandler();
            return httpHandler.makeServiceCall(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray results = jsonObject.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject place = results.getJSONObject(i);
                    String name = place.getString("name");
                    String address = place.getString("vicinity");
                    JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");

                    LatLng latLng = new LatLng(lat, lng);
                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(name));
                    markerDetailsMap.put(marker, "Name: " + name + "\nAddress: " + address);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
