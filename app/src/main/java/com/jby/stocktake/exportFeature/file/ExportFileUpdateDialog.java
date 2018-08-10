package com.jby.stocktake.exportFeature.file;
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

public class ExportFileUpdateDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private EditText exportFileUpdateDialogEditTextCategory;
    private Button exportFileUpdateDialogButtonCancel, exportFileUpdateDialogButtonOK;
    private TextView exportFileUpdateDialogTitle;
    String exportFileUpdateTitle = "Update File";
    UpdateDialogCallBack updateDialogCallBack;
    String category_id, category_name, fromDialog = "0";

    public ExportFileUpdateDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_category_update_dialog, container);
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
        exportFileUpdateDialogEditTextCategory = (EditText) rootView.findViewById(R.id.fragment_category_update_dialog_edit_text_category);
        exportFileUpdateDialogButtonCancel = (Button) rootView.findViewById(R.id.fragment_category_update_dialog_button_cancel);
        exportFileUpdateDialogButtonOK = (Button) rootView.findViewById(R.id.fragment_category_update_dialog_button_ok);
        exportFileUpdateDialogTitle = (TextView)rootView.findViewById(R.id.fragment_category_update_dialog_title);

        updateDialogCallBack = (UpdateDialogCallBack) getActivity();

    }
    public void objectSetting(){
        Bundle mArgs = getArguments();
        if (mArgs != null) {
            category_id = mArgs.getString("file_id");
            category_name = mArgs.getString("file_name");
            updateDialogCallBack = (UpdateDialogCallBack) getActivity();

            exportFileUpdateDialogEditTextCategory.setText(category_name);
        }
        exportFileUpdateDialogTitle.setText(exportFileUpdateTitle);
        exportFileUpdateDialogEditTextCategory.setHint(R.string.file_update_dialog_edit_text_label);
        exportFileUpdateDialogButtonCancel.setOnClickListener(this);
        exportFileUpdateDialogButtonOK.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_category_update_dialog_button_cancel:
                closeKeyBoard();
                dismiss();
                break;

            case R.id.fragment_category_update_dialog_button_ok:
                if(!exportFileUpdateDialogEditTextCategory.getText().toString().equals("")){
                    updateDialogCallBack.updateFile(exportFileUpdateDialogEditTextCategory.getText().toString(), category_id);
                    closeKeyBoard();
                    dismiss();
                }
                else{
                    alertMessage();
                }
                break;
        }
    }
    public interface UpdateDialogCallBack {
        void updateFile(String category_name, String category_id);
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