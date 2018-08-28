package com.jby.stocktake.importFeature.sub_category.subCategory.subCategory.subCategory;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.R;

public class ImportSubCategoryUpdateDialog extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {
    View rootView;
    private Button categoryUpdateDialogButtonCancel, categoryUpdateDialogButtonUpdate ;
    private EditText subCategoryUpdateDialogEditTextBarcode, subCategoryUpdateDialogEditTextQuantity;
    private TextView subCategoryUpdateDialogTextViewDate ,subCategoryUpdateDialogTextViewItemCode, subCategoryUpdateDialogTextViewSystemQuantity;
    private TextView subCategoryUpdateDialogTextViewSellingPrice, subCategoryUpdateDialogTextViewCostPrice, subCategoryUpdateDialogTextViewDescription;
    private TextView SubCategoryUpdateDialogTextViewDescriptionContent;
    ImportUpdateSubCategoryDialogCallBack importUpdateSubCategoryDialogCallBack;
    String currentBarCode, currentQuantity, selectedID, createdDate;
    String itemCode, systemQuantity, description, sellingPrice, costPrice, editBarcode, labelSellingPrice, labelCostPrice;
    public ImportSubCategoryUpdateDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_import_sub_category_update_dialog, container);
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
        subCategoryUpdateDialogTextViewItemCode = (TextView) rootView.findViewById(R.id.fragment_sub_category_update_dialog_item_code_content);
        subCategoryUpdateDialogTextViewSystemQuantity = (TextView) rootView.findViewById(R.id.fragment_sub_category_update_dialog_system_quantity_content);
        subCategoryUpdateDialogTextViewSellingPrice = (TextView) rootView.findViewById(R.id.fragment_sub_category_update_dialog_selling_price);
        subCategoryUpdateDialogTextViewCostPrice = (TextView) rootView.findViewById(R.id.fragment_sub_category_update_dialog_cost_price);
        SubCategoryUpdateDialogTextViewDescriptionContent = (TextView) rootView.findViewById(R.id.fragment_sub_category_update_dialog_description_content);

        importUpdateSubCategoryDialogCallBack = (ImportUpdateSubCategoryDialogCallBack) getActivity();

    }
    public void objectSetting(){
        Bundle mArgs = getArguments();
        if (mArgs != null) {
            currentBarCode = mArgs.getString("barcode");
            currentQuantity = mArgs.getString("quantity");
            selectedID = mArgs.getString("selectID");
            createdDate = mArgs.getString("date");
            itemCode = mArgs.getString("itemCode");
            systemQuantity = mArgs.getString("systemQuantity");
            description = mArgs.getString("description");
            sellingPrice = mArgs.getString("sellingPrice");
            costPrice = mArgs.getString("costPrice");
            labelSellingPrice = mArgs.getString("sellingPrice");
            labelCostPrice = mArgs.getString("costPrice");
            editBarcode = mArgs.getString("editBarcode");

            subCategoryUpdateDialogEditTextBarcode.setText(currentBarCode);
            subCategoryUpdateDialogEditTextQuantity.setText(currentQuantity);
            subCategoryUpdateDialogTextViewDate.setText(createdDate);
            if(itemCode.equals(""))
                subCategoryUpdateDialogTextViewItemCode.setText("-");
            else
                subCategoryUpdateDialogTextViewItemCode.setText(itemCode);

            if(systemQuantity.equals(""))
                subCategoryUpdateDialogTextViewSystemQuantity.setText("-");
            else
                subCategoryUpdateDialogTextViewSystemQuantity.setText(systemQuantity);

            if(description.equals(""))
                SubCategoryUpdateDialogTextViewDescriptionContent.setText("-");
            else
                SubCategoryUpdateDialogTextViewDescriptionContent.setText(description);

            if(sellingPrice.equals("")){
                labelSellingPrice = "-";
                subCategoryUpdateDialogTextViewSellingPrice.setText(labelSellingPrice);
            }
            else
                subCategoryUpdateDialogTextViewSellingPrice.setText(labelSellingPrice);

            if(costPrice.equals("")){
                labelCostPrice = "-";
                subCategoryUpdateDialogTextViewCostPrice.setText(labelCostPrice);
            }
            else
                subCategoryUpdateDialogTextViewCostPrice.setText(labelCostPrice);


            if(editBarcode.equals("able"))
                subCategoryUpdateDialogEditTextBarcode.setEnabled(true);
            else
                subCategoryUpdateDialogEditTextBarcode.setEnabled(false);


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
                if(editBarcode.equals("able"))
                    updateBarcodeNQuantity();
                else
                    updateQuantity();
                break;
        }
    }
    public interface ImportUpdateSubCategoryDialogCallBack {
        void updateSubCategoryItem(String barcode, String quantity, String selected_Id);
        void updateSubCategoryItemQuantityOnly(String quantity, String selected_Id);
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
                updateBarcodeNQuantity();
                break;
        }
        return false;
    }
    public void updateBarcodeNQuantity(){
        String barcode = subCategoryUpdateDialogEditTextBarcode.getText().toString();
        String quantity = subCategoryUpdateDialogEditTextQuantity.getText().toString();

        if(!barcode.equals("") && !quantity.equals("") && !quantity.equals("0")){
            if(!barcode.equals(currentBarCode) || !quantity.equals(currentQuantity)){

                importUpdateSubCategoryDialogCallBack.updateSubCategoryItem(barcode, quantity, selectedID);
            }
            closeKeyBoard();
            dismiss();
        }
        else
            alertMessage();
    }

    public void updateQuantity(){
        String quantity = subCategoryUpdateDialogEditTextQuantity.getText().toString();

        if(!quantity.equals("") && !quantity.equals("0")){
            if(!quantity.equals(currentQuantity)){
                importUpdateSubCategoryDialogCallBack.updateSubCategoryItemQuantityOnly(quantity, selectedID);
            }
            closeKeyBoard();
            dismiss();
        }
        else
            alertMessage();
    }
}