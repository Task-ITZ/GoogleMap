package com.example.googleMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_CODE = 100;

    private GoogleMap gMap;
    private static int PERMISSION_REQUEST_CODE=12;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentMarker;
    private PlacesClient placesClient;
    private Bookmark bookmark;
    private BookmarkRepository bookmarkRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Places.initialize(getApplicationContext(), "AIzaSyCZbFUXnFOuho5pSs6rN-uNU_k6R7pocYA");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        bookmarkRepository = new BookmarkRepository(getApplication());
        checkRuntimePermission();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        mapFragment.getMapAsync(this);
    }

    private void checkRuntimePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        loadSavedBookmarks();

        gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout infoWindowLayout = new LinearLayout(MainActivity.this);
                infoWindowLayout.setOrientation(LinearLayout.HORIZONTAL);

                ImageView imageView = new ImageView(MainActivity.this);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                infoWindowLayout.addView(imageView);

                if (marker.getTag() != null) {
                    Bitmap imageBitmap = (Bitmap) marker.getTag();
                    imageView.setImageBitmap(imageBitmap);
                }

                TextView title = new TextView(MainActivity.this);
                title.setText(marker.getTitle());
                title.setTextColor(Color.BLACK);
                title.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT){{
                    setMargins(10, 20, 0, 0);
                }});
                infoWindowLayout.addView(title);

                TextView phone = new TextView(MainActivity.this);
                return infoWindowLayout;
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
            getCurrentLocation();
            gMap.setOnPoiClickListener(this::onPoiClick);
            gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(@NonNull Marker marker) {
                    Long id = bookmarkRepository.addBookmark(bookmark);
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra("mark_id", id);
                    startActivity(intent);
                }
            });
            gMap.setOnMarkerClickListener(this::onClickMarker);
        }
    }

    private boolean onClickMarker(Marker marker) {
        String bookmarkStr = marker.getSnippet();
        if(bookmarkStr != null){
            Long bookmarkId = Long.parseLong(bookmarkStr);
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("mark_id", bookmarkId);
            startActivity(intent);
        }
        return false;
    }

    private void onPoiClick(PointOfInterest poi) {
        Toast.makeText(this, "Bạn đã chọn: " + poi.name, Toast.LENGTH_SHORT).show();

        placesClient = Places.createClient(this);
        List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.PHOTO_METADATAS, Place.Field.PHONE_NUMBER, Place.Field.ADDRESS);
        FetchPlaceRequest request = FetchPlaceRequest.builder(poi.placeId, fields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            bookmark = new Bookmark(null, poi.placeId, poi.name, place.getAddress(), poi.latLng.latitude, poi.latLng.longitude, place.getPhoneNumber(),"");
            if (place != null) {
                if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                    PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
                    FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                            .setMaxWidth(500)
                            .setMaxHeight(500)
                            .build();

                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener(fetchPhotoResponse -> {
                        Bitmap bitmap = fetchPhotoResponse.getBitmap();
                        bookmark.setImage(bitmap, this);
                        if (currentMarker != null) {
                            currentMarker.remove();
                        }
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(poi.latLng.latitude, poi.latLng.longitude))
                                .title(poi.name);


                        currentMarker = gMap.addMarker(markerOptions);
                        currentMarker.setTag(bitmap);
                        currentMarker.showInfoWindow();
                    }).addOnFailureListener(exception -> {
                        Toast.makeText(this, "Không thể lấy ảnh", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(this, "Không có ảnh cho địa điểm này", Toast.LENGTH_SHORT).show();
                    if (currentMarker != null) {
                        currentMarker.remove();
                    }
                }
            } else {
                Toast.makeText(this, "Không thể lấy thông tin địa điểm", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(exception -> {
            Toast.makeText(this, "Không thể lấy thông tin địa điểm", Toast.LENGTH_SHORT).show();
        });
    }



    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        gMap.addMarker(new MarkerOptions().position(currentLatLng).title("Vị trí của tôi"));
                        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    }
                }
            });
        }
    }
    private void loadSavedBookmarks() {
        bookmarkRepository.allBookmarks().observe(this, bookmarks -> {
            if (bookmarks != null) {
                gMap.clear();

                getCurrentLocation();
                for (Bookmark bookmark : bookmarks) {
                    LatLng location = new LatLng(bookmark.latitude, bookmark.longitude);
                    gMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(bookmark.name)
                            .snippet(String.valueOf(bookmark.id))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadSavedBookmarks();
        }
    }
}