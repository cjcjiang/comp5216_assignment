package comp5216.sydney.edu.au.assignment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EditActivity extends FragmentActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnTouchListener {

    public final String APP_TAG = "NoteLAB";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int PICK_PHOTO_CODE = 1046;

    private static final String TAG = EditActivity.class.getSimpleName();
    private static final int REQUEST_ACCESS_FINE_LOCATION = 605;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;

    public String photoFileName = "photo";

    EditText titleEditText;
    EditText messageEditText;
    TextView timeTextView;
    ImageView ivPreview;
    Calendar timeOfCreation;
    TextView etSetTime;


    TextView locationTextView;
    //EditText setLocationEditView;

    String title;
    String time;
    String message;
    double latitude;
    double longitude;
    String imageURI = "http://www.free-icons-download.net/images/notebook-icon-44654.png";

    private String alertTime;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int min;

    private View inflate;
    private TextView choosePhoto;
    private TextView takePhoto;
    private Dialog dialog;

    //Flag to know whether it is updating note or just adding new note
    private String realEditingStatusFlag;
    private String editingStatusFlag = "Editing Status Flag";
    private String updatingNote = "Updating Note";
    private String addingNewNote = "Adding New Note";

    public int position=0;

    private int REQUEST_MAP_CODE = 866;

    private Bitmap shareImage;
    private ShareButton fbShareButton;
    private SharePhoto sharePhoto;
    private SharePhotoContent content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        titleEditText = (EditText)findViewById(R.id.textView_title);
        messageEditText = (EditText)findViewById(R.id.editText_message);
        timeTextView = (TextView) findViewById(R.id.textView_time);
        locationTextView = (TextView) findViewById(R.id.textView_location);
        //setLocationEditView = (EditText) findViewById(R.id.editText_setLocation);

        locationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditActivity.this, MapsActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivityForResult(intent, REQUEST_MAP_CODE);
            }
        });

        ivPreview = (ImageView) findViewById(R.id.imageView);
        //set time
        etSetTime = (TextView)findViewById(R.id.et_set_time);
        etSetTime.setOnTouchListener(this);
        //Check if it is adding or updating
        Intent intent = getIntent();
        realEditingStatusFlag = intent.getStringExtra(editingStatusFlag);
        //If this is to add new note
        if(addingNewNote.equals(realEditingStatusFlag)){
            //Use calendar to get the current system time
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            timeOfCreation = Calendar.getInstance();
            time = f.format(timeOfCreation.getTime());
            //Change the text in the textView to the current system time
            timeTextView.setText(time);

            if (checkPlayServices()) {

                // Building the GoogleApi client
                buildGoogleApiClient();
            }
            displayLocation();
        }

        //If this is to update the note
        if(updatingNote.equals(realEditingStatusFlag)){
            Note updateNote = (Note) intent.getSerializableExtra("updateNote");
            position = intent.getIntExtra("position",-1);
            timeTextView.setText(updateNote.getTime());
            etSetTime.setText(updateNote.getAlertTime());
            titleEditText.setText(updateNote.getTitle());
            messageEditText.setText(updateNote.getMessage());
            latitude = updateNote.getLatitude();
            longitude = updateNote.getLongitude();

            String strLat = "" + latitude;
            String strLon = "" + longitude;
            locationTextView.setText("Latitude:" + strLat.format("%.2f",latitude) + ", Longitude:" + strLon.format("%.2f",longitude));
            //setLocationEditView.setText("Latitude:" + strLat.format("%.2f",latitude) + ", Longitude:" + strLon.format("%.2f",longitude));
            //Try to display the image
            imageURI = updateNote.getImageURI();
            Uri photoUri = Uri.parse(updateNote.getImageURI());
            // Do something with the photo based on Uri
            Bitmap selectedImage;
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                // Load the selected image into a preview

                ivPreview.setImageBitmap(selectedImage);
                //ivPreview.setVisibility(View.VISIBLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Share with facebook
        fbShareButton = (ShareButton) findViewById(R.id.button_share);
//        ShareLinkContent content = new ShareLinkContent.Builder()
//                .setContentUrl(Uri.parse("https://developers.facebook.com"))
//                .build();

//        Uri sharePhotoUri = getFileUri(imageURI);
//        shareImage = BitmapFactory.decodeFile(sharePhotoUri
//                .getPath());
//        Log.d(TAG, "share button uri: " + sharePhotoUri.getPath());
//        sharePhoto = new SharePhoto.Builder()
//                .setBitmap(shareImage)
//                .build();
//        content = new SharePhotoContent.Builder()
//                .addPhoto(sharePhoto)
//                .build();

        String fbShareTitle = titleEditText.getText().toString();
        String fbShareDescription = messageEditText.getText().toString();
        if(fbShareTitle.isEmpty()){
            fbShareTitle = "No title";
        }
        if(fbShareDescription.isEmpty()){
            fbShareDescription = "No message";
        }
        Uri myUri = Uri.parse("http://www.free-icons-download.net/images/notebook-icon-44654.png");

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentTitle(fbShareTitle)
                .setContentDescription(fbShareDescription)
                .setImageUrl(myUri)
                .setContentUrl(Uri.parse("http://sydney.edu.au/"))
                .build();

        fbShareButton.setShareContent(content);

    }

    public void show(View view){
        dialog = new Dialog(this,R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null);
        //初始化控件
        choosePhoto = (TextView) inflate.findViewById(R.id.choosePhoto);
        takePhoto = (TextView) inflate.findViewById(R.id.takePhoto);
        choosePhoto.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        //将布局设置给Dialog
        dialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity( Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 20;//设置Dialog距离底部的距离
//       将属性设置给窗体
        dialogWindow.setAttributes(lp);
        dialog.show();//显示对话框
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.takePhoto:
                onTakePhotoClick();
                break;
            case R.id.choosePhoto:
                Toast.makeText(this,"点击了从相册选择",Toast.LENGTH_SHORT).show();
                onLoadPhotoClick();
                break;
        }
        dialog.dismiss();
    }

    //When user click on the takePhoto button, this method will be invoked
    public void onTakePhotoClick() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String photoDir = photoFileName + time + ".jpg";
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getFileUri(photoDir)); // set file name

        // Start the image capture intent to take photo
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    //When the user click on the loadPhoto button, this method will be invoked
    public void onLoadPhotoClick() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Bring up gallery to select a photo
        startActivityForResult(intent, PICK_PHOTO_CODE);
    }

    //Get uri of the photo
    public Uri getFileUri(String fileName) {
        // Get safe storage directory for photos
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator
                + fileName));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String photoDir = photoFileName + time + ".jpg";
                Uri takenPhotoUri = getFileUri(photoDir);

                //Preparing the uri of the takenPhoto for storing in the note
                imageURI = takenPhotoUri.toString();

                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri
                        .getPath());
                // Load the taken image into a preview
                ivPreview.setImageBitmap(takenImage);

                shareImage = takenImage;
                Log.d(TAG, "share image uri: " + takenPhotoUri.getPath());

                //ivPreview.setVisibility(View.VISIBLE);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!",
                        Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == PICK_PHOTO_CODE) {
            if (resultCode == RESULT_OK) {
                Uri photoUri = data.getData();

                //Preparing the uri of the loadPhoto for storing in the note
                imageURI = photoUri.toString();

                // Do something with the photo based on Uri
                Bitmap selectedImage;
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(
                            this.getContentResolver(), photoUri);
                    // Load the selected image into a preview

                    ivPreview.setImageBitmap(selectedImage);

                    shareImage = selectedImage;
                    Log.d(TAG, "share image uri: " + photoUri.getPath());

                    //ivPreview.setVisibility(View.VISIBLE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == REQUEST_MAP_CODE) {
            if(resultCode == RESULT_OK ){
                //if no location stored in this intent, set it as 52, 0.1
                latitude = data.getDoubleExtra("latitude", 52.204296);
                longitude = data.getDoubleExtra("longitude", 0.114767);
                String strLat = "" + latitude;
                String strLon = "" + longitude;
                locationTextView.setText("Latitude:" + strLat.format("%.2f",latitude) + ", Longitude:" + strLon.format("%.2f",longitude));
                //setLocationEditView.setText("Latitude:" + strLat.format("%.2f",latitude) + ", Longitude:" + strLon.format("%.2f",longitude));
                //locationTextView.setText("Latitude: " + latitude + ", Longitude: " + longitude);
            }
        }
    }

    //Save the note, return the new note to main activity with intent
    public void saveNote(View view) {
        //After clicking the save button, transfer the new Note back to the MainActivity
        title = titleEditText.getText().toString();
        time = timeTextView.getText().toString();
        alertTime = etSetTime.getText().toString();
        message = messageEditText.getText().toString();
        //If the edit text is empty, give the text a default value
        if(title.isEmpty()){
            title = "No title";
        }
        if(message.isEmpty()){
            message = "No message";
        }

        if(alertTime.isEmpty()){
            alertTime = "No Alert Time";
        }

        Note note = new Note(title, time, alertTime, message, imageURI, latitude, longitude);
        Intent intent = new Intent();
        intent.putExtra("New Note", note);
        intent.putExtra("position", position);
        setResult(RESULT_OK, intent);
        finish();

    }


    //Cancel the adding, and return to the mainActivity
    public void cancelAdd(View view) {
        //After clicking the cancel button, pop up a dialog to ask the user whether to give up this edit
        AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
        builder.setTitle(R.string.dialog_cancel_title).setMessage(R.string.dialog_cancel_msg)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Give up this edit
                        finish();

                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //User cancelled the dialog
                //Continue to edit the new note
            }
        });
        builder.create().show();

    }

    ////Check if the google play services are available on this phone
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

    //Display the location of the creation of the new note
    private void displayLocation() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //Get the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION );
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            String strLat = "" + latitude;
            String strLon = "" + longitude;
            //locationTextView.setText("Latitude:" + strLat.format("%.2f",latitude) + ", Longitude:" + strLon.format("%.2f",longitude));
            //setLocationEditView.setText("Latitude:" + strLat.format("%.2f",latitude) + ", Longitude:" + strLon.format("%.2f",longitude));


            //locationTextView.setText("Current Location"+"\n"+"Latitude:" + latitude + ", Longitude:" + longitude);
        } else {
            //locationTextView.setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
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
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    //SET TIME DIALOG

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = View.inflate(this, R.layout.date_time_dialog, null);
            final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
            final TimePicker timePicker = (android.widget.TimePicker) view.findViewById(R.id.time_picker);
            builder.setView(view);

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

            timePicker.setIs24HourView(true);
            if (Build.VERSION.SDK_INT >= 23 ){
                timePicker.setHour(cal.get(Calendar.HOUR_OF_DAY));
                timePicker.setMinute(Calendar.MINUTE);
            }
            else{
                timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(Calendar.MINUTE);
            }


            if (v.getId() == R.id.et_set_time) {
                final int inType = etSetTime.getInputType();
                etSetTime.setInputType(InputType.TYPE_NULL);
                etSetTime.onTouchEvent(event);
                etSetTime.setInputType(inType);
                //etSetTime.setSelection(etSetTime.getText().length());

                builder.setTitle("Select time");
                builder.setPositiveButton("ENTER", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        StringBuffer sb = new StringBuffer();
                        sb.append(String.format("%d-%02d-%02d",
                                datePicker.getYear(),
                                datePicker.getMonth() + 1,
                                datePicker.getDayOfMonth()));
                        sb.append("  ");
                        if (Build.VERSION.SDK_INT >= 23 ){
                            sb.append(timePicker.getHour())
                                    .append(":").append(timePicker.getMinute());
                        }
                        else{
                            sb.append(timePicker.getCurrentHour())
                                    .append(":").append(timePicker.getCurrentMinute());
                        }
                        String a = sb.toString();

                        alertTime = a;
                        etSetTime.setText(a);


                        dialog.cancel();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();//nothing happens
                    }
                });

            }


            Dialog dialog = builder.create();
            dialog.show();
        }

        return true;
    }


}






