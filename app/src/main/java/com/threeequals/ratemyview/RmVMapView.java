package com.threeequals.ratemyview;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;

public class RmVMapView extends MapView{
	
	public RmVMapView(Context context, AttributeSet s){
		super(context, s);
	}

	public RmVMapView(Context context, int tileSizePixels) {
		super(context, tileSizePixels);
		// TODO Auto-generated constructor stub
	}

	public void updateLocation(Location location) {
		// TODO Auto-generated method stub
		//location.
		GeoPoint p = new GeoPoint(location);
		this.getController().animateTo(p);
	}

}
