package fr.traore.adama.ubitransportspeedmeter.ui;

import android.Manifest;
import android.location.LocationListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.traore.adama.ubitransportspeedmeter.helper.LocationHelper;
import fr.traore.adama.ubitransportspeedmeter.R;

public class MainActivity extends AppCompatActivity {

    //region Properties
    private LocationListener mLocationListener;
    @BindView(R.id.txvCurrentSpeed) TextView txvCurrentSpeed;
    //endregion


    //region Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Dexter will simplify the process of requesting permissions
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if(report.areAllPermissionsGranted()){
                    mLocationListener = LocationHelper.getInstance(MainActivity.this, txvCurrentSpeed);
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

    //endregion

}
