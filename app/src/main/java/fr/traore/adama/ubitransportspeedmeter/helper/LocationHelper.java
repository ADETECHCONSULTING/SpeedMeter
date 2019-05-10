package fr.traore.adama.ubitransportspeedmeter.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import fr.traore.adama.ubitransportspeedmeter.ui.SummaryActivity;

public class LocationHelper implements LocationListener {



    //region Properties
    private static final String TAG = LocationHelper.class.getSimpleName();
    //The min distance to update in meters
    private static final int MIN_DISTANCE_CHANGE_UPDATE = 5; //5 meters

    //The min time to update in milliseconds
    private static final long MIN_TIME_CHANGE_UPDATE = 3000; //3 seconds

    private static LocationHelper instance = null;
    public Location mLocation;
    private LocationManager mLocationManager;
    public double mLongitude;
    public double mLatitude;
    private TextView mTextView;
    private double prevSpeed = 0;
    private Context mContext;
    private ArrayList<Integer> mListSpeed;
    //endregion


    //region Init
    private LocationHelper(Context context, TextView view) {
        mContext = context;
        mTextView = view;
        mListSpeed = new ArrayList<>();

        init();
        Log.d(TAG, "LocationHelper created");
    }

    /**
     * Singleton
     * @param context
     * @param view
     * @return
     */
    public static LocationHelper getInstance(Context context, TextView view){
        if(instance == null){
            instance = new LocationHelper(context, view);
        }
        return instance;
    }


    /**
     * initialize location after permission is granted
     */
    private void init(){
        //If device is v23 or sup and the permissions were not granted
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Cannot init LocationHelper. Permissions not granted");
            return;
        }

        try{
            mLatitude = 0.0;
            mLongitude = 0.0;
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

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
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_CHANGE_UPDATE, MIN_DISTANCE_CHANGE_UPDATE, this);

                if(mLocationManager != null){
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }

            if(isGPSEnabled){
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_CHANGE_UPDATE, MIN_DISTANCE_CHANGE_UPDATE, this);

                if(mLocationManager != null){
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }

            Log.d(TAG, "Init succeed");
        }
        catch (Exception e){
            Log.d(TAG, "Error while initializing LocationHelper : "+e.getMessage());
        }
    }
    //endregion


    //region Overrided methods
    @Override
    public void onLocationChanged(Location location) {

        if(location != null){

            mLocation  = location;
            float speed = location.getSpeed();

            //Ajoute dans l'historique des vitesses mais pas de doublons de 0
            if(prevSpeed > 0 || speed > 0)
                mListSpeed.add(Math.round(speed));

            if(prevSpeed > 0 && speed == 0){
                SummaryActivity.launch(mContext, mListSpeed);
            }

            mTextView.setText(String.valueOf(getConvertedSpeed(speed)));

            prevSpeed = speed;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    //endregion

    /**
     * LocationManager.getConvertedSpeed() return the speed in m/s so we have to convert it
     * @param speed
     * @return
     */
    private long getConvertedSpeed(float speed){
        String countryCode = Locale.getDefault().getCountry();
        long convertedSpeed = 0;
        if(countryCode.equals("US")){
            convertedSpeed = Math.round(speed * 2.2369);
        }
        else{
            convertedSpeed = Math.round(speed*3.6);
        }

        return convertedSpeed;
    }
}
