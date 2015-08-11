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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.alexbbb.uploadservice.UploadService;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyViewActivity extends AppCompatActivity {

    private Uri mImageUri;
    private LatLng position;
    private long heading;
    private LocationManager locationManager;
    static final int PICTURE_REQUEST_CODE = 1;


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
		//checkForUnsaved();
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
            determineLocationGps();
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
        locationManager.removeUpdates(locationListener);
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
        //checkForUnsaved();
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
            determineLocationGps();//Grab location and heading
		} else {
			finish();//Return to previous activity
		}
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
            determineLocationGps();//Get position from GPS
        }
    }

    private void determineLocationGps(){
        //Check for stale location data first
        String locationProvider = LocationManager.GPS_PROVIDER;
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if(lastKnownLocation != null &&
            lastKnownLocation.getTime() > new Date().getTime() - (1000 * 45)//Less than 45 seconds old
            && lastKnownLocation.getAccuracy() < 50//Accuracy
        ){
            //Log.d("Accurary", "" + lastKnownLocation.getAccuracy());
            position = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        } else {
            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
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
