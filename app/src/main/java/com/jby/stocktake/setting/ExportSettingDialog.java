package com.jby.stocktake.setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.R;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;

public class ExportSettingDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private EditText categoryExportSettingDialogFieldName;
    private TextView categoryExportSettingDialogTitle;
    private Button categoryExportSettingDialogButtonCancel, categoryExportSettingDialogButtonEnable;
    private String fieldName;
    ExportSettingDialogCallBack exportSettingDialogCallBack;

    public ExportSettingDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_setting_export_setting_dialog, container);
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
        categoryExportSettingDialogFieldName = (EditText) rootView.findViewById(R.id.fragment_setting_export_setting_dialog_field_name);
        categoryExportSettingDialogTitle = (TextView) rootView.findViewById(R.id.fragment_setting_export_setting_dialog_title);
        categoryExportSettingDialogButtonCancel = (Button) rootView.findViewById(R.id.fragment_setting_export_setting_dialog_button_cancel);
        categoryExportSettingDialogButtonEnable = (Button) rootView.findViewById(R.id.fragment_setting_export_setting_dialog_button_ok);

    }

    public void objectSetting() {
        categoryExportSettingDialogButtonCancel.setOnClickListener(this);
        categoryExportSettingDialogButtonEnable.setOnClickListener(this);
        exportSettingDialogCallBack = (ExportSettingDialogCallBack) getActivity();

        if (getArguments() != null) {
            fieldName = getArguments().getString("field_name");
            categoryExportSettingDialogTitle.setText(fieldName);
            fieldName = "Export" + fieldName + "Value";
            categoryExportSettingDialogFieldName.append(SharedPreferenceManager.getExportDefaultValue(getActivity(), fieldName));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_setting_export_setting_dialog_button_cancel:
                dismiss();
                break;

            case R.id.fragment_setting_export_setting_dialog_button_ok:
                if (!categoryExportSettingDialogFieldName.getText().toString().equals(""))
                    checkingInput();
                else
                    alertMessage();
                break;
        }
    }

    private void checkingInput() {
        String newFieldName = categoryExportSettingDialogFieldName.getText().toString().trim();
        SharedPreferenceManager.setExportDefaultValue(getActivity(), fieldName, newFieldName);
        SharedPreferenceManager.setExportField(getActivity(), fieldName.substring(0, fieldName.length() - 5), "1");
        exportSettingDialogCallBack.exportSetting();
        dismiss();

    }

    public void alertMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Bad Request");
        builder.setMessage("Field name can't be blank!");
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

    public interface ExportSettingDialogCallBack {
        void exportSetting();
    }
}