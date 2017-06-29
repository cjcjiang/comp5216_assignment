package comp5216.sydney.edu.au.assignment;

import android.app.Fragment;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude;
    private double longitude;


    private TextView latitudeTextView;
    private TextView longitudeTextView;

    private Marker userMarker;

    private final int REQUEST_CANCEL_CODE = 25;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        latitudeTextView = (TextView) findViewById(R.id.latitudeTextView);
        longitudeTextView = (TextView) findViewById(R.id.longitudeTextView);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 52.204296);
        longitude = intent.getDoubleExtra("longitude", 0.114767);


        String strLat = "" + latitude;
        String strLon = "" + longitude;
        latitudeTextView.setText("Latitude: " + strLat.format("%.2f",latitude));
        longitudeTextView.setText("Longitude: " + strLon.format("%.2f",longitude));

        // Add a marker in Sydney and move the camera
        LatLng userLocation = new LatLng(latitude, longitude);
        userMarker = mMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .draggable(true)
                .title("user location"));

        // Move the camera instantly to location with a zoom of 15.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
                Toast.makeText(MapsActivity.this, "Dragging Start",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng position = marker.getPosition();
                latitude = position.latitude;
                longitude = position.longitude;
                String strLat = "" + latitude;
                String strLon = "" + longitude;
                latitudeTextView.setText("Latitude: " + strLat.format("%.2f",latitude));
                longitudeTextView.setText("Longitude: " + strLon.format("%.2f",longitude));
                Toast.makeText(
                        MapsActivity.this,
                        "Lat " + strLat.format("%.2f",latitude) + "\n"
                                + "Long " + strLon.format("%.2f",longitude),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                String strLat = "" + latitude;
                String strLon = "" + longitude;
                latitudeTextView.setText("Latitude: " + strLat.format("%.2f",latitude));
                longitudeTextView.setText("Longitude: " + strLon.format("%.2f",longitude));
                userMarker.remove();
                LatLng userLocation = new LatLng(latitude, longitude);
                userMarker = mMap.addMarker(new MarkerOptions()
                        .position(userLocation)
                        .draggable(true)
                        .title("user location"));
            }
        });

    }

    public void saveLocation(View view){
        Intent intent = new Intent();
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        setResult(RESULT_OK, intent);
        finish();
    }


}
