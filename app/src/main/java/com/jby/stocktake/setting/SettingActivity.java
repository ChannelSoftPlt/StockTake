package com.jby.stocktake.setting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.transition.Explode;
import android.transition.Fade;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.stocktake.R;
import com.jby.stocktake.others.SquareHeightLinearLayout;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        QuickScanDialog.QuickScanDialogCallBack, DeviceNameDialog.DeviceNameDialogCallback {
    SwitchCompat settingFragmentScanSoundSwitchButton, settingFragmentReminderSwitchButton, settingFragmentQuickScanSwitchButton;
    RelativeLayout settingFragmentLogOutButton,settingFragmentQuickScanButton, settingFragmentMyAccount, settingFragmentContactUs, settingActivityDeviceName;
    TextView settingFragmentQuickScanQuantity, settingActivityTextViewDeviceName;
    private TextView actionBarTitle, settingFragmentVersionName;
    private SquareHeightLinearLayout actionBarSearch, actionbarSetting, actionbarBackButton, actionBarCancel;
    private ProgressDialog pd;
    private int resultCode = 0;
    Intent intent;

    DialogFragment dialogFragment;
    FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        settingFragmentScanSoundSwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_scan_sound_button);
        settingFragmentReminderSwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_reminder_button);
        settingFragmentQuickScanSwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_quick_scan_switch_button);
        settingFragmentLogOutButton = (RelativeLayout) findViewById(R.id.fragment_setting_log_out_button);
        settingFragmentQuickScanButton = (RelativeLayout) findViewById(R.id.fragment_setting_quick_scan_button);
        settingFragmentMyAccount = (RelativeLayout)findViewById(R.id.fragment_setting_user_account);
        settingActivityDeviceName = (RelativeLayout)findViewById(R.id.fragment_setting_user_device);

        settingFragmentQuickScanQuantity = (TextView) findViewById(R.id.fragment_setting_quick_scan_quantity);
        settingActivityTextViewDeviceName = (TextView) findViewById(R.id.activity_setting_device_name);

        settingFragmentVersionName = (TextView) findViewById(R.id.fragment_setting_version_name);
        settingFragmentContactUs = (RelativeLayout)findViewById(R.id.fragment_setting_contact_us);
//        action bar
        actionBarTitle = (TextView)findViewById(R.id.actionBar_title);
        actionBarSearch = (SquareHeightLinearLayout)findViewById(R.id.actionBar_search);
        actionbarSetting = (SquareHeightLinearLayout)findViewById(R.id.actionBar_setting);
        actionbarBackButton = (SquareHeightLinearLayout)findViewById(R.id.actionBar_back_button);
        fm = getSupportFragmentManager();
        pd = new ProgressDialog(this);

    }

    private void objectSetting() {
        settingFragmentLogOutButton.setOnClickListener(this);
        settingFragmentQuickScanButton.setOnClickListener(this);
        settingFragmentMyAccount.setOnClickListener(this);
        settingFragmentContactUs.setOnClickListener(this);
        settingActivityDeviceName.setOnClickListener(this);
        settingFragmentScanSoundSwitchButton.setOnCheckedChangeListener(this);
        settingFragmentReminderSwitchButton.setOnCheckedChangeListener(this);
        settingFragmentQuickScanSwitchButton.setOnCheckedChangeListener(this);
        actionbarBackButton.setOnClickListener(this);
        actionBarTitle.setText(R.string.actionbar_setting_title);
        actionBarSearch.setVisibility(View.GONE);
        actionbarSetting.setVisibility(View.GONE);
        actionbarBackButton.setVisibility(View.VISIBLE);
        preSetting();
        pd.setMessage("Loading...");
        pd.setCancelable(false);
//        version name
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = "Version " + pInfo.versionName;
            settingFragmentVersionName.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_setting_log_out_button:
                alertMessage();
                break;

            case R.id.fragment_setting_quick_scan_button:
                if(SharedPreferenceManager.getQuickScan(this).equals("0")){
                    popOutDialog();
                }
                else{
                    settingFragmentQuickScanSwitchButton.setChecked(false);
                    SharedPreferenceManager.setQuickScan(this,"0");
                }
                break;
            case R.id.actionBar_back_button:
                clickEffect(actionbarBackButton);
                finish();
                break;
            case R.id.fragment_setting_user_account:
                clickEffect(settingFragmentMyAccount);
                intent = new Intent(this, UserAccountActivity.class);
                startActivity(intent);
                break;
            case R.id.fragment_setting_contact_us:
                clickEffect(settingFragmentContactUs);
                intent = new Intent(this, ContactUsActivity.class);
                startActivity(intent);
                break;
            case R.id.fragment_setting_user_device:
                clickEffect(settingActivityDeviceName);
                popOutDeviceNameDialog();
                break;
        }
    }


    public void popOutDialog(){
        dialogFragment = new QuickScanDialog();
        dialogFragment.show(fm, "");
    }

    public void popOutDeviceNameDialog(){
        dialogFragment = new DeviceNameDialog();
        dialogFragment.show(fm, "");
    }


    public void quickScanSetting(){

        if(!SharedPreferenceManager.getQuickScan(this).equals("0")){
            settingFragmentQuickScanSwitchButton.setChecked(true);
        }
        String quickScanQuantity = "default: "+ SharedPreferenceManager.getQuickScanQuantity(this);
        settingFragmentQuickScanQuantity.setText(quickScanQuantity);

    }

    public void deviceNameSetting(){
        String deviceName = SharedPreferenceManager.getDeviceName(this);
        settingActivityTextViewDeviceName.setText(deviceName);
    }

    @Override
    public void checkingSetting() {

    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.fragment_setting_reminder_button:
                reminderSetting(b);
                break;

            case R.id.fragment_setting_scan_sound_button:
                scanSoundSetting(b);
                break;

        }
    }

    public void reminderSetting(boolean b){
        if (b)
            SharedPreferenceManager.setReminder(this, "1");
        else
            SharedPreferenceManager.setReminder(this, "0");
    }


    public void scanSoundSetting(boolean b){
        if (b)
            SharedPreferenceManager.setScanSound(this, "1");
        else
            SharedPreferenceManager.setScanSound(this, "0");
    }

    public void preSetting(){
        if(SharedPreferenceManager.getQuickScan(this).equals("1"))
            settingFragmentQuickScanSwitchButton.setChecked(true);
        else
            settingFragmentQuickScanSwitchButton.setChecked(false);

        if(SharedPreferenceManager.getReminder(this).equals("1"))
            settingFragmentReminderSwitchButton.setChecked(true);
        else
            settingFragmentReminderSwitchButton.setChecked(false);

        if(SharedPreferenceManager.getScanSound(this).equals("1"))
            settingFragmentScanSoundSwitchButton.setChecked(true);
        else
            settingFragmentScanSoundSwitchButton.setChecked(false);


        String quickScanQuantity = "default: "+ SharedPreferenceManager.getQuickScanQuantity(this);
        settingFragmentQuickScanQuantity.setText(quickScanQuantity);
        String deviceName = SharedPreferenceManager.getDeviceName(this);
        settingActivityTextViewDeviceName.setText(deviceName);
    }

    public void alertMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Log Out");
        builder.setMessage("Are you sure you want to log out?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        resultCode = 3;
                        onBackPressed();
                    }
                });
        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(resultCode, intent);
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.fade_out);
    }

    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }
}
