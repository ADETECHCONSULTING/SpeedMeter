package fr.traore.adama.ubitransportspeedmeter.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import fr.traore.adama.ubitransportspeedmeter.ui.SummaryActivity;

public class LocationService extends Service {

    //region Properties
    private static final String TAG = LocationService.class.getSimpleName();
    //The min distance to update in meters
    private static final int MIN_DISTANCE_CHANGE_UPDATE = 5; //5 meters

    //The min time to update in milliseconds
    private static final long MIN_TIME_CHANGE_UPDATE = 3000; //3 seconds

    private static LocationListener mLocationListener = null;
    public Location mLocation;
    private LocationManager mLocationManager;
    private double prevSpeed = 0;
    private ArrayList<Integer> mListSpeed;
    static final public String COPA_RESULT = "com.controlj.copame.backend.COPAService.REQUEST_PROCESSED";
    static final public String COPA_MESSAGE = "com.controlj.copame.backend.COPAService.COPA_MSG";
    private LocalBroadcastManager mBroadcast;
    //endregion

    public LocationService(){}


    private class LocationListener implements android.location.LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if(location != null){

                mLocation  = location;
                float speed = location.getSpeed();

                //Add in the list of speed but filter if there two consecutive zeros
                if(prevSpeed > 0 || speed > 0)
                    mListSpeed.add(Math.round(speed));

                if(prevSpeed > 0 && speed == 0){
                    SummaryActivity.launch(LocationService.this, mListSpeed);
                }

                sendResult(String.valueOf(getConvertedSpeed(speed)));

                prevSpeed = speed;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mLocationManager != null){
            try{
                mLocationManager.removeUpdates(mLocationListener);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mBroadcast = LocalBroadcastManager.getInstance(this);
        mListSpeed = new ArrayList<>();
        mLocationListener = new LocationListener();



        try{
            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            if(mLocationManager == null) {
                Log.d(TAG, "LocationManager null");
                return;
            }

            //Get GPS and NETWORK Status
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(!isGPSEnabled && !isNetworkEnabled){
                Log.d(TAG, "Cannot get location. Neither GPS and Network are enabled");
                return;
            }

            if(isNetworkEnabled){
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_CHANGE_UPDATE, MIN_DISTANCE_CHANGE_UPDATE, mLocationListener);

                if(mLocationManager != null){
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }

            if(isGPSEnabled){
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_CHANGE_UPDATE, MIN_DISTANCE_CHANGE_UPDATE, mLocationListener);

                if(mLocationManager != null){
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }

            Log.d(TAG, "Init succeed");
        }
        catch (SecurityException e){
            Log.d(TAG, "Error permissions : "+e.getMessage());
        }
        catch (Exception e){
            Log.d(TAG, "Error while initializing LocationService : "+e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * LocationManager.getConvertedSpeed() return the speed in m/s so we have to convert it
     * @param speed
     * @return
     */
    private long getConvertedSpeed(float speed){
        String countryCode = Locale.getDefault().getCountry();
        long convertedSpeed = 0;
        if(countryCode.equals("US")){
            convertedSpeed = Math.round(speed * 2.2369); //mph
        }
        else{
            convertedSpeed = Math.round(speed*3.6); //kmh
        }

        return convertedSpeed;
    }

    public void sendResult(String message) {
        Intent intent = new Intent(COPA_RESULT);
        if(message != null)
            intent.putExtra(COPA_MESSAGE, message);
        mBroadcast.sendBroadcast(intent);
    }
}
