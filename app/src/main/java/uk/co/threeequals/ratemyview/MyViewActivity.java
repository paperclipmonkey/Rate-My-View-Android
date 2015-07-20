package uk.co.threeequals.ratemyview;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

public class MyViewActivity extends AppCompatActivity {

	private long heading;
	private RmVOverlayItem rmvOverlayItem;
	
	private Uri mImageUri;
	private ProgressDialog progressDialog;
	private AlertDialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_my_view);
		heading = 0;
		progressDialog = null;
		mImageUri = null;

        if(savedInstanceState == null || savedInstanceState.getString("image") == null) {//Check if null
            dispatchTakePictureIntent(11);
        }
		//checkForUnsaved();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		//System.out.println("Saving instance state");
	  
		//Compass
		//if(savedInstanceState != null){
			savedInstanceState.putLong("heading", heading);
		//}
		
		//Coords
//		if(locationObj != null){
//			savedInstanceState.putLong("lat", (long) (locationObj.getLatitudeE6() * 1e6));
//			savedInstanceState.putLong("lng", (long) (locationObj.getLongitudeE6() * 1e6));
//		}
		
		//image file
		if(mImageUri != null){
			savedInstanceState.putString("image", mImageUri.toString());
		}

	  // Save UI state changes to the savedInstanceState.
	  // This bundle will be passed to onCreate if the process is
	  // killed and restarted.
//	  savedInstanceState.putBoolean("MyBoolean", true);
//	  savedInstanceState.putDouble("myDouble", 1.9);
//	  savedInstanceState.putInt("MyInt", 1);
//	  savedInstanceState.putString("MyString", "Welcome back to Android");
	  // etc.
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		//System.out.println("restoring instance state");
		
        if(savedInstanceState.containsKey("heading")){//Check if null
            heading = savedInstanceState.getLong("heading");
        }

        if(savedInstanceState.getString("image") != null){//Check if null
            mImageUri = Uri.parse(savedInstanceState.getString("image"));
            //Re-create image
            displayImage();
        }

        if(savedInstanceState.containsKey("lat")){//Check if null
//            locationObj = new GeoPoint(
//                savedInstanceState.getDouble("lat"),
//                savedInstanceState.getDouble("lng")
//            );
        }

		//Need to re-setup the image view
	  // Restore UI state from the savedInstanceState.
	  // This bundle has also been passed to onCreate.
//	  boolean myBoolean = savedInstanceState.getBoolean("MyBoolean");
//	  double myDouble = savedInstanceState.getDouble("myDouble");
//	  int myInt = savedInstanceState.getInt("MyInt");
//	  String myString = savedInstanceState.getString("MyString");
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
	}
	
	@Override
	public void onResume() {
		super.onResume();
        //checkForUnsaved();
	}
	
    protected void onStop(){
    	super.onStop();
    }
	
//	private void checkForUnsaved(){
//		//Check if there are any unsaved views
//		final List<ViewORM> unSaved = ViewORM.getUnsaved();
//		//System.out.println("DB id: " + unSaved.get(0).photoLocation);
//		//unSaved.get(0).delete();
//		if(unSaved.size() > 0){//Unsaved views to upload
//			//Show button
//			//Button uploadSaved = (Button) findViewById(R.id.saveUpload);
//			//uploadSaved.setVisibility(View.VISIBLE);//Turn on upload button
//			//uploadSaved.setText(uploadSaved.getText().toString().replace("*num*", "" + unSaved.size()));//Show number in button
//
//			CompoundButton.OnClickListener uploadSavedEvent = new CompoundButton.OnClickListener() {
//				@Override
//				public void onClick(View view) {
//					//Upload views
//			        //System.out.println(unSaved.get(0));
//			        //System.out.println("Lat: " + unSaved.get(0).lat);
//			        //System.out.println("Lng: " + unSaved.get(0).lng);
//			        rmvOverlayItem = new RmVOverlayItem(unSaved.get(0));
//			        rmvOverlayItem.dbId = unSaved.get(0).photoLocation;
//			        showUploadDialog();
//			        new PostViewTask().execute(rmvOverlayItem);
//			    }
//			};
//			//uploadSaved.setOnClickListener(uploadSavedEvent);
//		} else {
//			//Button uploadSaved = (Button) findViewById(R.id.saveUpload);
//			//uploadSaved.setVisibility(View.GONE);//Turn off upload button
//		}
//	}
	
	public Bitmap getResizedBitmap(Bitmap bm, float newHeight, float newWidth) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    
	    if (width > height) {//Resize proportionally
	    	newHeight = height * (newWidth / width);
	    } else {
	    	newWidth = width * (newHeight / height);
	    }
	    
	    //System.out.println(newWidth + " " + newHeight);
	    
	    float scaleWidth = ( newWidth) / width;
	    float scaleHeight = ( newHeight) / height;
	    // CREATE A MATRIX FOR THE MANIPULATION
	    Matrix matrix = new Matrix();
	    // RESIZE THE BIT MAP
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
	
	private void dispatchTakePictureIntent(int actionCode) {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    File photo;
	    try {
	        // place where to store camera taken picture
	        photo = this.createTemporaryFile("picture", ".jpg");
	        photo.delete();
	    } catch(Exception e) {
	        //Log.v(TAG, "Can't create file to take picture!");
	        Toast.makeText(getBaseContext(), "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG).show();
	        return;
	    }
	    mImageUri = Uri.fromFile(photo);
	    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
	    //start camera intent
	    startActivityForResult(takePictureIntent, actionCode);
	}
	
	protected void onActivityResult (int requestCode, int resultCode, Intent data){
		if(requestCode==11 && resultCode==RESULT_OK){
			this.resizeImage();
			//Grab location and heading
			//heading = (long) mMyLocationOverlay.getOrientation();
			//locationObj =  mMyLocationOverlay.getMyLocation();
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
	        System.out.println(e.getMessage());
	        System.out.println(e.getStackTrace());
	        //Log.d(TAG, "Failed to load", e);
	    } catch(java.lang.Throwable e){
	        System.out.println(e.getMessage());
	        System.out.println(e.getStackTrace());
            Toast.makeText(getBaseContext(),"Photo not available",Toast.LENGTH_SHORT).show();
	    }
	}
	
	public String getImageDataString(){
		try {
			InputStream inputStream = new FileInputStream(mImageUri.getPath());
			byte[] bytes;
			byte[] buffer = new byte[8192];
			int bytesRead;
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			while ((bytesRead = inputStream.read(buffer)) != -1) {
	            output.write(buffer, 0, bytesRead);
			}
	        bytes = output.toByteArray();
	        inputStream.close();
	        return Base64.encodeToString(bytes, Base64.DEFAULT);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//When the send button is clicked
    @SuppressLint("SimpleDateFormat")
	public void send(){
    	//get message from message box
    	
		//make message text field object
    	EditText commentsTextField = (EditText) findViewById(R.id.comments);
        String comments = commentsTextField.getText().toString();
        
        String imageStr = null;
        
        if(mImageUri != null){
        	imageStr = getImageDataString();
        }
        
        //Class variable used to store the image
        if(imageStr == null){
        	Toast.makeText(getBaseContext(),"Photo is required",Toast.LENGTH_SHORT).show();
            return;
        }
        
    	EditText wordOneTextField = (EditText) findViewById(R.id.wordOne);
        String  wordOne = wordOneTextField.getText().toString();
        
        if(wordOne.length()<1) {//display message if text field is empty
            Toast.makeText(getBaseContext(),"Word one is required",Toast.LENGTH_SHORT).show();
            return;
        }
        
    	EditText wordTwoTextField = (EditText) findViewById(R.id.wordTwo);
        String  wordTwo = wordTwoTextField.getText().toString();
        
        if(wordTwo.length()<1) {//display message if text field is empty
            Toast.makeText(getBaseContext(),"Word two is required",Toast.LENGTH_SHORT).show();
            return;
        }
        
    	EditText wordThreeTextField = (EditText) findViewById(R.id.wordThree);
        String  wordThree = wordThreeTextField.getText().toString();
        
        if(wordThree.length()<1) {//display message if text field is empty
            Toast.makeText(getBaseContext(),"Word three is required",Toast.LENGTH_SHORT).show();
            return;
        }
        
        Spinner ageSpinnerField = (Spinner) findViewById(R.id.ageSpinner);
        String  age = (String) ageSpinnerField.getSelectedItem();
        
        Spinner knowSpinnerField = (Spinner) findViewById(R.id.knowSpinner);
        String  know = (String) knowSpinnerField.getSelectedItem();
        
        RatingBar ratingField = (RatingBar) findViewById(R.id.rating);
        int  rating = ratingField.getNumStars();
        
    	String imageLoc;
		imageLoc = mImageUri.getPath();
		
		//ensure locationObj is set
//		if(locationObj == null){
//            Toast.makeText(getBaseContext(),"Location could not be acquired",Toast.LENGTH_SHORT).show();
//            return;
//		}
        
 //       rmvOverlayItem = new RmVOverlayItem("","", locationObj);
        rmvOverlayItem.setComments(comments);
        rmvOverlayItem.setWords(new String[]{wordOne, wordTwo, wordThree});
        rmvOverlayItem.setAge(age);
        rmvOverlayItem.setKnow(know);
        rmvOverlayItem.setRating(rating);
        rmvOverlayItem.setPhotoLocation(imageLoc);
        rmvOverlayItem.setPhotoData(imageStr);
        
        rmvOverlayItem.setTs(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        rmvOverlayItem.setTime(new SimpleDateFormat("hh:mm").format(new Date()));
        
        //Get location and direction data directly from map
        rmvOverlayItem.setHeading(heading);
        //System.out.println("Heading being sent is: " + heading);
        
        String nonce = java.util.UUID.randomUUID().toString();
        rmvOverlayItem.setNonce(nonce);


        showUploadDialog();
        new PostViewTask().execute(rmvOverlayItem);
    }
    
    private void showUploadDialog(){
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Sending\nPlease wait..");
		progressDialog.setCancelable(false);
		progressDialog.setIndeterminate(true);
		progressDialog.show();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);//Lock screen rotation
    }
    
    private void hideUploadDialog(){
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);//Unlock screen rotation
        progressDialog.cancel();
    }
    
    private void reloadView(){
    	Intent intent = new Intent(this, MyViewActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//Don't show up in history //FLAG_ACTIVITY_CLEAR_TOP
     	this.startActivity(intent);
    }
    
    private void showSaveDialog(){
    	OnClickListener save = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    	System.out.println("Saving view");
    	    	saveView(rmvOverlayItem);
    	    	hideSaveDialog();
    	    	reloadView();
    	    }
    	};
    	
    	OnClickListener cancel = new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    	    	System.out.println("Discarding view");
    	    	hideSaveDialog();
    	    	reloadView();
    		}
    	};
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

    	// 2. Chain together various setter methods to set the dialog characteristics
    	builder.setMessage(R.string.save_message)
    	       .setTitle(R.string.save_title)
    	       .setPositiveButton(R.string.save_view, save)
    	       .setNegativeButton(R.string.cancel, cancel);

    	// 3. Get the AlertDialog from create()
    	alertDialog = builder.create();
    	alertDialog.show();
    }
    
    private void hideSaveDialog(){
    	alertDialog.dismiss();
    }
    
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
    
    public void saveView(RmVOverlayItem iRmvOverlayItem){
		System.out.println("Saving View");
	//	ViewORM entity = new ViewORM(iRmvOverlayItem);
	//	entity.save();//Save object to DB
        Toast.makeText(getBaseContext(), "View saved locally", Toast.LENGTH_LONG).show();
    }
    
    public void postCallback(String data){
    	hideUploadDialog();
    	if(data == null){//Failed request
    		if(rmvOverlayItem.dbId != null){
    	        Toast.makeText(getBaseContext(), "Could not upload saved", Toast.LENGTH_LONG).show();
    			return;
    		} else {
    			showSaveDialog();
    			return;
    		}
    	}
    	//Check to see if String will parse
    	//Get ID and Photo url from entity
    	try {
            // A Simple JSONArray Creation
            JSONObject json = new JSONObject(data);
            //System.out.println("Id: " + json.getString("id"));
            
            String photo = json.getString("photo");
            rmvOverlayItem.setPhoto(photo);
            
            rmvOverlayItem.setId(json.getString("id"));
            
            if(rmvOverlayItem.fromDB){//Item was from Database
                //Try and remove View from DB if it was previously saved
            	//ViewORM.Del(rmvOverlayItem.dbId);
                //ViewORM.delete(null, rmvOverlayItem.dbId);//ID of view in DB
            }
            
	        Toast.makeText(getBaseContext(), "View submitted successfully", Toast.LENGTH_LONG).show();
	    	Intent intent = new Intent(this, TheirViewActivity.class);
	    	intent.putExtra("object", rmvOverlayItem);//Item will be new RmVOverlayItem
	    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//Don't show up in history
	     	this.startActivity(intent);
    	} catch (JSONException e) {
    		System.out.println("Failed to parse Object as JSON");
			e.printStackTrace();
			showSaveDialog();
		}
    }
    
    private class PostViewTask extends AsyncTask<RmVOverlayItem, Integer, String> {
    	private String convertStreamToString(InputStream is) {
            /*
             * To convert the InputStream to String we use the BufferedReader.readLine()
             * method. We iterate until the BufferedReader return null which means
             * there's no more data to read. Each line will appended to a StringBuilder
             * and returned as String.
             */
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
     
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
     
            return sb.toString();
        }
    	
        protected String doInBackground(RmVOverlayItem... item) {
        	RmVOverlayItem myView = item[0];
            try {
            	
            	PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            	int versionNumber = pinfo.versionCode;
            	String versionName = pinfo.versionName;
            	
            	System.out.println("Submitting View");
          	  	
            	final HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
                HttpConnectionParams.setSoTimeout(httpParams, 30000);
                HttpClient httpclient = new DefaultHttpClient(httpParams);
                HttpPost httppost = new HttpPost(getString(R.string.base_url) + "view/");

                List<NameValuePair> nameValuePairs = new ArrayList<>(2);
            	
                //Send device information along with the View
				nameValuePairs.add(new BasicNameValuePair("appVersion", versionNumber + ""));
				nameValuePairs.add(new BasicNameValuePair("appOS", "Android"));
				
				
				nameValuePairs.add(new BasicNameValuePair("nonce", myView.getNonce()));
				
				nameValuePairs.add(new BasicNameValuePair("comments", myView.getComments()));
				nameValuePairs.add(new BasicNameValuePair("age", myView.getAge()));
				nameValuePairs.add(new BasicNameValuePair("knowarea", myView.getKnow()));
				nameValuePairs.add(new BasicNameValuePair("rating", "" + myView.getRating()));
				nameValuePairs.add(new BasicNameValuePair("photo", "" + myView.getPhotoData()));
				nameValuePairs.add(new BasicNameValuePair("heading", "" + myView.getHeading()));
			//	nameValuePairs.add(new BasicNameValuePair("lat", "" + myView.getLat()));
			//	nameValuePairs.add(new BasicNameValuePair("lng", "" + myView.getLng()));
                nameValuePairs.add(new BasicNameValuePair("words[]", myView.getWords()[0]));
                nameValuePairs.add(new BasicNameValuePair("words[]", myView.getWords()[1]));
                nameValuePairs.add(new BasicNameValuePair("words[]", myView.getWords()[2]));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse resp = httpclient.execute(httppost);
                HttpEntity entity = resp.getEntity();
                //Read input to String
                StatusLine status = resp.getStatusLine();
          
                if(status.getStatusCode() != 200){
                	return null;
                }
                 
                if (entity != null) {
                	// A Simple Response Read
                    InputStream instream = entity.getContent();
                    String result = convertStreamToString(instream);
                    // Closing the input stream will trigger connection release
                    instream.close();
                	//System.out.println(result);
                    return result;
                }
                return null;
            } catch (ClientProtocolException e) {
	        	e.printStackTrace();
	        } catch (IOException e) {
	        	e.printStackTrace();
	        } catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
        	System.out.println("Submitted View");
        	postCallback(result);
        }
    }
}
