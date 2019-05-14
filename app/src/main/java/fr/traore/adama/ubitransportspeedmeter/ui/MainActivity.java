package fr.traore.adama.ubitransportspeedmeter.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationListener;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.traore.adama.ubitransportspeedmeter.R;
import fr.traore.adama.ubitransportspeedmeter.service.LocationService;

public class MainActivity extends AppCompatActivity {

    //region Properties
    private LocationListener mLocationListener;
    @BindView(R.id.txvCurrentSpeed) TextView txvCurrentSpeed;
    @BindView(R.id.animationView) LottieAnimationView animationView;
    private BroadcastReceiver mReceiver;
    //endregion


    //region Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String speedConverted = intent.getStringExtra(LocationService.COPA_MESSAGE);
                txvCurrentSpeed.setText(speedConverted);
            }
        };

        //Dexter will simplify the process of requesting permissions
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if(report.areAllPermissionsGranted()){

                    Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
                    startService(serviceIntent);

                    //Demarrage de l'animation
                    animationView.setAnimation("bus_loading.json");

                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        })
                .onSameThread()
                .check();

    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver((mReceiver),
                new IntentFilter(LocationService.COPA_RESULT)
        );

        animationView.playAnimation();

    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        animationView.pauseAnimation();
        super.onStop();
    }

    //endregion

}
