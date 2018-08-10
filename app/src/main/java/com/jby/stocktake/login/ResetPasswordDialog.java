package com.jby.stocktake.login;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class ResetPasswordDialog extends DialogFragment implements View.OnClickListener{
    View rootView;
    EditText resetPasswordDialogNewPassword;
    EditText resetPasswordDialogConfirmPassword;
    Button resetPasswordDialogSubmit;

    private Handler handler = new Handler();
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    ProgressDialog pd;
    String email;

    ResetPasswordDialogCallBack resetPasswordDialogCallBack;

    public ResetPasswordDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_forgot_password_reset_password_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void objectInitialize() {
        resetPasswordDialogNewPassword = (EditText)rootView.findViewById(R.id.fragment_forgot_password_reset_password_dialog_new_password);
        resetPasswordDialogConfirmPassword = (EditText)rootView.findViewById(R.id.fragment_forgot_password_reset_password_dialog_confirm_password);
        resetPasswordDialogSubmit = (Button) rootView.findViewById(R.id.fragment_forgot_password_reset_password_dialog_button_submit);
        pd = new ProgressDialog(getActivity());

        resetPasswordDialogCallBack = (ResetPasswordDialogCallBack) getTargetFragment();
    }
    public void objectSetting(){
        resetPasswordDialogSubmit.setOnClickListener(this);
        pd.setMessage("Checking...");
        Bundle mArgs = getArguments();
        if (mArgs != null) {
            setEmail(mArgs.getString("email"));
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_forgot_password_reset_password_dialog_button_submit:
                checkPassword();
                break;
        }
    }

    public void checkPassword(){
        final String newPassword = resetPasswordDialogNewPassword.getText().toString().trim();
        String confirmPassword = resetPasswordDialogConfirmPassword.getText().toString().trim();
        if(newPassword.equals(confirmPassword)){
            pd.show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetPassword(newPassword);
                }
            },200);

        }
        else{
            pd.dismiss();
            resetPasswordDialogConfirmPassword.setError("Not match with password above");
            resetPasswordDialogNewPassword.setHintTextColor(ContextCompat.getColor(getActivity(),R.color.error_message));
        }
    }

    public void resetPassword(String newPassword){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("new_password",newPassword));
        apiDataObjectArrayList.add(new ApiDataObject("reset","1"));
        apiDataObjectArrayList.add(new ApiDataObject("email",getEmail()));

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
                        resetPasswordDialogCallBack.returnToHome();
                        dismiss();
                        Toast.makeText(getActivity(), "Password Reset SuccessFully", Toast.LENGTH_SHORT).show();
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
        pd.dismiss();
    }

    public void setEmail(String email){
        this.email = email;
    }
    public String getEmail(){
        return email;
    }
    public interface ResetPasswordDialogCallBack{
        void returnToHome();
    }
}