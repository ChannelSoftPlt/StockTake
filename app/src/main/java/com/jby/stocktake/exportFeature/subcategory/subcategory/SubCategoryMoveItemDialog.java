package com.jby.stocktake.exportFeature.subcategory.subcategory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jby.stocktake.R;
import com.jby.stocktake.database.CustomSqliteHelper;
import com.jby.stocktake.database.FrameworkClass;
import com.jby.stocktake.database.ResultCallBack;
import com.jby.stocktake.shareObject.CustomToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.jby.stocktake.database.CustomSqliteHelper.TB_CATEGORY;
import static com.jby.stocktake.database.CustomSqliteHelper.TB_SUB_CATEGORY;

public class SubCategoryMoveItemDialog extends DialogFragment implements AdapterView.OnItemClickListener, ResultCallBack {
    View rootView;
    private ListView subCategoryMoveItemDialogListView;
    private FrameworkClass tbCategory, tbSubCategory;
    private String fileID, moveItemIds;
    private JSONArray jsonArray;
    private ArrayList<String> categoryList;
    private AlertDialog.Builder builder;
    private SubCategoryMoveItemDialogCallBack subCategoryMoveItemDialogCallBack;

    public SubCategoryMoveItemDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.sub_category_move_item_dialog, container);
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
        Bundle bundle = getArguments();
        if (bundle != null) {
            fileID = bundle.getString("file_id");
            moveItemIds = bundle.getString("move_item_id");
        }
        subCategoryMoveItemDialogListView = rootView.findViewById(R.id.sub_category_move_item_dialog_list_view);
        tbCategory = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_CATEGORY);
        tbSubCategory = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_SUB_CATEGORY);

        subCategoryMoveItemDialogCallBack = (SubCategoryMoveItemDialogCallBack) getActivity();
    }

    public void objectSetting() {
        subCategoryMoveItemDialogListView.setOnItemClickListener(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                tbCategory.new Read("id, category_name").where("file_id = " + fileID).perform();
            }
        }).start();
    }

    /*-----------------------------------------------------------------list view purpose----------------------------------------------------------------------*/
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        moveRequest(i, categoryList.get(i));
    }

    private void setUpListView(final String result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    jsonArray = new JSONObject(result).getJSONArray("result");
                    categoryList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        categoryList.add(jsonArray.getJSONObject(i).getString("category_name"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, categoryList);
                    subCategoryMoveItemDialogListView.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //    --------------------------------------------------------------move purpose------------------------------------------------------------------------------
    public void moveRequest(final int position, String category_name) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setMessage("Move to " + category_name + " ? *Once confirm cannot be undo! ");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Move",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int id) {
                        move(position);
                    }
                });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void move(final int position){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tbSubCategory
                            .new Update("category_id", jsonArray.getJSONObject(position).getString("id"))
                            .where("id IN (" + moveItemIds + ")", "").perform();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void createResult(String status) {

    }

    @Override
    public void readResult(String result) {
        setUpListView(result);
    }

    @Override
    public void updateResult(String status) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                subCategoryMoveItemDialogCallBack.afterMove();
                dismiss();
            }
        });
        new CustomToast(getActivity(), "Move successfully!");
    }

    @Override
    public void deleteResult(String status) {

    }

   public interface SubCategoryMoveItemDialogCallBack {
        void afterMove();
    }
}