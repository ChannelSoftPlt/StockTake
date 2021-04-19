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
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.stocktake.R;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        QuickScanDialog.QuickScanDialogCallBack, DeviceNameDialog.DeviceNameDialogCallback, ExportSettingDialog.ExportSettingDialogCallBack {

    SwitchCompat settingFragmentScanSoundSwitchButton, settingFragmentReminderSwitchButton, settingFragmentQuickScanSwitchButton;
    SwitchCompat settingFragmentExportDateSwitchButton, settingFragmentExportTimeSwitchButton, settingFragmentExportCategorySwitchButton;
    SwitchCompat settingFragmentExportItemNoSwitchButton, settingFragmentExportDescriptionSwitchButton, settingFragmentExportBarcodeSwitchButton;
    SwitchCompat settingFragmentExportSystemQuantitySwitchButton, settingFragmentExportCheckQuantitySwitchButton;
    SwitchCompat settingFragmentExportSellingPriceSwitchButton, settingFragmentExportCostPriceSwitchButton, settingFragmentDecimalSwitchButton;

    RelativeLayout settingFragmentLogOutButton, settingFragmentQuickScanButton, settingFragmentMyAccount, settingFragmentContactUs, settingActivityDeviceName;
    RelativeLayout settingFragmentExportCatgoryButton, settingFragmentExportItemNoButton, settingFragmentExportDescriptionButton;
    RelativeLayout settingFragmentExportBarcodeButton, settingFragmentExportSystemQuantityButton, settingFragmentExportCheckQuantityButton;
    RelativeLayout settingFragmentExportDateButton, settingFragmentExportTimeButton;
    RelativeLayout settingFragmentExportSellingPriceButton, settingFragmentExportCostPriceButton;
    TextView settingFragmentQuickScanQuantity, settingActivityTextViewDeviceName;

    TextView settingFragmentDefaultExportCategoryValue, settingFragmentDefaultExportItemCodeValue, settingFragmentDefaultExportDescriptionValue;
    TextView settingFragmentDefaultExportBarcodeValue, settingFragmentDefaultExportSystemQuantityValue, settingFragmentDefaultExportCheckQuantityValue;
    TextView settingFragmentDefaultExportDateValue, settingFragmentDefaultExportTimeValue;
    TextView settingFragmentDefaultExportSellingPriceValue, settingFragmentDefaultExportCostPriceValue;
    private TextView actionBarTitle, settingFragmentVersionName;
    private ImageView actionBarSearch, actionbarSetting, actionbarBackButton, actionBarCancel;
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
        settingFragmentExportDateSwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_export_date_switch_button);
        settingFragmentExportTimeSwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_export_time_switch_button);

        settingFragmentExportItemNoSwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_export_item_code_switch_button);
        settingFragmentExportDescriptionSwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_export_description_switch_button);
        settingFragmentExportBarcodeSwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_export_barcode_switch_button);
        settingFragmentExportCategorySwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_export_category_switch_button);
        settingFragmentExportSystemQuantitySwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_export_system_quantity_switch_button);
        settingFragmentExportCheckQuantitySwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_export_check_quantity_switch_button);
        settingFragmentExportSellingPriceSwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_export_selling_price_switch_button);
        settingFragmentExportCostPriceSwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_export_cost_price_switch_button);
        settingFragmentDecimalSwitchButton = (SwitchCompat) findViewById(R.id.fragment_setting_decimal_button);

        settingFragmentLogOutButton = (RelativeLayout) findViewById(R.id.fragment_setting_log_out_button);
        settingFragmentQuickScanButton = (RelativeLayout) findViewById(R.id.fragment_setting_quick_scan_button);

        settingFragmentMyAccount = (RelativeLayout) findViewById(R.id.fragment_setting_user_account);
        settingActivityDeviceName = (RelativeLayout) findViewById(R.id.fragment_setting_user_device);

        settingFragmentExportCatgoryButton = (RelativeLayout) findViewById(R.id.fragment_setting_export_category);
        settingFragmentExportItemNoButton = (RelativeLayout) findViewById(R.id.fragment_setting_export_item_code);
        settingFragmentExportDescriptionButton = (RelativeLayout) findViewById(R.id.fragment_setting_export_description);
        settingFragmentExportBarcodeButton = (RelativeLayout) findViewById(R.id.fragment_setting_export_barcode);
        settingFragmentExportSystemQuantityButton = (RelativeLayout) findViewById(R.id.fragment_setting_export_system_quantity);
        settingFragmentExportCheckQuantityButton = (RelativeLayout) findViewById(R.id.fragment_setting_export_check_quantity);
        settingFragmentExportDateButton = (RelativeLayout) findViewById(R.id.fragment_setting_export_date_button);
        settingFragmentExportTimeButton = (RelativeLayout) findViewById(R.id.fragment_setting_export_time_button);
        settingFragmentExportCostPriceButton = (RelativeLayout) findViewById(R.id.fragment_setting_export_cost_price_button);
        settingFragmentExportSellingPriceButton = (RelativeLayout) findViewById(R.id.fragment_setting_export_selling_price_button);

        settingFragmentQuickScanQuantity = (TextView) findViewById(R.id.fragment_setting_quick_scan_quantity);
        settingActivityTextViewDeviceName = (TextView) findViewById(R.id.activity_setting_device_name);

        settingFragmentDefaultExportCategoryValue = (TextView) findViewById(R.id.fragment_setting_default_export_category);
        settingFragmentDefaultExportItemCodeValue = (TextView) findViewById(R.id.fragment_setting_default_export_item_code);
        settingFragmentDefaultExportDescriptionValue = (TextView) findViewById(R.id.fragment_setting_default_export_description);
        settingFragmentDefaultExportBarcodeValue = (TextView) findViewById(R.id.fragment_setting_default_export_barcode);
        settingFragmentDefaultExportSystemQuantityValue = (TextView) findViewById(R.id.fragment_setting_default_export_system_quantity);
        settingFragmentDefaultExportCheckQuantityValue = (TextView) findViewById(R.id.fragment_setting_default_export_check_quantity);
        settingFragmentDefaultExportDateValue = (TextView) findViewById(R.id.fragment_setting_default_export_date);
        settingFragmentDefaultExportTimeValue = (TextView) findViewById(R.id.fragment_setting_default_export_time);
        settingFragmentDefaultExportSellingPriceValue = (TextView) findViewById(R.id.fragment_setting_default_export_selling_price);
        settingFragmentDefaultExportCostPriceValue = (TextView) findViewById(R.id.fragment_setting_default_export_cost_price);

        settingFragmentVersionName = (TextView) findViewById(R.id.fragment_setting_version_name);
        settingFragmentContactUs = (RelativeLayout) findViewById(R.id.fragment_setting_contact_us);
//        action bar
        actionBarTitle = (TextView) findViewById(R.id.actionBar_title);
        actionBarSearch = findViewById(R.id.actionBar_search);
        actionbarSetting = findViewById(R.id.actionBar_setting);
        actionbarBackButton = findViewById(R.id.actionBar_back_button);
        fm = getSupportFragmentManager();
        pd = new ProgressDialog(this);

    }

    private void objectSetting() {
        settingFragmentLogOutButton.setOnClickListener(this);
        settingFragmentQuickScanButton.setOnClickListener(this);
        settingFragmentMyAccount.setOnClickListener(this);
        settingFragmentContactUs.setOnClickListener(this);
        settingActivityDeviceName.setOnClickListener(this);

        settingFragmentExportCatgoryButton.setOnClickListener(this);
        settingFragmentExportItemNoButton.setOnClickListener(this);
        settingFragmentExportDescriptionButton.setOnClickListener(this);
        settingFragmentExportBarcodeButton.setOnClickListener(this);
        settingFragmentExportSystemQuantityButton.setOnClickListener(this);
        settingFragmentExportCheckQuantityButton.setOnClickListener(this);
        settingFragmentExportDateButton.setOnClickListener(this);
        settingFragmentExportTimeButton.setOnClickListener(this);
        settingFragmentExportSellingPriceButton.setOnClickListener(this);
        settingFragmentExportCostPriceButton.setOnClickListener(this);

        settingFragmentScanSoundSwitchButton.setOnCheckedChangeListener(this);
        settingFragmentReminderSwitchButton.setOnCheckedChangeListener(this);
        settingFragmentQuickScanSwitchButton.setOnCheckedChangeListener(this);
        settingFragmentExportTimeSwitchButton.setOnCheckedChangeListener(this);
        settingFragmentExportDateSwitchButton.setOnCheckedChangeListener(this);
        settingFragmentDecimalSwitchButton.setOnCheckedChangeListener(this);
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
        switch (view.getId()) {
            case R.id.fragment_setting_log_out_button:
                alertMessage();
                break;
            case R.id.fragment_setting_quick_scan_button:
                if (SharedPreferenceManager.getQuickScan(this).equals("0")) {
                    popOutDialog();
                } else {
                    settingFragmentQuickScanSwitchButton.setChecked(false);
                    SharedPreferenceManager.setQuickScan(this, "0");
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
            case R.id.fragment_setting_export_category:
                checkExportSetting("Category");
                break;
            case R.id.fragment_setting_export_item_code:
                checkExportSetting("ItemCode");
                break;
            case R.id.fragment_setting_export_description:
                checkExportSetting("Description");
                break;
            case R.id.fragment_setting_export_barcode:
                checkExportSetting("Barcode");
                break;
            case R.id.fragment_setting_export_cost_price_button:
                checkExportSetting("CostPrice");
                break;
            case R.id.fragment_setting_export_selling_price_button:
                checkExportSetting("SellingPrice");
                break;
            case R.id.fragment_setting_export_system_quantity:
                checkExportSetting("SystemQuantity");
                break;
            case R.id.fragment_setting_export_check_quantity:
                checkExportSetting("CheckQuantity");
                break;
            case R.id.fragment_setting_export_date_button:
                checkExportSetting("Date");
                break;
            case R.id.fragment_setting_export_time_button:
                checkExportSetting("Time");
                break;
        }
    }

    public void popOutDialog() {
        dialogFragment = new QuickScanDialog();
        dialogFragment.show(fm, "");
    }

    public void popOutDeviceNameDialog() {
        dialogFragment = new DeviceNameDialog();
        dialogFragment.show(fm, "");
    }

    public void quickScanSetting() {
        if (!SharedPreferenceManager.getQuickScan(this).equals("0")) {
            settingFragmentQuickScanSwitchButton.setChecked(true);
        }
        String quickScanQuantity = "default: " + SharedPreferenceManager.getQuickScanQuantity(this);
        settingFragmentQuickScanQuantity.setText(quickScanQuantity);
    }

    public void deviceNameSetting() {
        String deviceName = SharedPreferenceManager.getDeviceName(this);
        settingActivityTextViewDeviceName.setText(deviceName);
    }

    @Override
    public void checkingSetting() {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.fragment_setting_reminder_button:
                reminderSetting(b);
                break;
            case R.id.fragment_setting_scan_sound_button:
                scanSoundSetting(b);
                break;
            case R.id.fragment_setting_export_date_switch_button:
                exportDateSetting(b);
                break;
            case R.id.fragment_setting_export_time_switch_button:
                exportTimeSetting(b);
                break;
            case R.id.fragment_setting_decimal_button:
                quantityDecimalSetting(b);
                break;
        }
    }

    public void reminderSetting(boolean b) {
        if (b)
            SharedPreferenceManager.setReminder(this, "1");
        else
            SharedPreferenceManager.setReminder(this, "0");
    }

    public void exportDateSetting(boolean b) {
        if (b)
            SharedPreferenceManager.setExportDate(this, "1");
        else
            SharedPreferenceManager.setExportDate(this, "0");
    }

    public void exportTimeSetting(boolean b) {
        if (b)
            SharedPreferenceManager.setExportTime(this, "1");
        else
            SharedPreferenceManager.setExportTime(this, "0");
    }

    public void scanSoundSetting(boolean b) {
        if (b)
            SharedPreferenceManager.setScanSound(this, "1");
        else
            SharedPreferenceManager.setScanSound(this, "0");
    }

    public void quantityDecimalSetting(boolean b) {
        if (b)
            SharedPreferenceManager.setQuantityDecimal(this, "1");
        else
            SharedPreferenceManager.setQuantityDecimal(this, "default");
    }

    public void preSetting() {
        if (SharedPreferenceManager.getQuickScan(this).equals("1"))
            settingFragmentQuickScanSwitchButton.setChecked(true);
        else
            settingFragmentQuickScanSwitchButton.setChecked(false);

        if (SharedPreferenceManager.getReminder(this).equals("1"))
            settingFragmentReminderSwitchButton.setChecked(true);
        else
            settingFragmentReminderSwitchButton.setChecked(false);

        if (SharedPreferenceManager.getScanSound(this).equals("1"))
            settingFragmentScanSoundSwitchButton.setChecked(true);
        else
            settingFragmentScanSoundSwitchButton.setChecked(false);

        if (SharedPreferenceManager.getQuantityDecimal(this).equals("1"))
            settingFragmentDecimalSwitchButton.setChecked(true);
        else
            settingFragmentDecimalSwitchButton.setChecked(false);

        String quickScanQuantity = "default: " + SharedPreferenceManager.getQuickScanQuantity(this);
        settingFragmentQuickScanQuantity.setText(quickScanQuantity);
        String deviceName = SharedPreferenceManager.getDeviceName(this);
        settingActivityTextViewDeviceName.setText(deviceName);
        exportSetting();
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

    public void clickEffect(View view) {
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    /*------------------------------------------------------------------------export setting-------------------------------------------------------------*/
    public void exportSetting() {
        if (!SharedPreferenceManager.getExportField(this, "ExportCategory").equals("0"))
            settingFragmentExportCategorySwitchButton.setChecked(true);
        else
            settingFragmentExportCategorySwitchButton.setChecked(false);
        settingFragmentDefaultExportCategoryValue.setText(defaultValue("ExportCategoryValue"));

        if (!SharedPreferenceManager.getExportField(this, "ExportItemCode").equals("0"))
            settingFragmentExportItemNoSwitchButton.setChecked(true);
        else
            settingFragmentExportItemNoSwitchButton.setChecked(false);
        settingFragmentDefaultExportItemCodeValue.setText(defaultValue("ExportItemCodeValue"));


        if (!SharedPreferenceManager.getExportField(this, "ExportDescription").equals("0"))
            settingFragmentExportDescriptionSwitchButton.setChecked(true);
        else
            settingFragmentExportDescriptionSwitchButton.setChecked(false);
        settingFragmentDefaultExportDescriptionValue.setText(defaultValue("ExportDescriptionValue"));

        if (!SharedPreferenceManager.getExportField(this, "ExportBarcode").equals("0"))
            settingFragmentExportBarcodeSwitchButton.setChecked(true);
        else
            settingFragmentExportBarcodeSwitchButton.setChecked(false);

        settingFragmentDefaultExportBarcodeValue.setText(defaultValue("ExportBarcodeValue"));

        if (!SharedPreferenceManager.getExportField(this, "ExportSellingPrice").equals("0"))
            settingFragmentExportSellingPriceSwitchButton.setChecked(true);
        else
            settingFragmentExportSellingPriceSwitchButton.setChecked(false);
        settingFragmentDefaultExportSellingPriceValue.setText(defaultValue("ExportSellingPriceValue"));

        if (!SharedPreferenceManager.getExportField(this, "ExportCostPrice").equals("0"))
            settingFragmentExportCostPriceSwitchButton.setChecked(true);
        else
            settingFragmentExportCostPriceSwitchButton.setChecked(false);
        settingFragmentDefaultExportCostPriceValue.setText(defaultValue("ExportCostPriceValue"));

        if (!SharedPreferenceManager.getExportField(this, "ExportSystemQuantity").equals("0"))
            settingFragmentExportSystemQuantitySwitchButton.setChecked(true);
        else
            settingFragmentExportSystemQuantitySwitchButton.setChecked(false);
        settingFragmentDefaultExportSystemQuantityValue.setText(defaultValue("ExportSystemQuantityValue"));

        if (!SharedPreferenceManager.getExportField(this, "ExportCheckQuantity").equals("0"))
            settingFragmentExportCheckQuantitySwitchButton.setChecked(true);
        else
            settingFragmentExportCheckQuantitySwitchButton.setChecked(false);
        settingFragmentDefaultExportCheckQuantityValue.setText(defaultValue("ExportCheckQuantityValue"));

        if (!SharedPreferenceManager.getExportField(this, "ExportDate").equals("0"))
            settingFragmentExportDateSwitchButton.setChecked(true);
        else
            settingFragmentExportDateSwitchButton.setChecked(false);
        settingFragmentDefaultExportDateValue.setText(defaultValue("ExportDateValue"));

        if (!SharedPreferenceManager.getExportField(this, "ExportTime").equals("0"))
            settingFragmentExportTimeSwitchButton.setChecked(true);
        else
            settingFragmentExportTimeSwitchButton.setChecked(false);
        settingFragmentDefaultExportTimeValue.setText(defaultValue("ExportTimeValue"));
    }

    private String defaultValue(String key) {
        return SharedPreferenceManager.getExportDefaultValue(this, key);
    }

    private void checkExportSetting(String fieldName){
        Log.d("haha", "haha: " + (SharedPreferenceManager.getExportField(this,"Export" + fieldName)));
        if(SharedPreferenceManager.getExportField(this,"Export" + fieldName).equals("1")){
            SharedPreferenceManager.setExportField(this, "Export" + fieldName, "0");
            exportSetting();
        }
        else openExportSettingDialog(fieldName);
    }

    public void openExportSettingDialog(String fieldName) {
        dialogFragment = new ExportSettingDialog();
        Bundle bundle = new Bundle();
        bundle.putString("field_name", fieldName);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }
}
