package uk.co.threeequals.ratemyview;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    GoogleMap mMap;
    BitmapDescriptor panoramicIcon;
    HashMap<String, RmVOverlayItem> markerData;
    ClusterManager<RmVOverlayItem> mClusterManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        FloatingActionButton myFab = (FloatingActionButton)  rootView.findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(getActivity(), MyViewActivity.class);
                startActivity(myIntent);
            }
        });

        markerData = new HashMap<>();

        return rootView;
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

        //Move the camera.
        LatLng uk = new LatLng(50.24344,-3.866643);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(uk, 5));

        setUpClusterer();
        getViews();//Get views when page opens
    }

    private void setUpClusterer() {
        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<>(getActivity(), mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                                           @Override
                                           public void onCameraChange(CameraPosition cameraPosition) {
                                               getViews();

                                               //Call the marker clusterer
                                               mClusterManager.onCameraChange(cameraPosition);
                                           }
                                       }
        );

        mMap.setOnMarkerClickListener(mClusterManager);

        //Setup click events
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<RmVOverlayItem>() {
            @Override
            public boolean onClusterItemClick(RmVOverlayItem rmVOverlayItem) {
                Intent intent = new Intent(getActivity(), TheirViewActivity.class);
                intent.putExtra("object", rmVOverlayItem);
                startActivity(intent);
                return false;
            }
        });

    }


    private void getViews(){
        //When changing camera need way of stopping first request
        //Then loading a new request
        VisibleRegion bounds = mMap.getProjection().getVisibleRegion();
        LatLngBounds latlngb = bounds.latLngBounds;

        //Don't send 0,0 Lat/Lng to server
        if(latlngb.southwest.longitude == 0 && latlngb.southwest.latitude == 0){
            return;
        }
        //Create points
        JSONArray jBounds = new JSONArray();
        try {
            jBounds.put(new JSONArray()
                            .put(
                                    latlngb.southwest.longitude
                            )
                            .put(
                                    latlngb.southwest.latitude
                            )
            );

            jBounds.put(new JSONArray()
                            .put(
                                    latlngb.southwest.longitude
                                    //bounds.getLonEastE6() / 1E6
                            )
                            .put(
                                    latlngb.northeast.latitude
                                    //bounds.getLatNorthE6() / 1E6
                            )
            );

            jBounds.put(new JSONArray()
                            .put(
                                    latlngb.northeast.longitude
                                    //bounds.getLonEastE6() / 1E6
                            )
                            .put(
                                    latlngb.northeast.latitude
                                    //bounds.getLatSouthE6() / 1E6
                            )
            );

            jBounds.put(new JSONArray()
                            .put(
                                    latlngb.northeast.longitude
                                    //bounds.getLonWestE6() / 1E6
                            )
                            .put(
                                    latlngb.southwest.latitude
                                    //bounds.getLatSouthE6() / 1E6
                            )
            );
        } catch (JSONException e) {
        }



        String url = getString(R.string.base_url) + getString(R.string.views_query_path) + jBounds.toString();
        new DownloadViewsTask().execute(url);

        //client.execute(url);

        //System.out.println(jBounds.toString());
    }

    class DownloadViewsTask extends AsyncTask<String, String, String> {
        private JSONArray jArray;
        @Override
        protected String doInBackground(String... uri) {
            String responseString = null;
            try {
                URL url = new URL(uri[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if(conn.getResponseCode() == HttpsURLConnection.HTTP_OK){
                    responseString = readInputStreamToString(conn);
                    try {
                        jArray = new JSONArray(responseString);
                    } catch (JSONException e) {
                        Log.e("JSONException", "Error: " + e.toString());
                    }
                }
                else {
                    responseString = "FAILED"; // See documentation for more info on response handling
                }
            } catch (IOException e) {
                //TODO Handle problems..
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(jArray != null) {
                addJSON(jArray);
            }
            //Do anything with response..
        }

        private String readInputStreamToString(HttpURLConnection connection) {
            String result = null;
            StringBuffer sb = new StringBuffer();
            InputStream is = null;

            try {
                is = new BufferedInputStream(connection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String inputLine = "";
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine);
                }
                result = sb.toString();
            }
            catch (Exception e) {
                result = null;
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (IOException e) {
                    }
                }
            }

            return result;
        }
    }

    public void addJSON(JSONArray jsonFromNet){
        int i = 0;
        while( i < jsonFromNet.length() ){
            JSONObject k;
            try {
                k = jsonFromNet.getJSONObject(i);

                if(markerData.get(k.getString("id")) == null){//If new
                    JSONArray locJSON = k.getJSONArray("loc");

                    LatLng loc = new LatLng(locJSON.getDouble(1),locJSON.getDouble(0));

//                  .icon(panoramicIcon)

                    RmVOverlayItem overlayItem = new RmVOverlayItem();
                    overlayItem.setPosition(loc);
                    overlayItem.setStringId(k.getString("id"));
                    overlayItem.setRating(k.getInt("rating"));
                    overlayItem.setTs(k.getString("ts"));
                    overlayItem.setComments(k.getString("comments"));
                    overlayItem.setHeading(k.getLong("heading"));

                    JSONArray words = k.getJSONArray("words");
                    String[] keyAttributes = new String[words.length()];
                    for(int i1 = 0; i1 < words.length(); i1++) {
                        keyAttributes[i1] = words.getString(i1);
                    }

                    overlayItem.setWordsArray(keyAttributes);
                    overlayItem.setPhoto(k.getString("photo"));

                    overlayItem.setPosition(loc);

                    mClusterManager.addItem(overlayItem);

                    markerData.put(overlayItem.getStringId(), overlayItem);
                }
            } catch (JSONException e) {

            }
            i++;
        }
        mClusterManager.cluster();
    }

}