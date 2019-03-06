package com.nasaspaceapps.sudo.beachinfo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nasaspaceapps.sudo.beachinfo.rest.api.ApiClient;
import com.nasaspaceapps.sudo.beachinfo.rest.api.ApiInterface;
import com.nasaspaceapps.sudo.beachinfo.rest.model.Result;
import com.nasaspaceapps.sudo.beachinfo.rest.model.SafeExposureTime;
import com.nasaspaceapps.sudo.beachinfo.rest.model.SunInfo;
import com.nasaspaceapps.sudo.beachinfo.rest.model.SunPosition;
import com.nasaspaceapps.sudo.beachinfo.rest.model.Ultraviolet;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UVActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS = 125;
    TextView UV;



    //@BindView(R.id.uvMax)
    TextView uvMax;

    //@BindView(R.id.maxUvTime)
    TextView maxUvTime;

    //@BindView(R.id.ozoneExpose)
    TextView ozoneExpose;

    //@BindView(R.id.sunAzimuth)
    TextView sunAzimuth;

    //@BindView(R.id.sunAltitude)
    TextView sunAlt;

    LocationManager locationManager;
    GPSTracker gps;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uv);


        //GPS Location Location Manager
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        beginPermission();

        gps = new GPSTracker(UVActivity.this);

        // Check if GPS enabled
        if(gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            // \n is for new line
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();



        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            gps.showSettingsAlert();
        }


        UV = findViewById(R.id.UV);
        uvMax = findViewById(R.id.uvMax);
        maxUvTime = findViewById(R.id.maxUvTime);
        ozoneExpose = findViewById(R.id.ozoneExpose);
        sunAzimuth = findViewById(R.id.sunAzimuth);
        sunAlt = findViewById(R.id.sunAltitude);


        ApiInterface openUVApi = ApiClient.getClient().create(ApiInterface.class);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Log.e("MESSAGE", "WE are Getting "+ gps.getLatitude()+ " Longitude as" + gps.getLongitude());

            }
        }, 6000);

        Log.e("MESSAGE", "WE are Getting "+ gps.getLatitude()+ " Longitude as" + gps.getLongitude());
        Call<Ultraviolet> call;

        if(gps.getLatitude()==0.0 && gps.getLongitude() ==0.0){

             call = openUVApi.getUvData("7d0f4babcad636158de5c9d830d3b632", ""+gps.getLatitude(), ""+gps.getLongitude());

        }
        else {

            call = openUVApi.getUvData("7d0f4babcad636158de5c9d830d3b632", "" + getLat(), "" + getLon());
        }

        call.enqueue(new Callback<Ultraviolet>() {
            @Override
            public void onResponse(Call<Ultraviolet> call, Response<Ultraviolet> response) {
                Log.d("response", response.toString());
                Result result = new Result();
                SafeExposureTime safeExposureTime = new SafeExposureTime();
                Ultraviolet uv = response.body();
                //GETTER METHODS FOR UV
                double currentuvData = uv.getResult().getUv();
                String currentUVTimeData = uv.getResult().getUvTime();
                double maxUVData = uv.getResult().getUvMax();
                String maxUVTimeData = uv.getResult().getUvMaxTime();
                double ozoneData = uv.getResult().getOzone();

                //Getter Method For SafeExposure
                SafeExposureTime safeExposureData = uv.getResult().getSafeExposureTime();
                Integer st1data = safeExposureData.getSt1();
                Integer st2data = safeExposureData.getSt2();
                Integer st3data = safeExposureData.getSt3();
                Integer st4data = safeExposureData.getSt4();
                Integer st5data = safeExposureData.getSt5();
                Integer st6data = safeExposureData.getSt6();

                //Getter Methods For SunInfo
                SunInfo sunInfo = uv.getResult().getSunInfo();
                SunPosition sunPosition = sunInfo.getSunPosition();
                double sunPositionAltitude = sunPosition.getAltitude();
                double sunPositionAzimuth = sunPosition.getAzimuth();

                //Setter Method For SafeExposureTime
                safeExposureData.setSt1(st1data);
                safeExposureData.setSt2(st2data);
                safeExposureData.setSt3(st3data);
                safeExposureData.setSt4(st4data);
                safeExposureData.setSt5(st5data);
                safeExposureData.setSt6(st6data);

                //Setter Methods for SunInfo
                sunPosition.setAltitude(sunPositionAltitude);
                sunPosition.setAzimuth(sunPositionAzimuth);

                //Setter MEthods For UV
                uv.setResult(result);
                result.setUv(currentuvData);
                result.setUvTime(currentUVTimeData);
                result.setUvMax(maxUVData);
                result.setUvMaxTime(maxUVTimeData);
                result.setOzone(ozoneData);


                //result.setSafeExposureTime(safeExposureTimeData);

                //Setter Methods For SafeExposure
                //safeExposureTime.setSt1(result.setSafeExposureTime());

                Log.e(" mainAction", "  UV - " + result.getUv());
                UV.setText(""+result.getUv());
                Log.e(" mainAction", "  UV Time - " + result.getUvTime());

                Log.e(" mainAction", "  UV Max - " + result.getUvMax());
                uvMax.setText(""+ result.getUvMax());
                Log.e("mainAction", "UV Max Time - " + result.getUvMaxTime());
                maxUvTime.setText(""+ result.getUvMaxTime());
                Log.e("mainAction", "Ozone Exposure" + result.getOzone());
                ozoneExpose.setText(""+ result.getOzone());
                Log.e("mainAction", "Safe Exposure Time 1:- " + safeExposureData.getSt1());

                Log.e("mainAction", "Safe Exposure Time 2:- " + safeExposureData.getSt2());

                Log.e("mainAction", "Safe Exposure Time 3:- " + safeExposureData.getSt3());

                Log.e("mainAction", "Safe Exposure Time 4:- " + safeExposureData.getSt4());

                Log.e("mainAction", "Safe Exposure Time 5:- " + safeExposureData.getSt5());

                Log.e("mainAction", "Safe Exposure Time 6:- " + safeExposureData.getSt6());

                Log.e("mainAction", "Sun Altitude:- " + sunPosition.getAltitude());
                sunAlt.setText(""+sunPosition.getAltitude());
                Log.e("mainAction", "Sun Azimuth:- " + sunPosition.getAzimuth());
                sunAzimuth.setText(""+ sunPosition.getAzimuth());


            }

            @Override
            public void onFailure(Call<Ultraviolet> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }

    public void beginPermission() {
        if ((ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {

            Log.i("1", "Permission is not granted");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA) && (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION))) {
                Log.i("REQUEST", "Requesting permission....");
                ActivityCompat.requestPermissions(UVActivity.this,
                        new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS);

            }
        } else {
            Toast.makeText(UVActivity.this, "THANKS", Toast.LENGTH_LONG);
            //Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            LocationListener locationListener = new MyLocationListener(this);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10, locationListener);




        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("1", "Permission is granted");
                    //Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    LocationListener locationListener = new MyLocationListener(this);
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.


                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

                } else {
                    Log.i("1", "Permission is again not granted");
                    Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content),
                            "Please ennable the permissions", Snackbar.LENGTH_SHORT);
                    mySnackbar.setAction("ENABLE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));

                        }
                    });
                    mySnackbar.show();

                }
                return;
            }
        }
    }

    public String getLat() {
        return "28.5474059";
       // SharedPreferences sharePref = getSharedPreferences("locationInfo", Context.MODE_PRIVATE);
       // return sharePref.getString("latitude", "ERROR!!");
    }

    public String getLon() {
        return "77.253208";
       // SharedPreferences sharePref = getSharedPreferences("locationInfo", Context.MODE_PRIVATE);
       // return sharePref.getString("longitude", "ERROR!!");
    }
}




