package uk.co.threeequals.ratemyview;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alexbbb.uploadservice.AbstractUploadServiceReceiver;
import com.alexbbb.uploadservice.UploadService;
import com.google.android.gms.analytics.Tracker;

public class BaseActivity extends AppCompatActivity {
    NavigationView mDrawerNav;
    DrawerLayout mDrawerLayout;
    public static Tracker tracker;

    final String TAGLISTEN = "RmVUploadListener";
    private AbstractUploadServiceReceiver uploadReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UploadService.NAMESPACE = getString(R.string.upload_namespace);

        //Set up Google Analytics
        RmVApplication application = (RmVApplication) getApplication();
        tracker = application.getDefaultTracker();

        setContentView(R.layout.activity_base_fragment);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerNav = (NavigationView) findViewById(R.id.navigation_drawer);
        mDrawerNav.setNavigationItemSelectedListener(new MyOnNavigationItemSelectedListener());

        updateWaitingViews();

        //Intent wants to know the status of the upload
        Intent intent = getIntent();
        if(intent != null && intent.getStringExtra("upload")!= null){
            openMenu();
        }

        Button reUploadButon = (Button) findViewById(R.id.reupload_button);
        reUploadButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Try to upload view again
                UploadManager.processQueue(getApplicationContext());
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.navigation_drawer_upload_helper_text);
                relativeLayout.setVisibility(View.GONE);
            }
        });

        uploadReceiver =
                new AbstractUploadServiceReceiver() {
                    final ProgressBar progressBar = (ProgressBar) findViewById(R.id.navigation_drawer_progress);
                    final TextView textView = (TextView) findViewById(R.id.navigation_drawer_text);

                    @Override
                    public void onProgress(String uploadId, int progress) {
                        //Log.i(TAGLISTEN, "The progress of the upload with ID "
                        //        + uploadId + " is: " + progress);

                        textView.setText(R.string.uploading_toast);

                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress(progress);
                    }

                    @Override
                    public void onError(String uploadId, Exception exception) {
                        Log.e(TAGLISTEN, "Error in upload with ID: " + uploadId + ". "
                                + exception.getLocalizedMessage(), exception);
                        textView.setText(R.string.upload_failed);
                        progressBar.setVisibility(View.INVISIBLE);

                        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.navigation_drawer_upload_helper_text);
                        relativeLayout.setVisibility(View.VISIBLE);

                        updateWaitingViews();
                    }

                    @Override
                    public void onCompleted(String uploadId,
                                            int serverResponseCode,
                                            String serverResponseMessage) {
//                        Log.i(TAGLISTEN, "Upload with ID " + uploadId
//                                + " has been completed with HTTP " + serverResponseCode
//                                + ". Response from server: " + serverResponseMessage);

                        //textView.setText(R.string.uploading_success);
                        progressBar.setVisibility(View.GONE);

                        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.navigation_drawer_upload_helper_text);
                        relativeLayout.setVisibility(View.VISIBLE);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateWaitingViews();
                            }
                        },1000); //adding one sec delay

                        //If your server responds with a JSON, you can parse it
                        //from serverResponseMessage string using a library
                        //such as org.json (embedded in Android) or google's gson
                    }
                };

        //Load map fragment
        selectItem(mDrawerNav.getMenu().getItem(0));
    }

    private void updateWaitingViews(){
        TextView textView = (TextView) findViewById(R.id.navigation_drawer_text);
        int viewsWaiting = UploadManager.getQueueLength();
        textView.setText(viewsWaiting + getString(R.string.upload_count));

        if(viewsWaiting == 0){
            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.navigation_drawer_upload_helper_text);
            relativeLayout.setVisibility(View.GONE);
        }
    }

    private void selectItem(MenuItem view) {
        // update the main content by replacing fragments
        //TODO cleanup so about fragment isn't converted to Map on rotation

        Fragment fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existingFragment;


        if(view.getItemId() == R.id.navigation_item_1) {
            fragment = new MapsFragment();
            existingFragment = getSupportFragmentManager().findFragmentByTag("MapFragment");
            if (existingFragment != null && existingFragment.getClass().equals(fragment.getClass()))
            {
                return; //nothing to do, because the fragment is already there
            }
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "MapFragment").commit();
        } else {
            fragment = new AboutFragment();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "AboutFragment").commit();
        }


        // update selected item and title, then close the drawer
        mDrawerNav.getMenu().findItem(view.getItemId()).setChecked(true);
        mDrawerLayout.closeDrawers();
    }

    class MyOnNavigationItemSelectedListener implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(MenuItem view) {
            selectItem(view);
        return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuitembutton, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_menu:
                toggleMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        uploadReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uploadReceiver.unregister(this);
    }


    private void openMenu(){
        updateWaitingViews();
        DrawerLayout drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer_layout.openDrawer(GravityCompat.START);
    }

    private void toggleMenu(){
        updateWaitingViews();
        DrawerLayout drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        } else {
            drawer_layout.openDrawer(GravityCompat.START);
        }
    }
}

