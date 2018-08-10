package com.jby.stocktake.exportFeature.subcategory.subcategory;
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

public class SubCategoryUpdateDialog extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {
    View rootView;
    private Button categoryUpdateDialogButtonCancel, categoryUpdateDialogButtonUpdate ;
    private EditText subCategoryUpdateDialogEditTextBarcode, subCategoryUpdateDialogEditTextQuantity;
    private TextView subCategoryUpdateDialogTextViewDate;
    UpdateSubCategoryDialogCallBack updateSubCategoryDialogCallBack;
    String currentBarCode, currentQuantity, selectedID, createdDate;
    public SubCategoryUpdateDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_sub_category_update_dialog, container);
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
        categoryUpdateDialogButtonCancel = (Button) rootView.findViewById(R.id.fragment_category_update_dialog_button_cancel);
        categoryUpdateDialogButtonUpdate = (Button) rootView.findViewById(R.id.fragment_category_update_dialog_button_update);
        subCategoryUpdateDialogEditTextBarcode = (EditText) rootView.findViewById(R.id.fragment_sub_category_update_dialog_barcode);
        subCategoryUpdateDialogEditTextQuantity = (EditText) rootView.findViewById(R.id.fragment_sub_category_update_dialog_quantity);
        subCategoryUpdateDialogTextViewDate = (TextView) rootView.findViewById(R.id.fragment_sub_category_update_dialog_date);

        updateSubCategoryDialogCallBack = (UpdateSubCategoryDialogCallBack) getActivity();

    }
    public void objectSetting(){
        Bundle mArgs = getArguments();
        if (mArgs != null) {
            currentBarCode = mArgs.getString("barcode");
            currentQuantity = mArgs.getString("quantity");
            selectedID = mArgs.getString("selectID");
            createdDate = "Created at: " + mArgs.getString("date");
            subCategoryUpdateDialogEditTextBarcode.setText(currentBarCode);
            subCategoryUpdateDialogEditTextQuantity.setText(currentQuantity);
            subCategoryUpdateDialogTextViewDate.setText(createdDate);

        }
        categoryUpdateDialogButtonCancel.setOnClickListener(this);
        categoryUpdateDialogButtonUpdate.setOnClickListener(this);
        subCategoryUpdateDialogEditTextQuantity.setOnEditorActionListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_category_update_dialog_button_cancel:
                closeKeyBoard();
                dismiss();

                break;

            case R.id.fragment_category_update_dialog_button_update:
                update();
                break;
        }
    }
    public interface UpdateSubCategoryDialogCallBack {
        void updateSubCategoryItem(String barcode, String quantity, String selected_Id);
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
    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        switch(textView.getId()){
            case R.id.fragment_sub_category_update_dialog_quantity:
                update();
                break;
        }
        return false;
    }
    public void update(){
        String barcode = subCategoryUpdateDialogEditTextBarcode.getText().toString();
        String quantity = subCategoryUpdateDialogEditTextQuantity.getText().toString();

        if(!barcode.equals("") && !quantity.equals("") && !quantity.equals("0")){
            if(!barcode.equals(currentBarCode) || !quantity.equals(currentQuantity)){

                updateSubCategoryDialogCallBack.updateSubCategoryItem(barcode, quantity, selectedID);
            }
            closeKeyBoard();
            dismiss();
        }
        else
            alertMessage();
    }
}