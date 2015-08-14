package uk.co.threeequals.ratemyview;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.alexbbb.uploadservice.UploadService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyViewActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{
    String TAG = "MyViewActivity";

    private Uri mImageUri;
    private LatLng position;
    private long heading;
    private LocationManager locationManager;
    static final int PICTURE_REQUEST_CODE = 1;
    protected GoogleApiClient mGoogleApiClient;


    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		UploadService.NAMESPACE = getString(R.string.upload_namespace);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		setContentView(R.layout.activity_my_view);
		heading = 0;
		mImageUri = null;

        if(savedInstanceState == null || savedInstanceState.getString("image") == null) {//Check if null
            dispatchTakePictureIntent();
        }

        buildGoogleApiClient();
	}

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		//Compass
		savedInstanceState.putLong("heading", heading);

        if(position != null) {
            savedInstanceState.putDouble("lat", position.latitude);
            savedInstanceState.getDouble("lng", position.longitude);
        }

		//image file
		if(mImageUri != null){
			savedInstanceState.putString("image", mImageUri.toString());
		}
	}
	
	@Override
	public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState.containsKey("heading")){//Check if null
            heading = savedInstanceState.getLong("heading");
        }

        if(savedInstanceState.getString("image") != null){//Check if null
            mImageUri = Uri.parse(savedInstanceState.getString("image"));
            displayImage();//Re-create image
        }

        if(savedInstanceState.containsKey("lat")){//Check if null
            position = new LatLng(
                savedInstanceState.getDouble("lat"),
                savedInstanceState.getDouble("lng")
            );
        } else {
            //determineLocationGps();
        }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.myviewmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                send();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onPause() {
		super.onPause();

        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
	}
	
	@Override
	public void onResume() {
		super.onResume();
        if (mGoogleApiClient.isConnected()){
            startLocationUpdates();
        }
	}

	
	public Bitmap getResizedBitmap(Bitmap bm, float newHeight, float newWidth) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    
	    if (width > height) {//Resize proportionally
	    	newHeight = height * (newWidth / width);
	    } else {
	    	newWidth = width * (newHeight / height);
	    }
	    

	    float scaleWidth = ( newWidth) / width;
	    float scaleHeight = ( newHeight) / height;
	    // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
	    // RESIZE THE BITMAP
	    matrix.postScale(scaleWidth, scaleHeight);

	    // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	}
	
	private File createTemporaryFile(String part, String ext) throws Exception{
	    File tempDir = Environment.getExternalStorageDirectory();
	    tempDir = new File(tempDir.getAbsolutePath()+"/.temp/");
	    if(!tempDir.exists())
	    {
	        tempDir.mkdir();
	    }
	    return File.createTempFile(part, ext, tempDir);
	}

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, PICTURE_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_RmV_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mImageUri = Uri.parse("file:" + image.getAbsolutePath());
        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mImageUri.getPath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
	
	protected void onActivityResult (int requestCode, int resultCode, Intent data){
		if(requestCode==PICTURE_REQUEST_CODE && resultCode==RESULT_OK){
			resizeImage();
            galleryAddPic();
            startLocationUpdates();
		} else {
			finish();//Return to previous activity
		}
	}

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if(mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
	
	public void displayImage(){
	    ContentResolver cr = this.getContentResolver();
	    cr.notifyChange(mImageUri, null);
	    Bitmap mImageBitmap;
    	ImageView mImageView = (ImageView) findViewById(R.id.previewImage);
    	try {
			mImageBitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
	    	mImageView.setImageBitmap(mImageBitmap);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void resizeImage(){
	    ContentResolver cr = this.getContentResolver();
	    cr.notifyChange(mImageUri, null);
	    Bitmap mImageBitmap;
	    try {
	    	mImageBitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
	    	Bitmap resizedBitmap = getResizedBitmap(mImageBitmap, 1000, 1000);

			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			resizedBitmap.compress(CompressFormat.JPEG, 100, baos);

			byte[] byteArray = baos.toByteArray();

			//Update the saved file to be smaller
			FileOutputStream fos = new FileOutputStream(mImageUri.getPath());
			fos.write(byteArray);
			fos.close();
			displayImage();//Show the image

	    } catch (Exception e) {
	        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
	        //Log.d("Rmv", e.getMessage(), e);
	    } catch(java.lang.Throwable e){
            //Log.d("Rmv", e.getMessage(), e);
            Toast.makeText(getBaseContext(),"Photo not available",Toast.LENGTH_SHORT).show();
	    }
	}

    //TODO - fix location exif gathering
    private void determineLocationExif(){
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(mImageUri.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        float[] latLong = new float[2];
        if (exif != null && exif.getLatLong(latLong)) {
            //Log.d("Location", "Location set by Photo");
            position = new LatLng(latLong[0], latLong[1]);
        } else {
            //determineLocationGps();//Get position from GPS
        }
    }

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            if(location.getAccuracy() < 50){
                //Log.d("Location", "Location set by GPS Listener");
                //Log.d("Accurary", "" + location.getAccuracy());
                position = new LatLng(location.getLatitude(), location.getLongitude());
                if(location.getAccuracy() < 10)
                    locationManager.removeUpdates(this);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            //updateUI();
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        //if (mRequestingLocationUpdates) {
            startLocationUpdates();
        //}
    }


    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        position = new LatLng(location.getLatitude(), location.getLongitude());
        if(location.getAccuracy() < 10) {
            stopLocationUpdates();
        }
        //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        //updateUI();
        //Toast.makeText(this, getResources().getString(R.string.location_updated_message),
        //        Toast.LENGTH_SHORT).show();
        Log.d(TAG, location.toString());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

	//When the send button is clicked
	public void send(){
		//make message text field object
    	EditText commentsTextField = (EditText) findViewById(R.id.comments);
        String comments = commentsTextField.getText().toString();

    	EditText wordOneTextField = (EditText) findViewById(R.id.wordOne);
        String  wordOne = wordOneTextField.getText().toString();
        
        if(wordOne.length()<1) {//display message if text field is empty
            Toast.makeText(getBaseContext(),"Word one is required",Toast.LENGTH_LONG).show();
            return;
        }
        
    	EditText wordTwoTextField = (EditText) findViewById(R.id.wordTwo);
        String  wordTwo = wordTwoTextField.getText().toString();
        
        if(wordTwo.length()<1) {//display message if text field is empty
            Toast.makeText(getBaseContext(),"Word two is required",Toast.LENGTH_LONG).show();
            return;
        }
        
    	EditText wordThreeTextField = (EditText) findViewById(R.id.wordThree);
        String  wordThree = wordThreeTextField.getText().toString();
        
        if(wordThree.length()<1) {//display message if text field is empty
            Toast.makeText(getBaseContext(),"Word three is required",Toast.LENGTH_LONG).show();
            return;
        }
        
        Spinner ageSpinnerField = (Spinner) findViewById(R.id.ageSpinner);
        String  age = (String) ageSpinnerField.getSelectedItem();
        if(ageSpinnerField.getSelectedItemPosition() == 0){
            Toast.makeText(getBaseContext(),"Please select your age",Toast.LENGTH_LONG).show();
            return;
        }

        Spinner knowSpinnerField = (Spinner) findViewById(R.id.knowSpinner);
        String  know = (String) knowSpinnerField.getSelectedItem();
        if(knowSpinnerField.getSelectedItemPosition() == 0){
            Toast.makeText(getBaseContext(),"Please select how well you know you know this location",Toast.LENGTH_LONG).show();
            return;
        }
        
        RatingBar ratingField = (RatingBar) findViewById(R.id.rating);
        int  rating = (int) ratingField.getRating();
        
    	String imageLoc;
		imageLoc = mImageUri.getPath();
		
		//ensure position has been found
		if(position == null){
            Toast.makeText(getBaseContext(),"Location could not be acquired",Toast.LENGTH_SHORT).show();
            return;
		}

        if(mCurrentLocation.getAccuracy() > 50){
            //Warn the user they're uploading vague data
        }

        RmVOverlayItem rmvOverlayItem = new RmVOverlayItem();
        rmvOverlayItem.setPosition(position);
        rmvOverlayItem.setComments(comments);

        rmvOverlayItem.setWords1(wordOne);
        rmvOverlayItem.setWords2(wordTwo);
        rmvOverlayItem.setWords3(wordThree);

        rmvOverlayItem.setAge(age);
        rmvOverlayItem.setKnow(know);
        rmvOverlayItem.setRating(rating);
        rmvOverlayItem.setPhotoLocation(imageLoc);

        rmvOverlayItem.setTs(new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(new Date()));
        rmvOverlayItem.setTime(new SimpleDateFormat("hh:mm", Locale.ENGLISH).format(new Date()));
        
        rmvOverlayItem.setHeading(heading);

        String nonce = java.util.UUID.randomUUID().toString();
        rmvOverlayItem.setNonce(nonce);

        rmvOverlayItem.save();//Save the upload incase it doesn't upload correctly
        UploadManager.upload(getApplicationContext(), rmvOverlayItem);
        finish();
    }
}
