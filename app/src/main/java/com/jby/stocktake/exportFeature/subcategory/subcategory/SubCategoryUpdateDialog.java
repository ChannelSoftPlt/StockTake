package com.jby.stocktake.exportFeature.subcategory.subcategory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
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
import com.jby.stocktake.database.CustomSqliteHelper;
import com.jby.stocktake.database.FrameworkClass;
import com.jby.stocktake.database.ResultCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.jby.stocktake.database.CustomSqliteHelper.TB_SUB_CATEGORY;

public class SubCategoryUpdateDialog extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {
    View rootView;
    private Button categoryUpdateDialogButtonCancel, categoryUpdateDialogButtonUpdate;
    private EditText subCategoryUpdateDialogEditTextQuantity;
    private TextView subCategoryUpdateDialogTextViewDate, subCategoryUpdateDialogTextViewSystemQuantity;
    private TextView subCategoryUpdateDialogTextViewSellingPrice, subCategoryUpdateDialogTextViewCostPrice, subCategoryUpdateDialogTextViewDescription;
    private TextView SubCategoryUpdateDialogTextViewDescriptionContent, subCategoryUpdateDialogEditTextBarcode;
    private TextView subCategoryUpdateDialogTextViewItemCode, subCategoryUpdateDialogTextViewCategory;
    private CustomSqliteHelper customSqliteHelper;
    SubCategoryUpdateDialogCallBack subCategoryUpdateDialogCallBack;
    String currentQuantity, id;
    private SubCategoryObject object;

    public SubCategoryUpdateDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
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
        subCategoryUpdateDialogEditTextBarcode = rootView.findViewById(R.id.fragment_sub_category_update_dialog_barcode);
        subCategoryUpdateDialogEditTextQuantity = (EditText) rootView.findViewById(R.id.fragment_sub_category_update_dialog_quantity);
        subCategoryUpdateDialogTextViewDate = (TextView) rootView.findViewById(R.id.fragment_sub_category_update_dialog_date);
        subCategoryUpdateDialogTextViewSystemQuantity = (TextView) rootView.findViewById(R.id.fragment_sub_category_update_dialog_system_quantity_content);
        subCategoryUpdateDialogTextViewSellingPrice = (TextView) rootView.findViewById(R.id.fragment_sub_category_update_dialog_selling_price);
        subCategoryUpdateDialogTextViewCostPrice = (TextView) rootView.findViewById(R.id.fragment_sub_category_update_dialog_cost_price);
        SubCategoryUpdateDialogTextViewDescriptionContent = (TextView) rootView.findViewById(R.id.fragment_sub_category_update_dialog_description_content);
        subCategoryUpdateDialogTextViewCategory = rootView.findViewById(R.id.fragment_sub_category_update_dialog_category);
        subCategoryUpdateDialogTextViewItemCode = rootView.findViewById(R.id.fragment_sub_category_update_dialog_item_code);
        subCategoryUpdateDialogCallBack = (SubCategoryUpdateDialogCallBack) getActivity();
        customSqliteHelper = new CustomSqliteHelper(getActivity());
    }

    public void objectSetting() {
        Bundle mArgs = getArguments();
        if (mArgs != null) {
            id = mArgs.getString("id");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    setUpValue(id);
                }
            }).start();
        }
        categoryUpdateDialogButtonCancel.setOnClickListener(this);
        categoryUpdateDialogButtonUpdate.setOnClickListener(this);
        subCategoryUpdateDialogEditTextQuantity.setOnEditorActionListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyBoard();
                subCategoryUpdateDialogEditTextQuantity.selectAll();
            }
        }, 200);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_category_update_dialog_button_cancel:
                closeKeyBoard();
                dismiss();
                break;

            case R.id.fragment_category_update_dialog_button_update:
                updateBarcodeNQuantity();
                break;
        }
    }

    public void alertMessage() {
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

    public void closeKeyBoard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        switch (textView.getId()) {
            case R.id.fragment_sub_category_update_dialog_quantity:
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) updateBarcodeNQuantity();
                break;
        }
        return false;
    }

    public void updateBarcodeNQuantity() {
        try {
            Double quantity = Double.valueOf(subCategoryUpdateDialogEditTextQuantity.getText().toString());
            if (quantity > 0) {
                if (!String.valueOf(quantity).equals(currentQuantity)) {
                    subCategoryUpdateDialogCallBack.updateSubCategoryItem(String.valueOf(quantity), id);
                }
                closeKeyBoard();
                dismiss();
            } else
                alertMessage();
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Invalid Quantity!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpValue(String result) {
        object = customSqliteHelper.getItemDetail(id);
        subCategoryUpdateDialogEditTextBarcode.setText(object.getBarcode());
        subCategoryUpdateDialogEditTextQuantity.setText(object.getCheckQuantity());
        subCategoryUpdateDialogTextViewDate.setText(object.getDate() + " " + object.getTime());

        if (object.getSystemQuantity().equals("0"))
            subCategoryUpdateDialogTextViewSystemQuantity.setText("-");
        else
            subCategoryUpdateDialogTextViewSystemQuantity.setText(object.getSystemQuantity());

        if (object.getDescription().equals("0"))
            SubCategoryUpdateDialogTextViewDescriptionContent.setText("-");
        else
            SubCategoryUpdateDialogTextViewDescriptionContent.setText(object.getDescription());

        if (object.getSellingPrice().equals("0")) {
            subCategoryUpdateDialogTextViewSellingPrice.setText("-");
        } else
            subCategoryUpdateDialogTextViewSellingPrice.setText(object.getSellingPrice());

        if (object.getCostPrice().equals("0")) {
            subCategoryUpdateDialogTextViewCostPrice.setText("-");
        } else
            subCategoryUpdateDialogTextViewCostPrice.setText(object.getCostPrice());

        if (object.getItemCode().equals("0") && object.getItemCode().equals("")) {
            subCategoryUpdateDialogTextViewItemCode.setText("-");
        } else
            subCategoryUpdateDialogTextViewItemCode.setText(object.getItemCode());

        if (object.getCategoryName().equals("")) {
            subCategoryUpdateDialogTextViewCategory.setText("-");
        } else
            subCategoryUpdateDialogTextViewCategory.setText(object.getCategoryName());
    }

    public void showKeyBoard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        subCategoryUpdateDialogEditTextQuantity.setSelectAllOnFocus(true);
        subCategoryUpdateDialogEditTextQuantity.requestFocus();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        subCategoryUpdateDialogCallBack.requestFocus(true);
    }

    public interface SubCategoryUpdateDialogCallBack {
        void updateSubCategoryItem(String quantity, String selected_Id);

        void requestFocus(boolean focus);
    }
}
