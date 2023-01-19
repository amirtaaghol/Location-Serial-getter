package com.yarab.hamtashariftestproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_loc, btn_serial, btn_submit;
    TextView tv_loc, tv_serial;

    final int REQ_LOC = 101;
    final int REQ_SERIAL = 102;

    FusedLocationProviderClient fusedLocationProviderClient;

    String serial;
    String altitude ;
    String longitude ;

    TelephonyManager telephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        btn_loc = findViewById(R.id.btn_loc);
        btn_serial = findViewById(R.id.btn_serial);
        btn_submit = findViewById(R.id.btn_submit);
        tv_loc = findViewById(R.id.tv_loc);
        tv_serial = findViewById(R.id.tv_serial);
        //
        btn_loc.setOnClickListener(this);
        btn_serial.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
        //
        tv_loc.setText(" ");
        tv_serial.setText(" ");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_loc) {
            if (hasLocPermission()) {
                if (isLocationEnabled()) {
                    fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            if (location == null) {
                                requestNewLocation();
                            } else {
                                tv_loc.setText("Location : ");
                                altitude = String.valueOf(location.getAltitude());
                                tv_loc.append("\nAltitude : " + altitude);
                                longitude = String.valueOf(location.getLongitude());
                                tv_loc.append("\nLongitude : " + longitude);
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }

            }

        } else if (id == R.id.btn_serial) {

//                telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//                serial = telephonyManager.getImei();
            serial = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            tv_serial.setText("Serial Number :");
            tv_serial.append(serial);


        } else if (id == R.id.btn_submit) {

            if ((tv_loc.getText().toString().trim().isEmpty()) && (tv_serial.getText().toString().trim().isEmpty())) {
                Toast.makeText(this, "لطفا با کلیک بر روی دکمه ها اطللاعات را دریافت کنید", Toast.LENGTH_SHORT).show();
            } else {
                JSONArray data = new JSONArray();
                try {
                    JSONObject location = new JSONObject();
                    location.put("altitude", altitude);
                    location.put("longitude", longitude);
                    JSONObject serial = new JSONObject();
                    serial.put("serial", tv_serial.getText().toString());
                    data.put(location);
                    data.put(serial);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestQueue requestQueue = Volley.newRequestQueue(this);
                String url = "https://demo.thingsboard.io:1881/api/v1/$G4yFFyaE295WKMa8KzZK/telemetry";

                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST,
                        url, data, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            Toast.makeText(MainActivity.this, "response is : " + response.getString(0), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                requestQueue.add(jsonArrayRequest);
                Toast.makeText(this, "داده ها با موفقیت ارسال شدند.", Toast.LENGTH_SHORT).show();

            }
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocation() {
        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            tv_loc.setText("Location : ");
            altitude = String.valueOf(mLastLocation.getAltitude());
            tv_loc.append("\nAltitude : " + altitude);
            longitude = String.valueOf(mLastLocation.getLongitude());
            tv_loc.append("\nLongitude : " + longitude);
        }
    };


    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean hasLocPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            new AlertDialog.Builder(this).setCancelable(false)
                    .setMessage("برای دریافت موقعیت مکانی نیاز به دسترسی می باشد")
                    .setPositiveButton("قبول", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestLocPermission();
                        }
                    }).show();
        }
        return false;
    }

    private void requestLocPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC);
        }
    }

    private boolean hasSerialPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            new AlertDialog.Builder(this).setCancelable(false)
                    .setMessage("برای خواندن سریال نیاز به دسترسی می باشد")
                    .setPositiveButton("قبول", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestSerialPermission();
                        }
                    }).show();
        }
        return false;
    }

    private void requestSerialPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQ_SERIAL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQ_LOC:
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //
                } else {
                    Toast.makeText(this, "دسترسی های مورد نظر داده نشد", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQ_SERIAL:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //
                } else {
                    Toast.makeText(this, "دسترسی های مورد نظر داده نشد", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}