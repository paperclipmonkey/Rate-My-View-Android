package uk.co.threeequals.ratemyview;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by michaelwaterworth on 28/07/15.
 */
public class RmVApplication extends com.orm.SugarApp{
    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link com.orm.SugarApp}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.app_global_tracker);
        }
        return mTracker;
    }
}
