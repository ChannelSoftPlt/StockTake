package com.jby.stocktake.importFeature.sub_category.subCategory.subCategory.subCategory;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jby.stocktake.R;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;

public class ImportSubCategoryNewRecordIsFoundDialog extends DialogFragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {
    View rootView;
    private Button subCategoryExistedDialogButtonIgnore, subCategoryExistedDialogButtonTakeAction;
    private CheckBox subCategoryDialogCheckBox;
    ImportSubCategoryNewRecordIsFoundDialogCallBack importSubCategoryNewRecordIsFoundDialogCallBack;
    private TextView SubCategoryExistedDialogText;
    private ImageView subCategoryExistedDialogCancelButton;
    String quantity, barCode
            ;

    public ImportSubCategoryNewRecordIsFoundDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_sub_category_new_record_is_found_dialog, container);
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
        subCategoryExistedDialogButtonIgnore = (Button) rootView.findViewById(R.id.fragment_category_insert_dialog_button_ignore);
        subCategoryExistedDialogButtonTakeAction = (Button) rootView.findViewById(R.id.fragment_category_insert_dialog_button_take_action);
        subCategoryDialogCheckBox = (CheckBox) rootView.findViewById(R.id.fragment_sub_category_existed_dialog_checkbox);
        SubCategoryExistedDialogText = (TextView) rootView.findViewById(R.id.fragment_sub_category_existed_dialog_text);
        subCategoryExistedDialogCancelButton = (ImageView) rootView.findViewById(R.id.fragment_sub_category_existed_dialog_cancel_button);
        importSubCategoryNewRecordIsFoundDialogCallBack = (ImportSubCategoryNewRecordIsFoundDialogCallBack) getActivity();

    }
    public void objectSetting(){
        Bundle mArgs = getArguments();
        if (mArgs != null) {
            barCode = mArgs.getString("barcode");
            quantity = mArgs.getString("quantity");
            String text = "This barcode " + barCode + " does not existed yet in this category!";
            SubCategoryExistedDialogText.setText(text);
        }
        subCategoryDialogCheckBox.setChecked(false);
        subCategoryExistedDialogButtonIgnore.setOnClickListener(this);
        subCategoryExistedDialogButtonTakeAction.setOnClickListener(this);
        subCategoryDialogCheckBox.setOnCheckedChangeListener(this);
        subCategoryExistedDialogCancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_category_insert_dialog_button_ignore:
                dismiss();
                break;

            case R.id.fragment_category_insert_dialog_button_take_action:
                importSubCategoryNewRecordIsFoundDialogCallBack.setCount();
                importSubCategoryNewRecordIsFoundDialogCallBack.insertSubCategoryItem(barCode, quantity);
                dismiss();
                break;

            case R.id.fragment_sub_category_existed_dialog_cancel_button:
                dismiss();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.fragment_sub_category_existed_dialog_checkbox:
                if(b)
                    SharedPreferenceManager.setReminder(getActivity(), "0");
                else
                    SharedPreferenceManager.setReminder(getActivity(), "1");
        }
    }

    public interface ImportSubCategoryNewRecordIsFoundDialogCallBack {
        void insertSubCategoryItem(String barcode, String quantity);
        void setCount();
    }
}