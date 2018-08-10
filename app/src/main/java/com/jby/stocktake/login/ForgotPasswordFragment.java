package com.jby.stocktake.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jby.stocktake.R;
import com.jby.stocktake.shareObject.ApiDataObject;
import com.jby.stocktake.shareObject.ApiManager;
import com.jby.stocktake.shareObject.AsyncTaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ForgotPasswordFragment extends Fragment implements View.OnClickListener, ResetPasswordDialog.ResetPasswordDialogCallBack {
    View rootView;
    private EditText forgotPasswordFragmentEditTextEmail;
    private Button forgotPasswordFragmentButtonSend;
    private LinearLayout forgotPasswordFragmentLayoutEnterEmail;
    private String emailFormat;
    private boolean enterEmail = true;
    private ProgressDialog pd;
    private ImageView forgotPasswordFragmentBackButton;

//    verify code purpose
    private LinearLayout forgotPasswordFragmentLayoutEnterCode;
    private EditText forgotPasswordFragmentEditTextCode;
    private Button forgotPasswordFragmentButtonConfirm;
    private String verifyCode = "";

    private Handler handler = new Handler();
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    //    dialog
    DialogFragment dialogFragment;
    Bundle bundle;
    FragmentManager fm;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_forgot_password, container, false);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        forgotPasswordFragmentEditTextEmail = (EditText)rootView.findViewById(R.id.fragment_forgot_email);
        forgotPasswordFragmentButtonSend = (Button) rootView.findViewById(R.id.fragment_forgot_password_button_send);
        forgotPasswordFragmentLayoutEnterEmail = (LinearLayout)rootView.findViewById(R.id.fragment_forgot_password_layout_enter_email);
        forgotPasswordFragmentBackButton = (ImageView)rootView.findViewById(R.id.fragment_forgot_password_back_button);
        emailFormat = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        pd = new ProgressDialog(getActivity());

        forgotPasswordFragmentLayoutEnterCode = (LinearLayout)rootView.findViewById(R.id.fragment_forgot_password_layout_enter_code);
        forgotPasswordFragmentEditTextCode = (EditText)rootView.findViewById(R.id.fragment_forgot_verify_code);
        forgotPasswordFragmentButtonConfirm = (Button) rootView.findViewById(R.id.fragment_forgot_password_button_confirm);
        fm = getActivity().getSupportFragmentManager();
    }

    private void objectSetting() {
        forgotPasswordFragmentButtonSend.setOnClickListener(this);
        forgotPasswordFragmentButtonConfirm.setOnClickListener(this);
        forgotPasswordFragmentBackButton.setOnClickListener(this);
        pd.setMessage("Checking...");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_forgot_password_button_send:
                pd.show();
                checkEmail();
                break;

            case R.id.fragment_forgot_password_button_confirm:
                checkVerifyCode();
                break;
            case R.id.fragment_forgot_password_back_button:
                checkCurrentState();
                break;
        }
    }
    public void checkEmail(){
        String email = forgotPasswordFragmentEditTextEmail.getText().toString().trim();
        if(!email.matches(emailFormat))
        {
            forgotPasswordFragmentEditTextEmail.setError("Invalid Email");
            forgotPasswordFragmentEditTextEmail.setHintTextColor(ContextCompat.getColor(getActivity(),R.color.error_message));
            pd.dismiss();
        }
        else{
            apiDataObjectArrayList = new ArrayList<>();
            apiDataObjectArrayList.add(new ApiDataObject("email", email));
            apiDataObjectArrayList.add(new ApiDataObject("request_code", "1"));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setAsyncTaskManager();
                }
            },200);
        }
    }

    public void checkVerifyCode(){
        String verify_code = forgotPasswordFragmentEditTextCode.getText().toString().trim();
        if(verify_code.equals(getVerifyCode())){
            openResetPasswordDialog();
        }
        else{
            Toast.makeText(getActivity(), "Invalid Code", Toast.LENGTH_SHORT).show();
        }
    }
    public void checkCurrentState(){
        if(enterEmail)
            ((LoginActivity)getActivity()).setCurrentPage(0);
        else{
            forgotPasswordFragmentLayoutEnterCode.setVisibility(View.GONE);
            forgotPasswordFragmentLayoutEnterEmail.setVisibility(View.VISIBLE);
            verifyCode = "";
            enterEmail = true;
        }
    }

    public void setAsyncTaskManager(){

        asyncTaskManager = new AsyncTaskManager(
                getContext(),
                new ApiManager().forgot,
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
                        if(enterEmail){
                            forgotPasswordFragmentLayoutEnterEmail.setVisibility(View.GONE);
                            forgotPasswordFragmentLayoutEnterCode.setVisibility(View.VISIBLE);
                            enterEmail = false;

                            setVerifyCode(jsonObjectLoginResponse.getString("verify_code"));
                        }
                    }
                    else if (jsonObjectLoginResponse.getString("status").equals("2")) {
                        pd.dismiss();
                        alertMessage();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("4")){
                        Toast.makeText(getActivity(), "Something error with server! Try it later!", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                Toast.makeText(getActivity(), "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(getActivity(), "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(getActivity(), "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        if(pd.isShowing())
            pd.dismiss();
    }

    public void alertMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Bad Request");
        builder.setMessage("Your email does not exist");
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
    public void openResetPasswordDialog(){
        dialogFragment = new ResetPasswordDialog();
        dialogFragment.setTargetFragment(ForgotPasswordFragment.this, 300);
        bundle = new Bundle();
        bundle.putString("email", forgotPasswordFragmentEditTextEmail.getText().toString().trim());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getFragmentManager(), "");

    }
    public void returnToHome(){
        ((LoginActivity)getActivity()).setCurrentPage(0);
        forgotPasswordFragmentEditTextEmail.setText("");
        forgotPasswordFragmentEditTextCode.setText("");
        enterEmail = true;
    }

    public String getVerifyCode(){
        return verifyCode;
    }
    public void setVerifyCode(String verifyCode){
        this.verifyCode = verifyCode;
    }

}
