package com.jby.stocktake.exportFeature.file;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class ExportFileMenuDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private TextView menuDialogExportButton, menuDialogSettingButton;
    private LinearLayout menuDialogExportParentLayout;
    ExportCategoryMenuDialogCallBack exportCategoryMenuDialogCallBack;
    private ProgressDialog pd;
    Handler handler;
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    public ExportFileMenuDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_export_category_menu_dialog, container);
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
        menuDialogExportButton = (TextView) rootView.findViewById(R.id.menu_export_button);
        menuDialogSettingButton = (TextView) rootView.findViewById(R.id.menu_setting_button);
        menuDialogExportParentLayout = (LinearLayout) rootView.findViewById(R.id.activity_export_category_menu_dialog_parent_layout);
        exportCategoryMenuDialogCallBack = (ExportCategoryMenuDialogCallBack) getActivity();
        pd = new ProgressDialog(getActivity());
        handler = new Handler();

    }
    public void objectSetting(){
        menuDialogExportButton.setOnClickListener(this);
        menuDialogSettingButton.setOnClickListener(this);
        menuDialogExportParentLayout.setOnClickListener(this);
        pd.setMessage("Loading...");
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.menu_export_button:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pd.show();
                        checkUserStatus();
                    }
                },50);
                break;
            case R.id.menu_setting_button:
                exportCategoryMenuDialogCallBack.openSetting();
                dismiss();
                break;
            case R.id.activity_export_category_menu_dialog_parent_layout:
                dismiss();
                break;
        }
    }

    public interface ExportCategoryMenuDialogCallBack{
        void openSetting();
    }

    public void checkUserStatus(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(getActivity())));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
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
                        pd.dismiss();
                        String userStatus = jsonObjectLoginResponse.getString("user_status");
                        if(userStatus.equals("1")){
                            exportFile();
                        }
                        else
                            failedToExport();
                    }
                    else if (jsonObjectLoginResponse.getString("status").equals("2")) {
                        Toast.makeText(getActivity(), "Something error with server! Try it later!", Toast.LENGTH_SHORT).show();
                    }
                    else if (jsonObjectLoginResponse.getString("status").equals("3")) {
                        failedToExport();
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
    public void failedToExport() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setMessage("Sorry, currently you are not allowed to perform action!");
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

    public void exportFile(){
        String url = "http://188.166.186.198/~cheewee/stocktake/backend/client/login.php?";
        String userID = "user_id=" + SharedPreferenceManager.getUserID(getActivity());
        String userPassword = "&password=" + SharedPreferenceManager.getUserPassword(getActivity());
        String action = "&action=2";
        url = url + userID + userPassword + action;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
        dismiss();
    }
}