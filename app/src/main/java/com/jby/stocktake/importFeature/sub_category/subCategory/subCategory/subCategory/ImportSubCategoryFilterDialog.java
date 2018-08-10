package com.jby.stocktake.importFeature.sub_category.subCategory.subCategory.subCategory;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.jby.stocktake.R;

public class ImportSubCategoryFilterDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private RelativeLayout importSubCategoryFilterDialogAll, importSubCategoryFilterDialogMore, importSubCategoryFilterDialogLess;
    private RelativeLayout importSubCategoryFilterDialogBalance;
    private LinearLayout importSubCategoryFilterDialogMainLayout;
    private AppCompatCheckBox importSubCategoryFilterDialogAllCheckBox,importSubCategoryFilterDialogMoreCheckBox, importSubCategoryFilterDialogLessCheckBox;
    private AppCompatCheckBox importSubCategoryFilterDialogBalanceCheckBox;

    ImportSubCategoryFilterDialogCallBack importSubCategoryFilterDialogCallBack;

    private String currentFilter;
    public ImportSubCategoryFilterDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_import_sub_catgory_filter_dialog, container);
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
        importSubCategoryFilterDialogAll = (RelativeLayout) rootView.findViewById(R.id.activity_import_sub_category_filter_dialog_all_layout);
        importSubCategoryFilterDialogMore = (RelativeLayout) rootView.findViewById(R.id.activity_import_sub_category_filter_dialog_more_layout);
        importSubCategoryFilterDialogLess = (RelativeLayout) rootView.findViewById(R.id.activity_import_sub_category_filter_dialog_less_layout);
        importSubCategoryFilterDialogBalance = (RelativeLayout) rootView.findViewById(R.id.activity_import_sub_category_filter_dialog_balance_layout);
        importSubCategoryFilterDialogMainLayout = (LinearLayout) rootView.findViewById(R.id.activity_import_sub_category_filter_dialog_main_layout);

        importSubCategoryFilterDialogAllCheckBox = (AppCompatCheckBox) rootView.findViewById(R.id.activity_import_sub_category_filter_dialog_all_checkbox);
        importSubCategoryFilterDialogMoreCheckBox = (AppCompatCheckBox) rootView.findViewById(R.id.activity_import_sub_category_filter_dialog_more_checkbox);
        importSubCategoryFilterDialogLessCheckBox = (AppCompatCheckBox) rootView.findViewById(R.id.activity_import_sub_category_filter_dialog_less_checkbox);
        importSubCategoryFilterDialogBalanceCheckBox = (AppCompatCheckBox) rootView.findViewById(R.id.activity_import_sub_category_filter_dialog_balance_checkbox);

        importSubCategoryFilterDialogCallBack = (ImportSubCategoryFilterDialogCallBack) getActivity();

    }
    public void objectSetting(){
        importSubCategoryFilterDialogAll.setOnClickListener(this);
        importSubCategoryFilterDialogMore.setOnClickListener(this);
        importSubCategoryFilterDialogLess.setOnClickListener(this);
        importSubCategoryFilterDialogBalance.setOnClickListener(this);
        importSubCategoryFilterDialogMainLayout.setOnClickListener(this);

        Bundle mArgs = getArguments();
        if (mArgs != null) {
            currentFilter = mArgs.getString("current_filter");
            assert currentFilter != null;
            checkingCurrentFiler(currentFilter);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activity_import_sub_category_filter_dialog_all_layout:
                filterSetup(true, false, false, false);
                currentFilter = "all";
                importSubCategoryFilterDialogCallBack.filter(currentFilter);
                break;
            case R.id.activity_import_sub_category_filter_dialog_more_layout:
                filterSetup(false, true, false, false);
                currentFilter = "more";
                importSubCategoryFilterDialogCallBack.filter(currentFilter);
                break;
            case R.id.activity_import_sub_category_filter_dialog_less_layout:
                filterSetup(false, false, true, false);
                currentFilter = "less";
                importSubCategoryFilterDialogCallBack.filter(currentFilter);
                break;
            case R.id.activity_import_sub_category_filter_dialog_balance_layout:
                filterSetup(false, false, false, true);
                currentFilter = "balance";
                importSubCategoryFilterDialogCallBack.filter(currentFilter);
                break;
            case R.id.activity_import_sub_category_filter_dialog_main_layout:
                break;
        }
        dismiss();
    }

    private void checkingCurrentFiler(String currentFilter) {
        switch(currentFilter){
            case "all":
                filterSetup(true, false, false, false);
                break;
            case "more":
                filterSetup(false, true, false, false);
                break;
            case "less":
                filterSetup(false, false, true, false);
                break;
            case "balance":
                filterSetup(false, false, false, true);
                break;

        }
    }


    private void filterSetup(boolean all, boolean more, boolean less, boolean balance){
        importSubCategoryFilterDialogAllCheckBox.setChecked(all);
        importSubCategoryFilterDialogMoreCheckBox.setChecked(more);
        importSubCategoryFilterDialogLessCheckBox.setChecked(less);
        importSubCategoryFilterDialogBalanceCheckBox.setChecked(balance);
    }


    public interface ImportSubCategoryFilterDialogCallBack {
        void filter(String selectedFilter);
    }
}