package uk.co.threeequals.ratemyview;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.content.Context;
import android.location.Location;

public class RmVMyLocationOverlay extends MyLocationOverlay{
	private RmVMapView mapView;

	public RmVMyLocationOverlay(Context ctx, RmVMapView iMapView) {
		super(ctx, iMapView);
		mapView = iMapView;
		// TODO Auto-generated constructor stub
	}
	
	public RmVMyLocationOverlay(Context ctx, RmVMapView iMapView, ResourceProxy p) {
		super(ctx, iMapView, p);
		mapView = iMapView;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onLocationChanged(Location location) {
	   super.onLocationChanged(location);
	   mapView.updateLocation(location);
	}
	
}