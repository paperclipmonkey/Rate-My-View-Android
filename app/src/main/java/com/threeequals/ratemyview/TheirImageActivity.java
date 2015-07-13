package com.threeequals.ratemyview;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import uk.co.threeequals.ratemyview.R;

public class TheirImageActivity extends Activity {
	ImageView bmImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_their_image);
		bmImage = (ImageView) findViewById(R.id.theirImageLarge);
		
		if(getIntent().hasExtra("Image")){
			String uri = getIntent().getStringExtra("Image");
			Bitmap bitmap = BitmapFactory.decodeFile(uri);
			bmImage.setImageBitmap(bitmap);			
		}
	}
	
	public void updateUri(String uri){
		Bitmap bitmap = BitmapFactory.decodeFile(uri);
		bmImage.setImageBitmap(bitmap);
	}

}
