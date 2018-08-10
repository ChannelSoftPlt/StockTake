package com.jby.stocktake.setting;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jby.stocktake.R;
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

public class DeviceNameDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private EditText deviceNameDialogDeviceName;
    private Button deviceNameDialogButtonCancel, deviceNameDialogButtonEnable;
    private View deviceNameDialogDivider;
    DeviceNameDialogCallback deviceNameDialogCallback;
    Dialog d;

    ProgressDialog pd;
    Handler handler;
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    public DeviceNameDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_setting_device_name_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Bundle mArgs = getArguments();
        if (mArgs != null) {
            String from = mArgs.getString("from");
            if(from != null){
                if(from.equals("home")){
                    preSetting();
                }
            }
        }
    }

    private void objectInitialize() {
        deviceNameDialogDeviceName = (EditText) rootView.findViewById(R.id.activity_setting_device_name_dialog_device_name);
        deviceNameDialogButtonCancel = (Button) rootView.findViewById(R.id.fragment_category_insert_dialog_button_cancel);
        deviceNameDialogButtonEnable = (Button) rootView.findViewById(R.id.fragment_category_insert_dialog_button_ok);
        deviceNameDialogDivider = (View)rootView.findViewById(R.id.fragment_category_insert_dialog_divider);
        handler = new Handler();

    }
    public void objectSetting(){
        deviceNameDialogButtonCancel.setOnClickListener(this);
        deviceNameDialogButtonEnable.setOnClickListener(this);
        deviceNameDialogCallback = (DeviceNameDialogCallback) getActivity();
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading...");

        String device = SharedPreferenceManager.getQuickScanQuantity(getActivity());
        if(!device.equals(""))
            deviceNameDialogDeviceName.setText(SharedPreferenceManager.getDeviceName(getActivity()));
        deviceNameDialogDeviceName.selectAll();
        deviceNameDialogDeviceName.requestFocus();
        showKeyBoard();
    }

    public void preSetting(){
        deviceNameDialogDivider.setVisibility(View.GONE);
        deviceNameDialogButtonCancel.setVisibility(View.GONE);
        if(d != null)
            d.setCancelable(false);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_category_insert_dialog_button_cancel:
                closeKeyBoard();
                dismiss();
                break;

            case R.id.fragment_category_insert_dialog_button_ok:
                if(!deviceNameDialogDeviceName.getText().toString().equals("")){
                    pd.show();
                    final String device = deviceNameDialogDeviceName.getText().toString();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(deviceNameDialogDivider.getVisibility() == View.GONE)
                                insertDeviceName(device);
                            else
                                updateDeviceName(device);
                        }
                    },200);

                }
                else
                    alertMessage();
                break;
        }
    }

    public void showKeyBoard(){
        final InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    public void closeKeyBoard(){
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    }

    public interface DeviceNameDialogCallback {
        void deviceNameSetting();
        void checkingSetting();
    }

    public void alertMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Bad Request");
        builder.setMessage("This field can't be blank!");
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

    public void insertDeviceName(String deviceName){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(getActivity())));
        apiDataObjectArrayList.add(new ApiDataObject("device_name", deviceName));
        apiDataObjectArrayList.add(new ApiDataObject("store", "store"));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().device,
                new ApiManager().getResultParameter(
                        "",
                        new ApiManager().setData(apiDataObjectArrayList),
                        ""
                )
        );
        doingInBackground();
    }

    public void updateDeviceName(String deviceName){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(getActivity())));
        apiDataObjectArrayList.add(new ApiDataObject("new_device_name", deviceName));
        apiDataObjectArrayList.add(new ApiDataObject("current_name", SharedPreferenceManager.getDeviceName(getActivity())));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().device,
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
                    if(jsonObjectLoginResponse.getString("status").equals("1")){
                        endSetting();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2"))
                        Toast.makeText(getActivity(), " Failed to store!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), " This device already existed!", Toast.LENGTH_SHORT).show();
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
    public void endSetting(){
        SharedPreferenceManager.setDeviceName(getActivity(), deviceNameDialogDeviceName.getText().toString().trim());
        if(deviceNameDialogDivider.getVisibility() != View.GONE){
            Toast.makeText(getActivity(), "Update Successful!", Toast.LENGTH_SHORT).show();
            deviceNameDialogCallback.deviceNameSetting();
        }
        else{
            Toast.makeText(getActivity(), "Store Successful!", Toast.LENGTH_SHORT).show();
            deviceNameDialogCallback.checkingSetting();
        }
        closeKeyBoard();
        dismiss();
    }
}