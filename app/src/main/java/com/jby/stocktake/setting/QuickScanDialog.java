package com.jby.stocktake.setting;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jby.stocktake.R;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;

public class QuickScanDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private EditText categoryInsertDialogEditTextQuantity;
    private Button categoryInsertDialogButtonCancel, categoryInsertDialogButtonEnable ;
    QuickScanDialogCallBack quickScanDialogCallBack;
    public QuickScanDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_setting_quick_scan_dialog, container);
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
        categoryInsertDialogEditTextQuantity = (EditText) rootView.findViewById(R.id.fragment_setting_quick_scan_dialog_quantity);
        categoryInsertDialogButtonCancel = (Button) rootView.findViewById(R.id.fragment_category_insert_dialog_button_cancel);
        categoryInsertDialogButtonEnable = (Button) rootView.findViewById(R.id.fragment_category_insert_dialog_button_ok);

    }
    public void objectSetting(){
        categoryInsertDialogButtonCancel.setOnClickListener(this);
        categoryInsertDialogButtonEnable.setOnClickListener(this);
        quickScanDialogCallBack = (QuickScanDialogCallBack) getActivity();
        categoryInsertDialogEditTextQuantity.append(SharedPreferenceManager.getQuickScanQuantity(getActivity()));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_category_insert_dialog_button_cancel:
                dismiss();
                break;

            case R.id.fragment_category_insert_dialog_button_ok:
                if(!categoryInsertDialogEditTextQuantity.getText().toString().equals(""))
                    checkingInput();
                else
                    alertMessage();
                break;
        }
    }

    private void checkingInput(){
        try{
            Double quantity = Double.valueOf(categoryInsertDialogEditTextQuantity.getText().toString().trim());
            SharedPreferenceManager.setQuickScanQuantity(getActivity(), String.valueOf(quantity));
            SharedPreferenceManager.setQuickScan(getActivity(), "1");
            quickScanDialogCallBack.quickScanSetting();
            dismiss();
        }catch (NumberFormatException e){
            Toast.makeText(getActivity(), "Invalid Barcode!", Toast.LENGTH_SHORT).show();
        }
    }

    public interface QuickScanDialogCallBack {
        void quickScanSetting();
    }

    public void alertMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Bad Request");
        builder.setMessage("Quantity can't be blank!");
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
}