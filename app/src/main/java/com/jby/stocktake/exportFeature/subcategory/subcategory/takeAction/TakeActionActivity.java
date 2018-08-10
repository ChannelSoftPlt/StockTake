package com.jby.stocktake.exportFeature.subcategory.subcategory.takeAction;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.R;
import com.jby.stocktake.database.CustomSqliteHelper;
import com.jby.stocktake.others.SquareHeightLinearLayout;
import com.jby.stocktake.shareObject.ApiDataObject;
import com.jby.stocktake.shareObject.ApiManager;
import com.jby.stocktake.shareObject.AsyncTaskManager;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TakeActionActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener{
    private ListView subCategoryTakeActionDialogListView;
    private ArrayList<TakeActionObject> takeActionObjectArrayList;
    private TakeActionListViewAdapter takeActionListViewAdapter;
    private SquareHeightLinearLayout takeActionBackButton;
    private String barcode, categoryID, quantity, id, category_name, fileID;
    //    server setting
    CustomSqliteHelper customSqliteHelper;
//    for move item purpose
    private TextView takeActionMoveButton;
    private TextView takeActionBackPreviousButton;
    private int selectedID = -1;
    private boolean processDone = false;
    private boolean load = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_action);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        subCategoryTakeActionDialogListView = (ListView) findViewById(R.id.fragment_sub_category_take_action_dialog_list_view);
        takeActionBackButton = (SquareHeightLinearLayout)findViewById(R.id.fragment_sub_category_take_action_dialog_back_button);
        takeActionMoveButton = (TextView)findViewById(R.id.activity_take_action_move_button);
        takeActionBackPreviousButton = (TextView)findViewById(R.id.activity_take_action_back_to_previous_button);
        takeActionObjectArrayList = new ArrayList<>();

        customSqliteHelper = new CustomSqliteHelper(this);
    }
    public void objectSetting(){

        subCategoryTakeActionDialogListView.setOnItemClickListener(this);
        takeActionMoveButton.setOnClickListener(this);
        takeActionBackButton.setOnClickListener(this);
        takeActionBackPreviousButton.setOnClickListener(this);
        takeActionMoveButton.setTextColor(getResources().getColor(R.color.default_background));
        takeActionBackPreviousButton.setTextColor(getResources().getColor(R.color.default_background));

    }
    @Override
    public void onStart() {
        super.onStart();
        if(!load){
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                barcode = bundle.getString("barcode");
                categoryID = bundle.getString("category_id");
                quantity = bundle.getString("quantity");
                fileID = bundle.getString("file_id");
                getExistedRecord();
                setUpListView();
                load = true;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch ((view.getId())){
            case R.id.fragment_sub_category_take_action_dialog_back_button:
                if(processDone){
//                    SubCategoryActivity.closeExistedDialog();
                    Intent intent=new Intent();
                    setResult(2,intent);
                }
                finish();
                break;
            case R.id.activity_take_action_move_button:
                alertMessage();
                takeActionListViewAdapter.setSelectedViewPosition(-1);
                break;
            case R.id.activity_take_action_back_to_previous_button:
                if(processDone){
//                    SubCategoryActivity.closeExistedDialog();
                    Intent intent=new Intent();
                    setResult(2,intent);
                }
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()){
            case R.id.fragment_sub_category_take_action_dialog_list_view:
                if(!processDone){
                    if(selectedID == i)
                    {
                        takeActionListViewAdapter.setSelectedViewPosition(-1);
                        takeActionListViewAdapter.notifyDataSetChanged();
                        takeActionMoveButton.setTextColor(getResources().getColor(R.color.default_background));
                        takeActionMoveButton.setEnabled(false);
                        selectedID = -1;
                    }
                    else{
                        takeActionListViewAdapter.setSelectedViewPosition(i);
                        takeActionListViewAdapter.notifyDataSetChanged();
                        id = takeActionObjectArrayList.get(i).getId();
                        category_name = takeActionObjectArrayList.get(i).getCategoryName();
                        takeActionMoveButton.setTextColor(getResources().getColor(R.color.blue));
                        takeActionMoveButton.setEnabled(true);
                        selectedID = i;
                    }
                }
                break;
        }
    }

    public void alertMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to move your current quantity(" +quantity+ ") into this " + category_name + " ? \n \n *This quantity will automatically count into this record.");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        moveRecord();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean getExistedRecord(){
        boolean status = false;
        takeActionObjectArrayList = customSqliteHelper.fetchAllExistedRecordFromOther(categoryID, barcode, fileID, takeActionObjectArrayList);
        if(takeActionObjectArrayList != null)
            status = true;
        return status;
    }

    public void moveRecord(){
        int newQuantity = Integer.valueOf(quantity);
        boolean updateQuantity = customSqliteHelper.getMoveItemQuantity(id, newQuantity);
       if(updateQuantity){
           takeActionObjectArrayList.clear();
           setUpAfterUpdated();
           if(getExistedRecord())
               takeActionListViewAdapter.notifyDataSetChanged();
       }
    }

    public void setUpAfterUpdated(){
        processDone = true;
        takeActionMoveButton.setVisibility(View.GONE);
        takeActionBackPreviousButton.setVisibility(View.VISIBLE);
    }
    public void setUpListView(){
        takeActionListViewAdapter = new TakeActionListViewAdapter(this, takeActionObjectArrayList);
        subCategoryTakeActionDialogListView.setAdapter(takeActionListViewAdapter);
    }
}

