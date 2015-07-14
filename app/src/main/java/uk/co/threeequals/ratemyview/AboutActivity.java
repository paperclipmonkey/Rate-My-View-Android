package uk.co.threeequals.ratemyview;

import android.os.Bundle;
import android.app.Activity;

import uk.co.threeequals.ratemyview.R;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		finish();//Free up memory
	}
}