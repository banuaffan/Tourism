package com.bnadev.tourism.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bnadev.tourism.BuildConfig;
import com.bnadev.tourism.R;
import com.bnadev.tourism.RecyclerTouchListener;
import com.bnadev.tourism.adapter.TourismAdapter;
import com.bnadev.tourism.detail.DetailActivity;
import com.bnadev.tourism.login.LoginActivity;
import com.bnadev.tourism.model.TourismList;
import com.bnadev.tourism.splash.SplashActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainContract.View, GoogleApiClient.OnConnectionFailedListener {

    public MainPresenter mPresenter;

    boolean doubleBackToExitPressedOnce = false;

    @BindView(R.id.vLoadingBg)View vLoadingBg;
    @BindView(R.id.vProgressBar)View vProgressBar;
    @BindView(R.id.swiperefresh)SwipeRefreshLayout swpiperefresh;
    @BindView(R.id.tourismRecyclerView)RecyclerView recyclerView;
    @BindView(R.id.tvEmptyView)TextView tvEmptyView;

    private TourismAdapter mAdapter;

    LinearLayoutManager linearLayoutManager;

    private GoogleApiClient mGoogleApiClient;

    private PopupWindow mPopupWindow;


    private String mLastUpdateTime;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 100000;

    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 50000;

    private static final int REQUEST_CHECK_SETTINGS = 100;

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    private Boolean mRequestingLocationUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);


        init();
        restoreValuesFromBundle(savedInstanceState);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mPresenter = new MainPresenter(this);

        viewGoneOne();

        swpiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swpiperefresh.setRefreshing(false);
                Toast.makeText(MainActivity.this,"Tourism List is Refreshing",Toast.LENGTH_SHORT).show();
            }
        });

        mPresenter.getRetrofitObject(getApplicationContext());

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

    }




    @Override
    protected void onResume() {
        super.onResume();

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateLocationUI();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
            }
        }

        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            SharedPreferences mLocation = MainActivity.this.getSharedPreferences("location", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mLocation.edit();
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lon = String.valueOf(mCurrentLocation.getLongitude());
            editor.putString("latitude", lat);
            editor.putString("longitude", lon);
            editor.apply();
        }
//        else {
//            SharedPreferences mLocation = MainActivity.this.getSharedPreferences("Location", Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = mLocation.edit();
////            String lat = String.valueOf("-6.21462");
////            String lon = String.valueOf("106.84513");
//            editor.putString("latitude", "-6.21462");
//            editor.putString("longitude", "106.84513");
//            editor.apply();
//        }

//        toggleButtons();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
        outState.putParcelable("last_known_location", mCurrentLocation);
        outState.putString("last_updated_on", mLastUpdateTime);

    }

//    private void toggleButtons() {
//        if (mRequestingLocationUpdates) {
//            btnStartUpdates.setEnabled(false);
//            btnStopUpdates.setEnabled(true);
//        } else {
//            btnStartUpdates.setEnabled(true);
//            btnStopUpdates.setEnabled(false);
//        }
//    }

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i("Location ", "All location settings are satisfied.");

//                        Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i("Location ", "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("Location ", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("Location ", errorMessage);

                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateLocationUI();
                    }
                });
    }

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
//                        toggleButtons();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e("Location ", "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e("Location ", "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
        }
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.myProfile:

                final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                final View view = layoutInflater.inflate(R.layout.my_profile,null);

                SharedPreferences mUser = MainActivity.this.getSharedPreferences("user",Context.MODE_PRIVATE);
                String name= mUser.getString("name","");
                String email = mUser.getString("email","");
                String photo = mUser.getString("photo",null);

                SharedPreferences mLocation = MainActivity.this.getSharedPreferences("location",Context.MODE_PRIVATE);
                String latitude = mLocation.getString("latitude", "-6.21462");
                String longitude = mLocation.getString("longitude", "106.84513");

                TextView tvNama = (TextView)view.findViewById(R.id.tvName);
                tvNama.setText(name);
                TextView tvEmail = (TextView)view.findViewById(R.id.tvEmail);
                tvEmail.setText(email);
                TextView tvLat = (TextView)view.findViewById(R.id.tvLat);
                tvLat.setText("Lat: "+latitude+", ");
                TextView tvLong = (TextView)view.findViewById(R.id.tvLong);
                tvLong.setText("Long: "+longitude);
                ImageView ivImage = (ImageView) view.findViewById(R.id.ivImage);
                if (photo!=null){
                    Picasso.with(getApplicationContext()).load(photo).into(ivImage);
                }else {
                    ivImage.setImageResource(R.drawable.logo_jogja);
                }

                alert.setView(view);
                alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alert.show();

//                // inflate the layout of the popup window
//                LayoutInflater inflater = (LayoutInflater)
//                        getSystemService(LAYOUT_INFLATER_SERVICE);
//                View popupView = inflater.inflate(R.layout.my_profile, null);
//
//                // create the popup window
//                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
//                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
//                boolean focusable = true; // lets taps outside the popup also dismiss it
//                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
//
//                // show the popup window
//                // which view you pass in doesn't matter, it is only used for the window tolken
//                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
//
//                // dismiss the popup window when touched
//                popupView.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        popupWindow.dismiss();
//                        return true;
//                    }
//                });

                return true;
            case R.id.share:
                String titleApp = "Yogyakarta Tourism App";
                String urlApp = "https://google.com";

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, titleApp);
                shareIntent.putExtra(Intent.EXTRA_TEXT, urlApp);
                startActivity(Intent.createChooser(shareIntent, "Share Yogyakarta Tourism App!"));
                return true;
            case R.id.logOut:

                DialogInterface.OnClickListener dialogInterface = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                        new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(Status status) {
                                                SharedPreferences mUser = MainActivity.this.getSharedPreferences("user", Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editorUser = mUser.edit();
                                                editorUser.clear();
                                                editorUser.commit();

                                                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:

                                break;

                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are You Sure Want To Log Out ?")
                        .setPositiveButton("Yes", dialogInterface)
                        .setNegativeButton("No", dialogInterface).show();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void getDataSuccess(String message, final List<TourismList> tourismLists) {

        Log.d("Data Success => ", message);

        mAdapter = new TourismAdapter(getApplicationContext(),tourismLists,R.layout.list_item_tourism);
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("name", tourismLists.get(position).getNamaPariwisata());
                intent.putExtra("address", tourismLists.get(position).getAlamatPariwisata());
                intent.putExtra("detail", tourismLists.get(position).getDetailPariwisata());
                intent.putExtra("image", tourismLists.get(position).getGambarPariwisata());

                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

    @Override
    public void getDataFailure(String message) {

        Log.d("Data Failure => ", message);

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    public void viewGoneOne() {

        tvEmptyView.setVisibility(View.GONE);
        vLoadingBg.setVisibility(View.VISIBLE);
        vProgressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

    }

    public void viewGoneTwo() {
        vProgressBar.setVisibility(View.GONE);
        vLoadingBg.setVisibility(View.GONE);
        tvEmptyView.setVisibility(View.GONE);

        recyclerView.setVisibility(View.VISIBLE);
    }

    public void viewGoneThree() {
        vProgressBar.setVisibility(View.GONE);
        vLoadingBg.setVisibility(View.GONE);

        tvEmptyView.setVisibility(View.VISIBLE);
        tvEmptyView.setText("Cannot Load The Data, Please Check Your Internet Connection, or Your Settings.");

        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
