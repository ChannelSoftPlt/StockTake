package com.jby.stocktake.exportFeature.subcategory.subcategory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.R;

public class SubCategoryInsertDialog extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {
    View rootView;
    private Button categoryInsertDialogButtonCancel, categoryInsertDialogButtonOK ;
    private EditText subCategoryInsertDialogEditTextBarcode, subCategoryInsertDialogEditTextQuantity;
    CreateDialogCallBack createDialogCallBack;
    public SubCategoryInsertDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_sub_category_insert_dialog, container);
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
        categoryInsertDialogButtonCancel = (Button) rootView.findViewById(R.id.fragment_category_insert_dialog_button_cancel);
        categoryInsertDialogButtonOK = (Button) rootView.findViewById(R.id.fragment_category_insert_dialog_button_ok);
        subCategoryInsertDialogEditTextBarcode = (EditText) rootView.findViewById(R.id.fragment_sub_category_insert_dialog_barcode);
        subCategoryInsertDialogEditTextQuantity = (EditText) rootView.findViewById(R.id.fragment_sub_category_insert_dialog_quantity);

        createDialogCallBack = (CreateDialogCallBack) getActivity();

    }

    public void objectSetting(){
        Bundle mArgs = getArguments();
        if (mArgs != null) {
            String barCode = mArgs.getString("barcode");
                subCategoryInsertDialogEditTextBarcode.setText(barCode);
                subCategoryInsertDialogEditTextQuantity.requestFocus();
                subCategoryInsertDialogEditTextQuantity.selectAll();
        }
//        showKeyBoard();
        categoryInsertDialogButtonCancel.setOnClickListener(this);
        categoryInsertDialogButtonOK.setOnClickListener(this);
        subCategoryInsertDialogEditTextQuantity.setOnEditorActionListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyBoard();
            }
        },400);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_category_insert_dialog_button_cancel:
                closeKeyBoard();
                dismiss();
                break;

            case R.id.fragment_category_insert_dialog_button_ok:
                insert();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        switch(textView.getId()){
            case R.id.fragment_sub_category_insert_dialog_quantity:
                if(i == EditorInfo.IME_ACTION_DONE) insert();
                break;
        }
        return false;
    }

    public interface CreateDialogCallBack {
        void insertSubCategoryItem(String barcode, String quantity);
        void requestFocus(boolean focus);
    }

    public void alertMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Bad Request");
        builder.setMessage("Every Field is required");
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

    public void closeKeyBoard(){
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    }

    public void showKeyBoard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void insert(){
        try{
            String barcode = subCategoryInsertDialogEditTextBarcode.getText().toString();
            Double quantity = Double.valueOf(subCategoryInsertDialogEditTextQuantity.getText().toString());
            if(!barcode.equals("") && quantity > 0){
                createDialogCallBack.insertSubCategoryItem(barcode, String.valueOf(quantity));
                closeKeyBoard();
                dismiss();
            }
            else
                alertMessage();
        }catch (NumberFormatException e){
            Toast.makeText(getActivity(), "Invalid Quantity!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        createDialogCallBack.requestFocus(true);
    }
}