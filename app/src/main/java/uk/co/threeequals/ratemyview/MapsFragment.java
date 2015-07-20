package uk.co.threeequals.ratemyview;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    GoogleMap mMap;
    BitmapDescriptor panoramicIcon;
    HashMap<Marker, RmVOverlayItem> markerData;


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
                                              Intent intent = new Intent(getActivity(), TheirViewActivity.class);
                                              intent.putExtra("object", markerData.get(marker));
                                              startActivity(intent);
                                              return false;
                                          }
                                      }
        );

        getViews();//Get views when page opens
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
                                    latlngb.southwest.latitude
                                    //bounds.getLatSouthE6() / 1E6
                            )
            );

            jBounds.put(new JSONArray()
                            .put(
                                    latlngb.southwest.longitude
                                    //bounds.getLonWestE6() / 1E6
                            )
                            .put(
                                    latlngb.southwest.latitude
                                    //bounds.getLatSouthE6() / 1E6
                            )
            );
        } catch (JSONException e) {
        }

        JSONClient client = new JSONClient(getActivity(), l);
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
                    JSONArray locJSON = k.getJSONArray("loc");

                    LatLng loc = new LatLng(locJSON.getDouble(1),locJSON.getDouble(0));
                    Marker m = mMap.addMarker(new MarkerOptions()
                            .position(loc)
                            .icon(panoramicIcon)
                    );

                    RmVOverlayItem overlayItem = new RmVOverlayItem(k.getString("id"), "RmV", loc);
                    overlayItem.setId(k.getString("id"));
                    overlayItem.setRating(k.getInt("rating"));
                    overlayItem.setTs(k.getString("ts"));
                    overlayItem.setComments(k.getString("comments"));
                    overlayItem.setHeading(k.getLong("heading"));

                    JSONArray words = k.getJSONArray("words");
                    String[] keyAttributes = new String[words.length()];
                    for(int i1 = 0; i1 < words.length(); i1++) {
                        keyAttributes[i1] = words.getString(i1);
                    }

                    overlayItem.setWords(keyAttributes);
                    overlayItem.setPhoto(k.getString("photo"));
                    markerData.put(m, overlayItem);

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

}