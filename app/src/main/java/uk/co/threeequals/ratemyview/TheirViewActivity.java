package uk.co.threeequals.ratemyview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.InputStream;

public class TheirViewActivity extends AppCompatActivity {

	private String theirImageUri;
	public ImageView img;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_their_view);
		Bundle extras = getIntent().getExtras();
		
		RmVOverlayItem item = extras.getParcelable("object");
		
		TextView rating = (TextView) findViewById(R.id.theirRating);
		rating.setText(item.getRating() + "/5");
	
		TextView comments = (TextView) findViewById(R.id.theirComments);

		String commentHtml = Html.fromHtml(item.getComments()).toString();
		comments.setText(commentHtml);
		
		TextView ts = (TextView) findViewById(R.id.theirTs);
		ts.setText("Uploaded: " + item.getTs());
		
//		TextView heading = (TextView) findViewById(R.id.theirHeading);
//		heading.setText("Heading: " + item.getHeading() + getString(R.string.degree_symbol));
		
		TextView words = (TextView) findViewById(R.id.theirWords);
		words.setText("");//Blank
		String[] wordList = item.getWordsArray();
		for( int i = 0; i < wordList.length; i++) {
			words.setText(words.getText() + "\n" + wordList[i]);
		}
		
		img = (ImageView) findViewById(R.id.theirImage);
//		img.setOnClickListener(
//			new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//						Intent i = new Intent(getBaseContext(), TheirImageActivity.class);
//						if(theirImageUri != null){
//							i.putExtra("Image", theirImageUri);
//						}
//						startActivity(i);
//			}
//		});
				
		if(item.getPhotoLocation() != null){
			theirImageUri = item.getPhotoLocation();
			File imgFile = new File(item.getPhotoLocation());
			if(imgFile.exists()){
		    	loadImageFromFile(imgFile);
			}
			
		} else {
	        File tempDir = Environment.getExternalStorageDirectory();
		    tempDir = new File(tempDir.getAbsolutePath()+"/.temp/");
		    if(!tempDir.exists()) {
		        tempDir.mkdir();
		    }
		    try {
		    	File fileExists = new File(tempDir.getAbsolutePath()+ item.getPhoto());
		    	if(fileExists.exists()){
			    	loadImageFromFile(fileExists);
			    } else {
			    	loadImageFromUrl(item.getPhoto());
			    }
		    } catch(Exception e){
		    	System.out.println("File error");
		    	loadImageFromUrl(item.getPhoto());
		    }
		}
	}
	
	private void loadImageFromUrl(String photoUrl){
        photoUrl = getString(R.string.assets_domain) + getString(R.string.assets_path) + photoUrl;
        new DownloadImageTask((ImageView) findViewById(R.id.theirImage))
                .execute(photoUrl);
	}
	
	private void loadImageFromFile(File f){
		theirImageUri = f.getAbsolutePath();
		Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
		img.setImageBitmap(myBitmap);
	}

    
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        final ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
	
//	private String getDistance(){
//		//return distance(lat, lng, lat, lng"k") + " km";
//		return null;
//	}
//	
//    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
//		double theta = lon1 - lon2;
//		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
//		dist = Math.acos(dist);
//		dist = rad2deg(dist);
//		dist = dist * 60 * 1.1515;
//		if (unit + "" == "K") {
//			dist = dist * 1.609344;
//		} else if (unit + "" == "N") {
//			dist = dist * 0.8684;
//		}
//		return (dist);
//    }

      /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
      /*::  This function converts decimal degrees to radians             :*/
      /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
//	  private double deg2rad(double deg) {
//		  return (deg * Math.PI / 180.0);
//	  }
//
//      /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
//      /*::  This function converts radians to decimal degrees             :*/
//      /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
//      private double rad2deg(double rad) {
//        return (rad * 180.0 / Math.PI);
//      }

}