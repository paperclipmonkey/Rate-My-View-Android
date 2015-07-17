package uk.co.threeequals.ratemyview;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    GoogleMap mMap;
    BitmapDescriptor panoramicIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton myFab = (FloatingActionButton)  findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(MapsActivity.this, MyViewActivity.class);
                MapsActivity.this.startActivity(myIntent);
            }
        });


        String[] mPageTitles = getResources().getStringArray(R.array.array_pages);
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.navigation_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPageTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectMenuItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectMenuItem(int position) {
        // Create a new fragment and specify the planet to show based on position
//        Fragment fragment = new PlanetFragment();
//        Bundle args = new Bundle();
//        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//        fragment.setArguments(args);
//
//        // Insert the fragment by replacing any existing fragment
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.content_frame, fragment)
//                .commit();
//
//        // Highlight the selected item, update the title, and close the drawer
//        mDrawerList.setItemChecked(position, true);
//        setTitle(mPlanetTitles[position]);
//        mDrawerLayout.closeDrawer(mDrawerList);

        if(position == 0){
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            //intent.putExtra("object", marker);
            startActivity(intent);
        } else if(position == 1){
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            //intent.putExtra("object", marker);
            startActivity(intent);
        }
    }



    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        panoramicIcon = BitmapDescriptorFactory.fromResource(R.drawable.panoramicview);
        mMap.setMyLocationEnabled(true);
        CustomUrlTileProvider aonbOverlay = new CustomUrlTileProvider(256, 256, getString(R.string.overlayTileUrl));
        mMap.addTileOverlay(
                new TileOverlayOptions()
                        .tileProvider(aonbOverlay)
                        .zIndex(1));

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng uk = new LatLng(50.24344,-3.866643);
        map.moveCamera(CameraUpdateFactory.newLatLng(uk));

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                                           @Override
                                           public void onCameraChange(CameraPosition cameraPosition) {
                                               // Make a web call for the locations
                                               //myTask = new MyTask();
                                               //myTask.execute();

                                               getViews();
                                           }
                                       }
        );

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                           @Override
                                           public boolean onMarkerClick(Marker marker) {
                                               Intent intent = new Intent(getApplicationContext(), TheirViewActivity.class);
                                               //intent.putExtra("object", marker);
                                               startActivity(intent);
                                               return false;
                                           }
                                       }
        );
    }

    private void getViews(){
        //When changing camera need way of stopping first request
        //Then loading a new request
        VisibleRegion bounds = mMap.getProjection().getVisibleRegion();
        LatLngBounds latlngb = bounds.latLngBounds;
        //Create points
        JSONArray jBounds = new JSONArray();
        try {
            jBounds.put(new JSONArray()
                            .put(
                                    latlngb.southwest.longitude
                            )
                            .put(
                                    latlngb.northeast.latitude
                            )
            );

            jBounds.put(new JSONArray()
                            .put(
                                    latlngb.northeast.longitude
                                    //bounds.getLonEastE6() / 1E6
                            )
                            .put(
                                    latlngb.northeast.longitude
                                    //bounds.getLatNorthE6() / 1E6
                            )
            );

            jBounds.put(new JSONArray()
                            .put(
                                    latlngb.northeast.longitude
                                    //bounds.getLonEastE6() / 1E6
                            )
                            .put(
                                    latlngb.southwest.longitude
                                    //bounds.getLatSouthE6() / 1E6
                            )
            );

            jBounds.put(new JSONArray()
                            .put(
                                    latlngb.southwest.longitude
                                    //bounds.getLonWestE6() / 1E6
                            )
                            .put(
                                    latlngb.southwest.longitude
                                    //bounds.getLatSouthE6() / 1E6
                            )
            );
        } catch (JSONException e) {
        }

        JSONClient client = new JSONClient(getApplicationContext(), l);
        String url = getString(R.string.base_url) + "views/?withinarea=" + jBounds.toString();
        client.execute(url);

        System.out.println(jBounds.toString());
    }

    GetJSONListener l = new GetJSONListener(){
        @Override
        public void onRemoteCallComplete(JSONArray jsonFromNet) {
            if (jsonFromNet == null) {
                return;
            }
            addJSON(jsonFromNet);
        }
    };

    public void addJSON(JSONArray jsonFromNet){
        List<RmVOverlayItem> newList = new ArrayList<>();
        List<RmVOverlayItem> items = new ArrayList<>();
        int i = 0;
        while( i < jsonFromNet.length() ){
            JSONObject k;
            try {
                k = jsonFromNet.getJSONObject(i);
                int ii = 0;
                boolean newObj = true;
                while(ii < items.size()){
                    RmVOverlayItem item = items.get(ii);
                    if(item.getPhoto().equals(k.getString("photo"))){
                        newObj = false;
                    }
                    ii++;
                }
                if(newObj){//If new
                    JSONArray loc = k.getJSONArray("loc");

                    LatLng sydney = new LatLng(loc.getDouble(1),loc.getDouble(0));
                    mMap.addMarker(new MarkerOptions()
                            .position(sydney)
                            .title(k.getString("id"))
                            .icon(panoramicIcon));
                    //map.moveCamera(CameraUpdateFactory.newLatLng(sydney));

                    //GeoPoint geoPoint = new GeoPoint(loc.getDouble(1),loc.getDouble(0));
                    //RmVOverlayItem overlayItem = new RmVOverlayItem(k.getString("id"), "RmV", geoPoint);
//                    overlayItem.setId(k.getString("id"));
//                    overlayItem.setRating(k.getInt("rating"));
//                    overlayItem.setTs(k.getString("ts"));
//                    overlayItem.setComments(k.getString("comments"));
//                    overlayItem.setHeading(k.getLong("heading"));
                    JSONArray words = k.getJSONArray("words");
                    String[] keyAttributes = new String[words.length()];
                    for(int i1 = 0; i1 < words.length(); i1++) {
                        keyAttributes[i1] = words.getString(i1);
                    }
//                    overlayItem.setWords(keyAttributes);
//
//                    overlayItem.setPhoto(k.getString("photo"));
//                    overlayItem.setMarker(getApplicationContext().getResources().getDrawable(R.drawable.panoramicview));
//                    newList.add(overlayItem);
                }
            } catch (JSONException e) {
            }
            i++;
        }

        //mMapView.getOverlays().remove(overlay);

//        ItemizedOverlayWithFocus overlay;
//
//        overlay = new ItemizedOverlayWithFocus<>(getApplicationContext(), new ArrayList<RmVOverlayItem>(),
//                new ItemizedIconOverlay.OnItemGestureListener<RmVOverlayItem>() {
//
//                    @Override
//                    public boolean onItemSingleTapUp(final int index, final RmVOverlayItem item) {
//                        //loadTheirView(item);
//                        return true;
//                    }
//
//                    @Override
//                    public boolean onItemLongPress(final int index, final RmVOverlayItem item) {
//                        return false;
//                    }
//                });
//
//        items.addAll(newList);
//        overlay.addItems(items);
//        overlay.setEnabled(true);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu items for use in the action bar
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menuitembutton, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle presses on the action bar items
//        switch (item.getItemId()) {
//            case R.id.action_menu:
//                openMenu();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    private void openMenu(){
        DrawerLayout drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer_layout.openDrawer(GravityCompat.START);
    }
}

