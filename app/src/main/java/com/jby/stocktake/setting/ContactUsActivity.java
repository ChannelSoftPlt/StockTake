package com.jby.stocktake.setting;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.R;
import com.jby.stocktake.others.SquareHeightLinearLayout;

public class ContactUsActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView contactUsActivityCallUs, contactUsActivityEmailUs;
    private TextView actionBarTitle;
    private SquareHeightLinearLayout actionBarSearch, actionbarSetting, actionbarBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {


        actionBarTitle = (TextView)findViewById(R.id.actionBar_title);
        actionBarSearch = (SquareHeightLinearLayout)findViewById(R.id.actionBar_search);
        actionbarSetting = (SquareHeightLinearLayout)findViewById(R.id.actionBar_setting);
        actionbarBackButton = (SquareHeightLinearLayout)findViewById(R.id.actionBar_back_button);

        contactUsActivityCallUs = (TextView)findViewById(R.id.activity_contact_us_call_us);
        contactUsActivityEmailUs = (TextView)findViewById(R.id.activity_contact_us_email_us);
    }

    private void objectSetting() {
        actionBarTitle.setText(R.string.activity_contact_us_title);
        actionBarSearch.setVisibility(View.GONE);
        actionbarSetting.setVisibility(View.GONE);
        actionbarBackButton.setVisibility(View.VISIBLE);

        actionbarBackButton.setOnClickListener(this);
        contactUsActivityCallUs.setOnClickListener(this);
        contactUsActivityEmailUs.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.actionBar_back_button:
                finish();
                break;
            case R.id.activity_contact_us_call_us:
                phoneCallPermission();
                break;
            case R.id.activity_contact_us_email_us:
                composeEmail();
                break;
        }
    }


    public static final int MY_PERMISSIONS_REQUEST_PHONE_CALL = 10;

    public boolean checkPhoneCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                ActivityCompat.requestPermissions(ContactUsActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_PHONE_CALL);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_PHONE_CALL);
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
            case MY_PERMISSIONS_REQUEST_PHONE_CALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CALL_PHONE)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        onCall();
                    }
                } else {
                    Toast.makeText(this, "Unable to make a phone call with permission!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void phoneCallPermission() {
        if (checkPhoneCallPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                onCall();
            }
        }
    }

    public void onCall() {
        String phoneNo = contactUsActivityCallUs.getText().toString().trim();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+phoneNo));    //this is the phone number calling

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //request permission from user if the app hasn't got the required permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},   //request specific permission from user
                    MY_PERMISSIONS_REQUEST_PHONE_CALL);
        }else {     //have got permission
            try{
                startActivity(callIntent);  //call activity and make phone call
            }
            catch (android.content.ActivityNotFoundException ex){
                Toast.makeText(this,"Invalid Number",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void composeEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"chaforteam@gmailcom"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Help");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
