package com.jby.stocktake.home;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.jby.stocktake.R;
import com.jby.stocktake.exportFeature.file.ExportFileActivity;
import com.jby.stocktake.importFeature.file.ImportFileActivity;
import com.jby.stocktake.login.LoginActivity;
import com.jby.stocktake.others.SquareHeightLinearLayout;
import com.jby.stocktake.setting.DeviceNameDialog;
import com.jby.stocktake.setting.SettingActivity;
import com.jby.stocktake.shareObject.ApiDataObject;
import com.jby.stocktake.shareObject.ApiManager;
import com.jby.stocktake.shareObject.AsyncTaskManager;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class HomeActivity extends AppCompatActivity implements OnClickListener,
        DeviceNameDialog.DeviceNameDialogCallback{
    private ImageView homeActivityExport, homeActivityImport;
    private SquareHeightLinearLayout homeActivitySettingButton;
    Intent intent;
    boolean exit = false;
    FragmentManager fm;
    DialogFragment dialogFragment;
    Bundle bundle;
    int checkUserActivation = 0;

    ProgressDialog pd;
    Handler handler;
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        homeActivityExport = (ImageView)findViewById(R.id.activity_home_export);
        homeActivityImport = (ImageView)findViewById(R.id.activity_home_import);
        homeActivitySettingButton = (SquareHeightLinearLayout)findViewById(R.id.actionBar_setting);
        pd = new ProgressDialog(this);
        handler = new Handler();
        fm = getSupportFragmentManager();
    }
    private void objectSetting() {
        homeActivityExport.setOnClickListener(this);
        homeActivityImport.setOnClickListener(this);
        homeActivitySettingButton.setOnClickListener(this);
        checkingSetting();
//        muteDefaultSound();
    }

    public void checkingSetting(){
        String deviceName = SharedPreferenceManager.getDeviceName(this);
        if(!deviceName.equals("")){
            Bundle bundle = getIntent().getExtras();
            if(bundle != null)
                checkUserActivation = bundle.getInt("status");

            if(checkUserActivation == 0)
            {
                if(isNetworkAvailable(this)){
                    pd.setMessage("Loading...");
                    startUp(checkUserActivation);
                }
                else
                    Toast.makeText(this, "No Network Available", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            popOutDeviceNameDialog();
        }
    }

    public void popOutDeviceNameDialog(){
        dialogFragment = new DeviceNameDialog();
        bundle = new Bundle();
        bundle.putString("from", "home");
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    public void startUp(int status){
        if(status != 1){
            pd.show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkUserActivation();
                }
            },300);
        }
    }

    public void muteDefaultSound(){
        AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if(audio != null)
            audio.setRingerMode(0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activity_home_export:
                clickEffect(homeActivityExport);
                intent = new Intent(this, ExportFileActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.activity_home_import:
                clickEffect(homeActivityImport);
                intent = new Intent(this, ImportFileActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.actionBar_setting:
                intent = new Intent(this, SettingActivity.class);
                startActivityForResult(intent, 3);
                break;
        }
    }


    public void exit(){
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

    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    public void checkUserActivation(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("token", SharedPreferenceManager.getDeviceToken(this)));
        apiDataObjectArrayList.add(new ApiDataObject("version", SharedPreferenceManager.getVersion(this)));

        Log.d("HomeActivity", "user_id" +SharedPreferenceManager.getUserID(this));
        Log.d("HomeActivity", "version" +SharedPreferenceManager.getVersion(this));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().home,
                new ApiManager().getResultParameter(
                        "",
                        new ApiManager().setData(apiDataObjectArrayList),
                        ""
                )
        );
        doingInBackground();
    }

    public void doingInBackground(){

        asyncTaskManager.execute();

        if (!asyncTaskManager.isCancelled()) {
            try {
                jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);

                if (jsonObjectLoginResponse != null) {
                    int userPackage = jsonObjectLoginResponse.getInt("package");
                    SharedPreferenceManager.setUserPackage(this, userPackage);

                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        pd.dismiss();

                    }
                    else if (jsonObjectLoginResponse.getString("status").equals("2")) {
                        almostExpired();

                    }
                    else if (jsonObjectLoginResponse.getString("status").equals("3")) {
                        expiredDialog();

                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("4")){
                        Toast.makeText(this, "Something error with server! Try it later!", Toast.LENGTH_SHORT).show();
                    }
                    else if (jsonObjectLoginResponse.getString("status").equals("5")) {
                        getNewVersion(jsonObjectLoginResponse.getString("url"));
                    }
                }
                else {
                    Toast.makeText(this, "Network Error!", Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException e) {
                Toast.makeText(this, "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(this, "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(this, "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(this, "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        if(pd.isShowing())
            pd.dismiss();
    }

    public void almostExpired() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notice");
        builder.setMessage("Hi, your member activation period is almost to has expired! Remember to renew it before your account is expired!");
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

    public void expiredDialog(){
        dialogFragment = new HomeExpireDialog();
        dialogFragment.show(fm, "");
    }

    public void getNewVersion(final String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notice");
        builder.setMessage("A new version is ready now!");
        builder.setCancelable(false);

        builder.setPositiveButton(
                "Get It Now",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(resultCode == 3)
        {
            logOutSetting();
        }
    }

    public void logOutSetting(){
        SharedPreferenceManager.setUserID(this, "default");
        SharedPreferenceManager.setUserPassword(this, "default");
        SharedPreferenceManager.setDeviceToken(this, "default");
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    Log.w("INTERNET:",String.valueOf(i));
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        Log.w("INTERNET:", "connected!");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void deviceNameSetting() {

    }
}
