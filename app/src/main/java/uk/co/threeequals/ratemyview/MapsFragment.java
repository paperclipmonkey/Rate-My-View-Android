package uk.co.threeequals.ratemyview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

public class MapsFragment extends Fragment {
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        MapFragment mMapFragment = new MapFragment();

        GoogleMap map = mMapFragment.getMap();

        FragmentTransaction fragmentTransaction =
                getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container, mMapFragment);
        fragmentTransaction.commit();
    }