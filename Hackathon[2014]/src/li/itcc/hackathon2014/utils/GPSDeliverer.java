
package li.itcc.hackathon2014.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

public class GPSDeliverer implements LocationListener {
    private GPSLocationListener fListener;
    private Context fContext;
    private LocationManager fLocManager;
    private long fSampleDeltaTimeMS;
    private long fNextSampleTimeMS = 0L;
    private boolean fHeartbeatReceived;

    public GPSDeliverer(Context context, long delay) {
        fContext = context;
        fSampleDeltaTimeMS = delay;
    }

    public void startDelivery(GPSLocationListener gpsLocationListener) {
        fHeartbeatReceived = false;
        if (gpsLocationListener == null) {
            throw new NullPointerException();
        }
        fListener = gpsLocationListener;
        fLocManager = (LocationManager) fContext
                .getSystemService(Context.LOCATION_SERVICE);
        fLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Handler mainHandler = new Handler(fContext.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                checkHeartBeat();
            }
        };
        mainHandler.postDelayed(myRunnable, 1000);
    }

    protected void checkHeartBeat() {
        if (fHeartbeatReceived) {
            return;
        }
        if (fListener != null) {
            fListener.onLocationSensorSearching();
        }
    }

    public void stopDelivery() {
        if (fListener != null) {
            fLocManager.removeUpdates(this);
            fListener = null;
        }
    }

    public boolean isRunning() {
        return fListener != null;
    }

    @Override
    public void onLocationChanged(Location location) {
        fHeartbeatReceived = true;
        if (fListener == null) {
            return;
        }
        if (fSampleDeltaTimeMS == 0) {
            fListener.onLocation(location);
        }
        long now = System.currentTimeMillis();
        if (fNextSampleTimeMS == 0 || now > fNextSampleTimeMS) {
            fNextSampleTimeMS = now + fSampleDeltaTimeMS;
            fListener.onLocation(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        fHeartbeatReceived = true;
        fListener.onLocationSensorEnabled();
    }

    @Override
    public void onProviderDisabled(String provider) {
        fHeartbeatReceived = true;
        fListener.onLocationSensorDisabled();
    }

}
