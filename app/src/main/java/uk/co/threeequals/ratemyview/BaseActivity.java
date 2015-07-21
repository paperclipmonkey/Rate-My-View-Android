package uk.co.threeequals.ratemyview;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alexbbb.uploadservice.AbstractUploadServiceReceiver;
import com.alexbbb.uploadservice.UploadService;

public class BaseActivity extends AppCompatActivity {
    ListView mDrawerList;
    DrawerLayout mDrawerLayout;
    String TAGLISTEN = "RmVUploadListener";
    private AbstractUploadServiceReceiver uploadReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UploadService.NAMESPACE = getString(R.string.upload_namespace);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_fragment);

        String[] mPageTitles = getResources().getStringArray(R.array.array_pages);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navigation_drawer_list);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPageTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        selectItem(0);

        Intent intent = getIntent();
        if(intent != null && intent.getStringExtra("upload")!= null){
            openMenu();
        }

        uploadReceiver =
                new AbstractUploadServiceReceiver() {
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.navigation_drawer_progress);
                    TextView textView = (TextView) findViewById(R.id.navigation_drawer_text);

                    @Override
                    public void onProgress(String uploadId, int progress) {
                        Log.i(TAGLISTEN, "The progress of the upload with ID "
                                + uploadId + " is: " + progress);

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
                    }

                    @Override
                    public void onCompleted(String uploadId,
                                            int serverResponseCode,
                                            String serverResponseMessage) {
                        Log.i(TAGLISTEN, "Upload with ID " + uploadId
                                + " has been completed with HTTP " + serverResponseCode
                                + ". Response from server: " + serverResponseMessage);

                        textView.setText(R.string.uploading_success);
                        progressBar.setVisibility(View.INVISIBLE);

                        //If your server responds with a JSON, you can parse it
                        //from serverResponseMessage string using a library
                        //such as org.json (embedded in Android) or google's gson
                    }
                };
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment;
        if(position == 0) {
             fragment = new MapsFragment();
        } else {
            fragment = new AboutFragment();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        //setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawers();
    }

    class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
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
                openMenu();
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
        DrawerLayout drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer_layout.openDrawer(GravityCompat.START);
    }
}

