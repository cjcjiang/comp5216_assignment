package comp5216.sydney.edu.au.assignment;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private ArrayList<Note> arrayOfNotes;
    private ListView listView;
    private UsersAdapter adapter;
    private Button addNewButton;

    //private static final int REQUEST_READ_EXTERNAL_STORAGE = 601;
    //This code is used to get the permission of using camera, storage and location in this app from the user
    private static final int REQUEST_PERMISSION = 602;

    //Explicitly get the permission of using location
    private static final int REQUEST_ACCESS_FINE_LOCATION = 605;
    //Explicitly get the permission of using camera
    //private static final int REQUEST_CAMERA= 606;

    //Number to store the current location with latitude and longitude
    private double currentLatitude;
    private double currentLongitude;

    //This code is used for checking the usability of google services
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final String TAG = MainActivity.class.getSimpleName();
    //Store the location got from google api
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates;
    public static final String firstTimeRun = "firstTimeRun" ;
    public static final String enableLocationService = "enableLocationService" ;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    private LocationRequest mLocationRequest;


    //Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters


    //Initialize things used for facebook login
    CallbackManager callbackManager;
    private LoginButton loginButton;
    private AccessToken accessToken;
    private AccessTokenTracker accessTokenTracker;

    //Distance notify
    private int locationNotifyID;
    private NotificationManager notificationManager;
    private PendingIntent pIntent;
    private double notifyDistance = 50;

    //sort button
    private Button sortButton;
    private View inflate;
    private TextView sortbylocation;
    private TextView sortbycreationtime;
    private TextView sortbyname;
    private Dialog dialog;

    //Flag to know whether it is updating note or just adding new note
    private String editingStatusFlag = "Editing Status Flag";
    private String updatingNote = "Updating Note";
    private String addingNewNote = "Adding New Note";

    //This request code is for the Add New button
    private final int REQUEST_ADDING_CODE = 233;
    private final int REQUEST_UPDATING_CODE = 234;

    private AlarmManager alarmManager;

    private Switch switchLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Initialize facebook sdk
        FacebookSdk.sdkInitialize(getApplicationContext());

        //Use Fresco to load the image, so initialize Fresco here
        Fresco.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get the permission of using camera, storage and location in this app from the user
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION );
        }

        //Initialize the arrayList to store notes in the mainActivity
        arrayOfNotes = new ArrayList<Note>();

        //Get all of the notes from the database
        readNotesFromDatabase();

        Collections.reverse(arrayOfNotes);

        //mRequestingLocationUpdates = true;

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);

        editor = sharedpreferences.edit();

        mRequestingLocationUpdates = sharedpreferences.getBoolean(enableLocationService, false);

        if(!sharedpreferences.getBoolean(firstTimeRun, false)) {
            //If firstTimeRun do not exist in the shared preference
            // that means this is the first time to run the application
            // getBoolean() will return false, the following code will be executed
            //If firstTimeRun exist, getBoolean() will return the value of firstTimeRun
            mRequestingLocationUpdates = false;
            editor.putBoolean(firstTimeRun, true);
            editor.putBoolean(enableLocationService, false);
            editor.commit();
        }

        //Check the usability of google services
        if (checkPlayServices()) {
            // If google services can be used, build the GoogleApi client
            buildGoogleApiClient();
            //Update the  current location periodically
            createLocationRequest();
        }

        //Get the current location
        getCurrentLocation();

        // Create the adapter to convert the array to views
        adapter = new UsersAdapter(this, arrayOfNotes);
        //Attach the adapter to a ListView
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);

        //pop up the dialog when user long click
        setupListViewListener();

        //Clicking on this button will jump to editActivity and start adding new note
        addNewButton = (Button) findViewById(R.id.addNewButton);

        sortButton = (Button) findViewById(R.id.sort);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        if(!isMyServiceRunning(LocationService.class)){
            startService(new Intent(MainActivity.this, LocationService.class));
        }

        alarmManager = (AlarmManager)this.getSystemService(this.ALARM_SERVICE); //Please note that context is "this" if you are inside an Activity
        Intent alarmIntent = new Intent(this, AlertTime.class);
        PendingIntent event = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000*10, 1000*30, event);

        if (alarmManager!= null) {
            Log.d(TAG, "alarmManager success");
        }

        switchLocationService = (Switch) findViewById(R.id.switchLocationService);
        if(mRequestingLocationUpdates){
            switchLocationService.setChecked(true);
        }else{
            switchLocationService.setChecked(false);
        }

        setupSwitchDisplayOnCheckedChangeListener();

    }

    //When current location is changed, this method will be invoked
    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;
        if (mLastLocation != null) {
            currentLatitude = mLastLocation.getLatitude();
            currentLongitude = mLastLocation.getLongitude();
        }
        //Toast.makeText(getApplicationContext(), "Location changed!", Toast.LENGTH_SHORT).show();
    }


    //Used to check if the user have already login, if yes, return TRUE
    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    //When AddNew button is clicked, this method will be invoked
    public void addNewNote(View view) {
        // Jump to the EditActivity
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(editingStatusFlag, addingNewNote);
        startActivityForResult(intent, REQUEST_ADDING_CODE);
    }

    //Sort the notes with the distance between the location stored in the note and the current location
    public void sortNotes() {
        getCurrentLocation();
        ArrayList<NoteDistanceCompare> arrayOfNotesDistanceCompare = new ArrayList<NoteDistanceCompare>();
        Iterator<Note> itr = arrayOfNotes.iterator();
        //Get all of the notes out and put them in the NoteDistanceCompare object to make the comparison easier
        while (itr.hasNext()) {
            NoteDistanceCompare noteDisComp = new NoteDistanceCompare(itr.next(), currentLatitude, currentLongitude);
            arrayOfNotesDistanceCompare.add(noteDisComp);
        }

        Collections.sort(arrayOfNotesDistanceCompare, new Comparator<NoteDistanceCompare>() {
            @Override
            public int compare(NoteDistanceCompare noteDistanceCompare, NoteDistanceCompare t1) {
                //Calculate the distance and the difference
                double temp = noteDistanceCompare.getDistance() - t1.getDistance();
                if (temp>0){
                    //If noteDistanceCompare is far than t1, return 1
                    return 1;
                }
                if (temp<0){
                    //If t1 is far than noteDistanceCompare, return -1
                    return -1;
                }
                //If they have the same distance, return 0
                return 0;
            }
        });

        //Clear the notes showed in gridView first
        arrayOfNotes.clear();
        ArrayList<Note> arrayOfNotes2 = new ArrayList<Note>();
        //Put the sorted notes in the new arrayList
        Iterator<NoteDistanceCompare> itr2 = arrayOfNotesDistanceCompare.iterator();
        while (itr2.hasNext()) {
            Note note = itr2.next().getNote();
            arrayOfNotes2.add(note);
        }

        //Update the notes that will be showed in the gridView with sorted notes
        arrayOfNotes.addAll(arrayOfNotes2);
        //Show the changing of notes in the gridView
        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // To receive the new note transfered from the EditActivity
        if (requestCode == REQUEST_ADDING_CODE) {
            if(resultCode == RESULT_OK){
                // Extract the new note from the intent
                Note note = (Note)intent.getSerializableExtra("New Note");
                arrayOfNotes.add(note);
                //Save the new note to the database
                saveNotesToDatabase();
                //Refresh the listView to display the new note
                readNotesFromDatabase();
                Collections.reverse(arrayOfNotes);
                adapter.notifyDataSetChanged();
            }
        }

        if (requestCode == REQUEST_UPDATING_CODE) {
            if(resultCode == RESULT_OK ){
                // Extract the updated note from the intent
                Note updatedNote = (Note)intent.getSerializableExtra("New Note");
                int position = intent.getIntExtra("position", -1);
                arrayOfNotes.set(position, updatedNote);
                //Save the new note to the database
                saveNotesToDatabase();
                //Refresh the listView to display the new note
                readNotesFromDatabase();
                Collections.reverse(arrayOfNotes);
                adapter.notifyDataSetChanged();
            }
        }

        //Handle the callback of facebook
        //callbackManager.onActivityResult(requestCode, resultCode, intent);

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

    //save all the notes to the database
    private void saveNotesToDatabase() {
        Note.deleteAll(Note.class);
        for (Note note:arrayOfNotes){
            note.save();
        }
    }

    private void setupListViewListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Note updateNote = adapter.getItem(position);
                Log.i("MainActivity", "Clicked item " + position + ": " + updateNote.getTitle());
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                if (intent != null) {
                    // put "extras" into the bundle for access in the edit activity
                    intent.putExtra("updateNote", updateNote);
                    intent.putExtra("position", position);
                    intent.putExtra(editingStatusFlag, updatingNote);

                    // brings up the second activity
                    startActivityForResult(intent, REQUEST_UPDATING_CODE);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            //When user long click the note, show this dialog
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long rowId) {
                Log.i("MainActivity", "Long Clicked item " + position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.dialog_delete_title).setMessage(R.string.dialog_delete_msg)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Delete the note
                                arrayOfNotes.remove(position);
                                adapter.notifyDataSetChanged();
                                saveNotesToDatabase();
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //User cancelled the dialog
                        // nothing happens
                    }
                });
                builder.create().show();
                return true;
            }
        });
    }

    //Check if the google play services are available on this phone
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }

    //Build the google api client to get the location
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected()&& mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        if(!isMyServiceRunning(LocationService.class)){
            startService(new Intent(MainActivity.this, LocationService.class));
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        if(!mRequestingLocationUpdates){
            if(isMyServiceRunning(LocationService.class)){
                stopService(new Intent(MainActivity.this, LocationService.class));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopLocationUpdates();

        if(!mRequestingLocationUpdates){
            if(isMyServiceRunning(LocationService.class)){
                stopService(new Intent(MainActivity.this, LocationService.class));
            }
        }

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
    }

    //Start location update
    protected void startLocationUpdates() {
//        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            //未取得權限，向使用者要求允許權限
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION );
//        }
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch(SecurityException e){

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        getCurrentLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    private void getCurrentLocation() {

//        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            //Get the permission of the location
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION );
//        }
        try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }catch (SecurityException e){

        }

        if (mLastLocation != null) {
            currentLatitude = mLastLocation.getLatitude();
            currentLongitude = mLastLocation.getLongitude();
        }
    }

    public void sort(View view){
        dialog = new Dialog(this,R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate = LayoutInflater.from(this).inflate(R.layout.dialog_layout_sort, null);
        //初始化控件
        sortbylocation = (TextView) inflate.findViewById(R.id.text_sortbylocation);
        sortbycreationtime = (TextView) inflate.findViewById(R.id.text_sortbylasteditedtime);
        //sortbyname = (TextView) inflate.findViewById(R.id.text_sortbyname) ;
        sortbylocation.setOnClickListener(this);
        sortbycreationtime.setOnClickListener(this);
        //sortbyname.setOnClickListener(this);
        //将布局设置给Dialog
        dialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity( Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 20;
        //设置Dialog距离底部的距离
        //将属性设置给窗体
        dialogWindow.setAttributes(lp);
        dialog.show();//显示对话框
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.text_sortbylocation:
                sortNotes();
                Toast.makeText(this,"sort by location",Toast.LENGTH_SHORT).show();
                break;

            case R.id.text_sortbylasteditedtime:
                readNotesFromDatabase();
                Collections.reverse(arrayOfNotes);
                adapter.notifyDataSetChanged();
                Toast.makeText(this,"sort by last edited time",Toast.LENGTH_SHORT).show();
                break;



        }
        dialog.dismiss();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void facebookLogOut(View view){
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(this, fblogin.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();//停止当前的Activity,如果不写,则按返回键会跳转回原来的Activity

    }

    private void setupSwitchDisplayOnCheckedChangeListener(){
        switchLocationService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked){
                    mRequestingLocationUpdates = true;
                    editor.putBoolean(enableLocationService, true);
                    editor.commit();
                    if(!isMyServiceRunning(LocationService.class)){
                        startService(new Intent(MainActivity.this, LocationService.class));
                    }
                }else{
                    mRequestingLocationUpdates = false;
                    editor.putBoolean(enableLocationService, false);
                    editor.commit();
                    if(isMyServiceRunning(LocationService.class)){
                        stopService(new Intent(MainActivity.this, LocationService.class));
                    }
                }

            }
        });
    }


}



