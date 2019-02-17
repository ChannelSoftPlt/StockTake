package com.jby.stocktake.login;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.jby.stocktake.exportFeature.file.ExportFileActivity;
import com.jby.stocktake.R;
import com.jby.stocktake.others.CustomViewPager;
import com.jby.stocktake.others.ViewPagerAdapter;
import com.jby.stocktake.others.ViewPagerObject;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import me.pushy.sdk.Pushy;

public class LoginActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private CustomViewPager loginActivityViewPager;
    ViewPagerAdapter loginActivityViewPagerAdapter;
    private ArrayList<ViewPagerObject> loginActivityViewPagerArrayList;
    boolean exit = false;
    Fragment fragment, fragment2, fragment3;
    LoginFragment loginFragment;
    RegisterFragment registerFragment;
    ForgotPasswordFragment forgotPasswordFragment;
    private String imei;
    private int page = 0;
    private boolean isLoad = false;
    public static final int MY_PERMISSIONS_REQUEST_READ_PHONE = 99;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 98;


    @Override
    public void onBackPressed() {
        if(loginActivityViewPager.getCurrentItem() == 2){

            forgotPasswordFragment.checkCurrentState();
        }
        else{
            if (loginActivityViewPager.getCurrentItem() != 0) {
                setCurrentPage(0);
            } else {
                exit();
            }
        }
    }

    public void exit() {
        if (exit) {
            System.exit(0); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Pushy.listen(this);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        loginActivityViewPager = (CustomViewPager) findViewById(R.id.activity_login_viewpager);
        loginActivityViewPagerArrayList = new ArrayList<>();

    }

    private void objectSetting() {
        loginActivityViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), loginActivityViewPagerArrayList);
        loginActivityViewPager.setAdapter(loginActivityViewPagerAdapter);
        loginActivityViewPager.setOnPageChangeListener(this);
        if(SharedPreferenceManager.getDeviceToken(this).equals("default"))
            new RegisterForPushNotificationsAsync(this).execute();
        setVersion();
    }

    public void setVersion(){
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String currentVersion = SharedPreferenceManager.getVersion(this);
            if(!currentVersion.equals(pInfo.packageName))
                SharedPreferenceManager.setVersion(this, pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onStart() {
        super.onStart();
        if (!SharedPreferenceManager.getUserID(this).equals("default")) {
            Intent i = new Intent(this, ExportFileActivity.class);
            startActivity(i);
            finish();
        }
        if(!isLoad){
            setPager();
            setCurrentPage(0);
            checkWriteExternalPermission();
            isLoad = true;
        }
    }

    public void checkWriteExternalPermission(){
        // Check whether the user has granted us the READ/WRITE_EXTERNAL_STORAGE permissions
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request both READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE so that the
            // Pushy SDK will be able to persist the device token in the external storage
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    public void setPager() {
        loginActivityViewPagerArrayList.add(new ViewPagerObject(new LoginFragment(), "Login"));
        loginActivityViewPagerArrayList.add(new ViewPagerObject(new RegisterFragment(), "Register"));
        loginActivityViewPagerArrayList.add(new ViewPagerObject(new ForgotPasswordFragment(), "Forgot"));

        fragment = loginActivityViewPagerAdapter.getItem(0);
        loginFragment = (LoginFragment) fragment;

        fragment2 = loginActivityViewPagerAdapter.getItem(1);
        registerFragment = (RegisterFragment) fragment2;

        fragment3 = loginActivityViewPagerAdapter.getItem(2);
        forgotPasswordFragment = (ForgotPasswordFragment)fragment3;

        loginActivityViewPager.setAdapter(loginActivityViewPagerAdapter);
        loginActivityViewPager.setCanScroll(false);
    }

    public void setCurrentPage(int page) {
        loginActivityViewPager.setCurrentItem(page);
    }

    public String getIMEI() {
        imei = "null";
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED){

            TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            if(telephonyManager != null)
                imei = telephonyManager.getDeviceId();
        }
        return imei;
    }



    public void alertMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bad Request");
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton(
                "I Got It",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            loginFragment.setupLogo(true);
            registerFragment.setupLogo(false);
        } else {
            loginFragment.setupLogo(false);
            registerFragment.setupLogo(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    public boolean checkReadStatePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                ActivityCompat.requestPermissions(LoginActivity.this,
                        new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE);

            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_PHONE_STATE)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        if(page == 1)
                            loginFragment.checkingInput();
                        else if(page == 2)
                            registerFragment.checkingInput();

                    }
                } else {
                   checkReadStatePermission();
                }
            }
            break;
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkReadStatePermission();
                }
                else
                   checkWriteExternalPermission();
            }
        }
    }

    public void readPhonePermission(int page) {
        this.page = page;
        if (checkReadStatePermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                if(page == 1)
                    loginFragment.checkingInput();
                else
                    registerFragment.checkingInput();
            }
        }
    }

    private static class RegisterForPushNotificationsAsync extends AsyncTask<Void, Void, Exception> {

        private WeakReference<LoginActivity> activityReference;

        // only retain a weak reference to the activity
        RegisterForPushNotificationsAsync(LoginActivity context) {
            activityReference = new WeakReference<>(context);
        }
        protected Exception doInBackground(Void... params) {
            try {
                LoginActivity activity = activityReference.get();
                // Assign a unique token to this device
                String deviceToken = Pushy.register(activity);
                SharedPreferenceManager.setDeviceToken(activity, deviceToken);
                Log.d("HomeActivity","Device Token: " +deviceToken);

            }
            catch (Exception exc) {
                // Return exc to onPostExecute
                return exc;
            }

            // Success
            return null;
        }

        @Override
        protected void onPostExecute(Exception exc) {
            // Failed?
            if (exc != null) {
                LoginActivity activity = activityReference.get();
                // Show error as toast message
                Toast.makeText(activity, exc.toString(), Toast.LENGTH_LONG).show();
            }

            // Succeeded, do something to alert the user
        }
    }
}
