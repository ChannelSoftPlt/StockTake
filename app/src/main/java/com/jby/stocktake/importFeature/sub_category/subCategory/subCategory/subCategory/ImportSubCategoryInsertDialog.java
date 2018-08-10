package com.jby.stocktake.importFeature.sub_category.subCategory.subCategory.subCategory;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jby.stocktake.R;

public class ImportSubCategoryInsertDialog extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {
    View rootView;
    private Button categoryInsertDialogButtonCancel, categoryInsertDialogButtonOK ;
    private EditText subCategoryInsertDialogEditTextBarcode, subCategoryInsertDialogEditTextQuantity;
    ImportCreateDialogCallBack importCreateDialogCallBack;
    public ImportSubCategoryInsertDialog() {
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

        importCreateDialogCallBack = (ImportCreateDialogCallBack) getActivity();

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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_category_insert_dialog_button_cancel:
                closeKeyBoard();
                dismiss();
                importCreateDialogCallBack.clearScanResult();
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
                insert();
                break;
        }
        return false;
    }

    public interface ImportCreateDialogCallBack {
        void insertSubCategoryItem(String barcode, String quantity);
        void clearScanResult();
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

    public void showKeyBoard(){
        final InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }
    public void insert(){
        String barcode = subCategoryInsertDialogEditTextBarcode.getText().toString();
        String quantity = subCategoryInsertDialogEditTextQuantity.getText().toString();
        if(!barcode.equals("") && !quantity.equals("") && !quantity.equals("0")){
            importCreateDialogCallBack.insertSubCategoryItem(barcode, quantity);
            closeKeyBoard();
            dismiss();
        }
        else
            alertMessage();
    }
}