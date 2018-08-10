package com.jby.stocktake.login;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.home.HomeActivity;
import com.jby.stocktake.R;
import com.jby.stocktake.shareObject.AnimationUtility;
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



public class LoginFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        TextView.OnEditorActionListener{
    View rootView;
    private EditText loginFragmentEditTextEmail;
    private EditText loginFragmentEditTextPassword;
    private ImageView loginFragmentImageViewLogo;
    private TextView loginFragmentTextViewSignUp;
    private TextView loginFragmentTextViewForgotPassword;
    private Button loginFragmentButtonSignIn;
    private CheckBox loginFragmentCheckBoxPassword;
    private TextView loginFragmentTextViewPassword;

    private Handler handler = new Handler();
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    public LoginFragment() {
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
        rootView =  inflater.inflate(R.layout.fragment_login, container, false);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        loginFragmentEditTextEmail = (EditText) rootView.findViewById(R.id.fragment_login_editText_email);
        loginFragmentEditTextPassword = (EditText) rootView.findViewById(R.id.fragment_login_editText_password);

        loginFragmentImageViewLogo = (ImageView) rootView.findViewById(R.id.fragment_login_logo);
        loginFragmentButtonSignIn = (Button)rootView.findViewById(R.id.frament_login_button_sign_in);
        loginFragmentCheckBoxPassword = (CheckBox) rootView.findViewById(R.id.fragment_login_checkBox_password);

        loginFragmentTextViewSignUp = (TextView) rootView.findViewById(R.id.fragment_login_editText_signUp);
        loginFragmentTextViewForgotPassword = (TextView) rootView.findViewById(R.id.fragment_login_editText_forgotPassword);
        loginFragmentTextViewPassword = (TextView) rootView.findViewById(R.id.fragment_login_textView_password);
    }

    private void objectSetting() {
        loginFragmentTextViewSignUp.setOnClickListener(this);
        loginFragmentButtonSignIn.setOnClickListener(this);
        loginFragmentCheckBoxPassword.setOnCheckedChangeListener(this);
        loginFragmentTextViewForgotPassword.setOnClickListener(this);
        loginFragmentEditTextPassword.setOnEditorActionListener(this);

        loginFragmentTextViewPassword.setText(R.string.fragment_register_unmask_password);
        loginFragmentEditTextPassword.setTransformationMethod(new PasswordTransformationMethod());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setupLogo(true);
            }
        },200);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_login_editText_signUp:
                ((LoginActivity)getActivity()).setCurrentPage(1);
                break;
            case R.id.frament_login_button_sign_in:
                ((LoginActivity)getActivity()).readPhonePermission(1);
                break;
            case R.id.fragment_login_editText_forgotPassword:
                ((LoginActivity)getActivity()).setCurrentPage(2);
        }
    }

    public void checkingInput(){
        if(!loginFragmentEditTextEmail.getText().toString().equals("") && !loginFragmentEditTextPassword.getText().toString().equals("")){

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    login(loginFragmentEditTextEmail.getText().toString(), loginFragmentEditTextPassword.getText().toString());
                }
            }, 200);

        }else{
            ((LoginActivity)getActivity()).alertMessage("All the field above is required");
        }
    }
    public void login(String email, String password){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("email",email));
        apiDataObjectArrayList.add(new ApiDataObject("password",password));
        apiDataObjectArrayList.add(new ApiDataObject("imei_num", ((LoginActivity)getActivity()).getIMEI()));
        ;
        asyncTaskManager = new AsyncTaskManager(
                getContext(),
                new ApiManager().login,
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
                        Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                        closeKeyBoard();
                        SharedPreferenceManager.setUserID(getActivity(),jsonObjectLoginResponse.getString("user_id"));
                        SharedPreferenceManager.setUserPassword(getActivity(), loginFragmentEditTextPassword.getText().toString());
                        Intent i = new Intent(getActivity(), HomeActivity.class);
                        startActivity(i);
                        getActivity().finish();
                    }
                    else if (jsonObjectLoginResponse.getString("status").equals("2")) {
                        Toast.makeText(getActivity(), "Invalid username or password!", Toast.LENGTH_SHORT).show();

                    }
                    else if (jsonObjectLoginResponse.getString("status").equals("3")) {
                        ((LoginActivity)getActivity()).alertMessage("This device is not allow to perform this action!");
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
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.fragment_login_checkBox_password:
                if(b)
                {
                    loginFragmentTextViewPassword.setText(R.string.fragment_register_mask_password);
                    loginFragmentEditTextPassword.setTransformationMethod(null);
                }
                else
                {
                    loginFragmentTextViewPassword.setText(R.string.fragment_register_unmask_password);
                    loginFragmentEditTextPassword.setTransformationMethod(new PasswordTransformationMethod());
                }
                break;
        }
    }
    public void setupLogo(final boolean show){
        if(show){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new AnimationUtility().fadeInVisible(getActivity(), loginFragmentImageViewLogo);

                }
            },200);
        }
        else{
            loginFragmentImageViewLogo.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        switch (textView.getId()){
            case R.id.fragment_login_editText_password:
                ((LoginActivity)getActivity()).readPhonePermission(1);
                break;
        }
        return true;
    }

    public void closeKeyBoard(){
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    }
}
