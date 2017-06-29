package comp5216.sydney.edu.au.assignment;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by JIANG on 2016/10/11.
 */

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "LocationService";

    private Location mLastLocation;
    private double newLatitude;
    private double newLongitude;
    private double currentLatitude;
    private double currentLongitude;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    //Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    private int locationNotifyID;
    private NotificationManager notificationManager;
    private PendingIntent pIntent;
    private double notifyDistance = 50;
    private ArrayList<Note> arrayOfNotes;

    //Flag to know whether it is updating note or just adding new note
    private String editingStatusFlag = "Editing Status Flag";
    private String updatingNote = "Updating Note";
    private final int REQUEST_UPDATING_CODE = 234;

    public LocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        arrayOfNotes = new ArrayList<Note>();
        readNotesFromDatabase();

        //Check the usability of google services
        if (checkPlayServices()) {
            // If google services can be used, build the GoogleApi client
            buildGoogleApiClient();
            //Update the  current location periodically
            createLocationRequest();
        }

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        Log.i(TAG, "Service started by LocationService for 5216 AS");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
            }
            return false;
        }
        Log.d(TAG, "checkPlayServices() success");
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        Log.d(TAG, "buildGoogleApiClient()");
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
        Log.d(TAG, "createLocationRequest()");
    }

    protected void startLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.d(TAG, "startLocationUpdates() success");
        }catch(Exception e){
            Log.d(TAG, "startLocationUpdates() failed");
        }

    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.i(TAG, "Google Api connected");
        getCurrentLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Log.i(TAG, "Google Api connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;
        newLatitude = mLastLocation.getLatitude();
        newLongitude = mLastLocation.getLongitude();
        double changeDistance = calDistance(currentLatitude, currentLongitude, newLatitude, newLongitude);
        Log.d(TAG, "new latitude: " + newLatitude + ", new longitude: " + newLongitude);
        Log.d(TAG, "current latitude: " + currentLatitude + ", current longitude: " + currentLongitude);
        Log.d(TAG, "changeDistance: " + changeDistance + " meters");
        if ((mLastLocation != null) && (newLatitude != currentLatitude || newLongitude != currentLongitude) && (changeDistance > 100)) {
            currentLatitude = mLastLocation.getLatitude();
            currentLongitude = mLastLocation.getLongitude();
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Intent intent = new Intent(this, MainActivity.class);
            pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
            Iterator<Note> itr = arrayOfNotes.iterator();
            //Initial the location notify id
            locationNotifyID = 275;
            //Get all of the notes out and put them in the NoteDistanceCompare object
            //If the distance stored in NoteDistanceCompare is smaller than notifyDistance
            //this means that the user is near the creation location of the note
            // this note will be notified
            while (itr.hasNext()) {
                NoteDistanceCompare noteDisComp = new NoteDistanceCompare(itr.next(), currentLatitude, currentLongitude);
                if(noteDisComp.getDistance()<notifyDistance){
                    String notifyTitle = noteDisComp.getNote().getTitle();
                    String notifyMessage = noteDisComp.getNote().getMessage();
                    Notification n  = new Notification.Builder(this)
                            .setContentTitle(notifyTitle)
                            .setContentText(notifyMessage)
                            .setSmallIcon(R.drawable.notificationicon)
                            .setContentIntent(pIntent)
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setPriority(Notification.PRIORITY_MAX)
                            .build();
                    notificationManager.notify(locationNotifyID, n);
                    locationNotifyID = locationNotifyID + 1;
                }
            }
            locationNotifyID = 275;

            Log.i(TAG, "Location updated");
        }
    }

    //read saved notes from the database
    private void readNotesFromDatabase() {
        List<Note> notesFromORM = Note.listAll(Note.class);
        ArrayList<Note> arrayOfNotes2 = new ArrayList<Note>();
        //arrayOfNotes = new ArrayList<Note>();
        arrayOfNotes.clear();
        if (notesFromORM != null & notesFromORM.size() > 0) {
            for (Note note : notesFromORM) {
                arrayOfNotes2.add(note);
            }
        }
        arrayOfNotes.addAll(arrayOfNotes2);
    }

    private void getCurrentLocation() {
        try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }catch(SecurityException e){

        }

        if (mLastLocation != null) {
            currentLatitude = mLastLocation.getLatitude();
            currentLongitude = mLastLocation.getLongitude();
        }
    }

    //Calculate the the distance from the current location to the new location *in meters*
    private double calDistance(double currentLatitude, double currentLongitude, double newLatitude, double newLongitude){
        double x,y,distance;
        double R = 6371229;
        x=(newLongitude-currentLongitude)*Math.PI*R*Math.cos( ((currentLatitude+newLatitude)/2) *Math.PI/180)/180;
        y=(newLatitude-currentLatitude)*Math.PI*R/180;
        distance=Math.hypot(x,y);
        return distance;
    }


}
