package com.jby.stocktake.setting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.R;
import com.jby.stocktake.login.LoginActivity;
import com.jby.stocktake.others.SquareHeightLinearLayout;
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

public class UserAccountActivity extends AppCompatActivity implements View.OnClickListener {
    TextView userAccountActivityUserStatus, userAccountActivityUserPackage, userAccountActivityUserLastActiveDate, userAccountActivityUserExpireDate;
    TextView userAccountActivityContactUs, userAccountActivityUserName, userAccountActivityUserEmail;
    private TextView actionBarTitle;
    ImageView actionBarSearch, actionbarSetting, actionbarBackButton, actionBarCancel;
    ProgressDialog pd;
    Handler handler;
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        userAccountActivityUserStatus = (TextView)findViewById(R.id.activity_user_account_user_status);
        userAccountActivityUserPackage = (TextView)findViewById(R.id.activity_user_account_user_package);
        userAccountActivityUserLastActiveDate = (TextView)findViewById(R.id.activity_user_account_active_date);
        userAccountActivityUserExpireDate = (TextView)findViewById(R.id.activity_user_account_expired_date);
        userAccountActivityContactUs = (TextView)findViewById(R.id.activity_user_account_button_contact_us);
        userAccountActivityUserEmail = (TextView)findViewById(R.id.activity_user_account_email);
        userAccountActivityUserName = (TextView)findViewById(R.id.activity_user_account_username);

        actionBarTitle = (TextView)findViewById(R.id.actionBar_title);
        actionBarSearch = findViewById(R.id.actionBar_search);
        actionbarSetting = findViewById(R.id.actionBar_setting);
        actionbarBackButton = findViewById(R.id.actionBar_back_button);

        handler = new Handler();
        pd = new ProgressDialog(this);
    }

    private void objectSetting() {
        actionBarTitle.setText(R.string.activity_user_actionbar_label);
        actionBarSearch.setVisibility(View.GONE);
        actionbarSetting.setVisibility(View.GONE);
        actionbarBackButton.setVisibility(View.VISIBLE);
        userAccountActivityContactUs.setOnClickListener(this);
        actionbarBackButton.setOnClickListener(this);
        checkUserId();
        pd.setMessage("Loading...");
        pd.show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUserPackageDetail();
            }
        },300);

    }
    public void checkUserId(){
        if(SharedPreferenceManager.getUserID(this).equals("default")){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void checkUserPackageDetail(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().userAccount,
                new ApiManager().getResultParameter(
                        "",
                        new ApiManager().setData(apiDataObjectArrayList),
                        ""
                )
        );
        asyncTaskManager.execute();

        if (!asyncTaskManager.isCancelled()) {
            try {
                jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);

                if (jsonObjectLoginResponse != null) {
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        String activeDate = "Last active on " + jsonObjectLoginResponse.getString("active_date");
                        String expiredDate = "Expires on " + jsonObjectLoginResponse.getString("expired_date");
                        String userPackage = jsonObjectLoginResponse.getString("package");
                        String userActivation = jsonObjectLoginResponse.getString("user_activation");
                        String userName = jsonObjectLoginResponse.getString("username");
                        String userEmail = jsonObjectLoginResponse.getString("email");

                        userAccountActivityUserLastActiveDate.setText(activeDate);
                        userAccountActivityUserExpireDate.setText(expiredDate);
                        userAccountActivityUserEmail.setText(userEmail);
                        userAccountActivityUserName.setText(userName);

//                        setup user package
                        if(userPackage.equals("2"))
                            userPackage = "Standard Package";
                        else
                            userPackage = "Premium Package";
                        userAccountActivityUserPackage.setText(userPackage);

//                        setup user status
                        if(userActivation.equals("1"))
                        {
                            userActivation = "Active";
                        }
                        else
                        {
                            userActivation = "Expired";
                            userAccountActivityUserStatus.setTextColor(getResources().getColor(R.color.warning));
                        }

                        userAccountActivityUserStatus.setText(userActivation);

                    }

                    else if(jsonObjectLoginResponse.getString("status").equals("4")){
                        Toast.makeText(this, "Something error with server! Try it later!", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.activity_user_account_button_contact_us:
                clickEffect(userAccountActivityContactUs);
                Intent intent = new Intent(this, ContactUsActivity.class);
                startActivity(intent);
                break;
            case R.id.actionBar_back_button:
                finish();
                break;
        }
    }

    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }
}
