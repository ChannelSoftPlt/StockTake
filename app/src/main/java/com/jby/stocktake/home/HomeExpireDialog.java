package com.jby.stocktake.home;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jby.stocktake.R;

public class HomeExpireDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private Button homeExpiredDialogButtonCancel, homeExpiredDialogButtonOK ;

    public HomeExpireDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_home_expired_dialog, container);
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
        homeExpiredDialogButtonCancel = (Button) rootView.findViewById(R.id.activity_home_expired_dialog_button_cancel);
        homeExpiredDialogButtonOK = (Button) rootView.findViewById(R.id.activity_home_expired_dialog_button_ok);

    }
    public void objectSetting(){
        homeExpiredDialogButtonCancel.setOnClickListener(this);
        homeExpiredDialogButtonOK.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activity_home_expired_dialog_button_cancel:
                dismiss();
                break;

            case R.id.activity_home_expired_dialog_button_ok:
                dismiss();
                break;
        }
    }

}