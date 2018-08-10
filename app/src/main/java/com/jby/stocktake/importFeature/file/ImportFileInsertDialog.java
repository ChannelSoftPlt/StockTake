package com.jby.stocktake.importFeature.file;
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

public class ImportFileInsertDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private EditText exportFileInsertDialogEditTextCategory;
    private Button exportFileInsertDialogButtonCancel, exportFileInsertDialogButtonOK ;
    private TextView exportFileInsertDialogTitle;
    String exportFileTitle = "Create New File";
    CreateDialogCallBack createDialogCallBack;
    public ImportFileInsertDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_category_insert_dialog, container);
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
        exportFileInsertDialogEditTextCategory = (EditText) rootView.findViewById(R.id.fragment_category_insert_dialog_edit_text_category);
        exportFileInsertDialogButtonCancel = (Button) rootView.findViewById(R.id.fragment_category_insert_dialog_button_cancel);
        exportFileInsertDialogButtonOK = (Button) rootView.findViewById(R.id.fragment_category_insert_dialog_button_ok);
        exportFileInsertDialogTitle = (TextView)rootView.findViewById(R.id.fragment_category_insert_dialog_title);

        createDialogCallBack = (CreateDialogCallBack) getActivity();

    }
    public void objectSetting(){
        exportFileInsertDialogButtonCancel.setOnClickListener(this);
        exportFileInsertDialogButtonOK.setOnClickListener(this);
        exportFileInsertDialogTitle.setText(exportFileTitle);
        exportFileInsertDialogEditTextCategory.setHint(R.string.file_insert_dialog_edit_text_label);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_category_insert_dialog_button_cancel:
                closeKeyBoard();
                dismiss();
                break;

            case R.id.fragment_category_insert_dialog_button_ok:
                if(!exportFileInsertDialogEditTextCategory.getText().toString().equals("")){
                    createDialogCallBack.createNewCategoryItem(exportFileInsertDialogEditTextCategory.getText().toString());
                    closeKeyBoard();
                    dismiss();
                }
                else
                    alertMessage();
                break;
        }
    }
    public interface CreateDialogCallBack {
        void createNewCategoryItem(String category_name);
    }

    public void alertMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Bad Request");
        builder.setMessage("Category name can't be blank!");
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
}