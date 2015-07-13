package com.threeequals.ratemyview;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.TilesOverlay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import uk.co.threeequals.ratemyview.R;

public class MapActivity extends AppCompatActivity {

	RmVMapView mMapView;
	private MyLocationOverlay mMyLocationOverlay;
	private MapTileProviderBasic tileProviderSatellite;
	private MapTileProviderBasic tileProviderOverlay;
	private List<RmVOverlayItem> items;
    ItemizedOverlayWithFocus<RmVOverlayItem> overlay;

	@Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        items = new ArrayList<>();
        setContentView(R.layout.mapscreen);
        
        overlay = new ItemizedOverlayWithFocus<>(getApplicationContext(), items,
                new ItemizedIconOverlay.OnItemGestureListener<RmVOverlayItem>() {

                    @Override
                    public boolean onItemSingleTapUp(final int index, final RmVOverlayItem item) {
                    	loadTheirView(item);
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final RmVOverlayItem item) {
                        return false;
                    }
                });

        mMapView = (RmVMapView) findViewById(R.id.mapview);
        
				
        
		tileProviderSatellite = new MapTileProviderBasic(getApplicationContext());
		String [] satelliteUrl = {
				"http://a.tiles.mapbox.com/v3/paperclipmonkey.map-asryj7mr/",
				"http://b.tiles.mapbox.com/v3/paperclipmonkey.map-asryj7mr/",
				"http://c.tiles.mapbox.com/v3/paperclipmonkey.map-asryj7mr/",
				"http://d.tiles.mapbox.com/v3/paperclipmonkey.map-asryj7mr/"
		};

		ITileSource tileSourceSatellite = new XYTileSource("Satellite", null, 3, 18, 256, ".png", satelliteUrl);
		tileProviderSatellite.setTileSource(tileSourceSatellite);
        mMapView.setTileSource(tileSourceSatellite);

        // Add tiles layer with custom tile source
        tileProviderOverlay = new MapTileProviderBasic(this);
        new XYTileSource(null, null, 0, 0, 0, null, null);
        String [] overlayUrl = {
        		"http://a.tiles.ratemyview.co.uk/v2/aonbs/",
        		"http://b.tiles.ratemyview.co.uk/v2/aonbs/",
        		"http://c.tiles.ratemyview.co.uk/v2/aonbs/",
        		"http://d.tiles.ratemyview.co.uk/v2/aonbs/"
        		};
		ITileSource tileSourceOverlay = new XYTileSource("AONBs", null, 1, 18, 256, ".png", overlayUrl);//{z}/{x}/{y}.png
        tileProviderOverlay.setTileSource(tileSourceOverlay);
        TilesOverlay tileOverlay = new TilesOverlay(tileProviderOverlay, getBaseContext());
        mMapView.getOverlays().add(tileOverlay);
        
        mMapView.setBuiltInZoomControls(false);
        mMapView.setMultiTouchControls(true);
        mMapView.setClickable(false);

        IMapController mMapController = mMapView.getController();
        mMapController.setZoom(12);
        if(savedInstanceState != null && savedInstanceState.containsKey("lng")){
            //Set map centerpoint to what it was before
        	GeoPoint gPt = new GeoPoint(savedInstanceState.getInt("lat"),savedInstanceState.getInt("lng"));
            mMapController.setCenter(gPt);
            setupMyLocation(false);
        } else {
        	final GeoPoint gPt = new GeoPoint(50.24344,-3.866643);//Center on South Devon AONB area
            mMapController.setCenter(gPt);
            setupMyLocation(true);
        }
        
        //getViews();
        
        mMapView.getOverlays().add(overlay);
        
        mMapView.setMapListener(new DelayedMapListener(
        	new MapListener() {   
	            public boolean onZoom(ZoomEvent arg0) {
	                return false;
	            }
	
	            public boolean onScroll(ScrollEvent arg0) {
	            	getViews();
	                return false;
	            }
	        }
        ,1000));//1000ms timeout
    }
	
	private void myExceptionHandler(Exception e){
		
	}
    
    private void getViews(){
    	//Need way of stopping first request
    	//Then loading a new request
    	BoundingBoxE6 bounds = mMapView.getBoundingBox();
    	//Create points
    	JSONArray jBounds = new JSONArray();
    	try {
			jBounds.put(new JSONArray()
			.put(
			    	bounds.getLonWestE6() / 1E6
				)
			.put(
					bounds.getLatNorthE6() / 1E6
				)
			);
    	
	    	jBounds.put(new JSONArray()
			.put(
					bounds.getLonEastE6() / 1E6
				)
			.put(
					bounds.getLatNorthE6() / 1E6
				)
	    	);
	    	
	    	jBounds.put(new JSONArray()
			.put(
			    	bounds.getLonEastE6() / 1E6
				)
			.put(
					bounds.getLatSouthE6() / 1E6
				)
	    	);
	    	
	    	jBounds.put(new JSONArray()
			.put(
			    	bounds.getLonWestE6() / 1E6
				)
			.put(
					bounds.getLatSouthE6() / 1E6
				)
	    	);
		} catch (JSONException e) {
			myExceptionHandler(e);
		}
    	
        JSONClient client = new JSONClient(getApplicationContext(), l);
		String url = getString(R.string.base_url) + "views/?withinarea=" + jBounds.toString();
	    client.execute(url);
    }
    
    protected void onStop(){
    	System.out.println("Stopping");
    	tileProviderSatellite.detach();
    	super.onStop();
    }
    
    private void setupMyLocation(final boolean goToLocation){
        mMyLocationOverlay = new MyLocationOverlay(
        		getApplicationContext(),
        		mMapView,
                mMapView.getResourceProxy());
        mMyLocationOverlay.enableMyLocation();
        mMyLocationOverlay.enableCompass();
        mMyLocationOverlay.disableFollowLocation();
        mMyLocationOverlay.setDrawAccuracyEnabled(true);
        mMyLocationOverlay.setCompassCenter(40, -20);
        if(goToLocation){
		    mMyLocationOverlay.runOnFirstFix(new Runnable() {
		        public void run() {
		        	mMapView.getController().animateTo(mMyLocationOverlay
		                    .getMyLocation());
		        }
		    });
        }
        mMapView.getOverlays().add(mMyLocationOverlay);
    }

    private void updateMyLocationOverlay(){
    	mMapView.getOverlays().remove(mMyLocationOverlay);
    	mMapView.getOverlays().add(mMyLocationOverlay);
    }

    public void addJSON(JSONArray jsonFromNet){
    	List<RmVOverlayItem> newList = new ArrayList<>();
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
		            GeoPoint geoPoint = new GeoPoint(loc.getDouble(1),loc.getDouble(0));
		            RmVOverlayItem overlayItem = new RmVOverlayItem(k.getString("id"), "RmV", geoPoint);
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
		            overlayItem.setMarker(getApplicationContext().getResources().getDrawable(R.drawable.panoramicview));
		            newList.add(overlayItem);
		        }
			} catch (JSONException e) {
				myExceptionHandler(e);
			}
			i++;
        }
        mMapView.getOverlays().remove(overlay);

        overlay = new ItemizedOverlayWithFocus<>(getApplicationContext(), new ArrayList<RmVOverlayItem>(),
                new ItemizedIconOverlay.OnItemGestureListener<RmVOverlayItem>() {

                    @Override
                    public boolean onItemSingleTapUp(final int index, final RmVOverlayItem item) {
                    	loadTheirView(item);
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final RmVOverlayItem item) {
                        return false;
                    }
                });
        
        items.addAll(newList);
        overlay.addItems(items);
        overlay.setEnabled(true);
        mMapView.getOverlays().add(overlay);
        updateMyLocationOverlay();
        mMapView.invalidate();
    }
    
    private void loadTheirView( final RmVOverlayItem item){
    	Intent intent = new Intent(getApplicationContext(), TheirViewActivity.class);
    	intent.putExtra("object", item);
     	startActivity(intent);
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

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    // Save the user's current game state
		IGeoPoint k = mMapView.getMapCenter();
	    savedInstanceState.putInt("lat", k.getLatitudeE6());
	    savedInstanceState.putInt("lng", k.getLongitudeE6());
	    // Always call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onPause() {
		System.out.println("Pausing");
		mMapView.destroyDrawingCache();
		mMapView.onDetach();
		tileProviderSatellite.clearTileCache();
		mMyLocationOverlay.disableMyLocation();
        mMyLocationOverlay.disableCompass();
        //Save map location
		super.onPause();
		//finish();
	}
	
	@Override
	public void onResume() {
		System.out.println("Resuming");
		tileProviderSatellite.createTileCache();
		mMyLocationOverlay.enableMyLocation();
        mMyLocationOverlay.enableCompass();
        //Reload map location
		super.onResume();
	}
	
	@Override
	public void onDestroy(){
		System.out.println("Destroying");
		mMapView.getTileProvider().clearTileCache();
		tileProviderSatellite.detach();
		super.onDestroy();
	}

}
