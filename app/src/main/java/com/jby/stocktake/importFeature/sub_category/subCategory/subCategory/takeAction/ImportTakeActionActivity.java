package com.jby.stocktake.importFeature.sub_category.subCategory.subCategory.takeAction;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.R;
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

public class ImportTakeActionActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener{
    private ListView subCategoryTakeActionDialogListView;
    private ArrayList<ImportTakeActionObject> takeActionObjectArrayList;
    private ImportTakeActionListViewAdapter takeActionListViewAdapter;
    private SquareHeightLinearLayout takeActionBackButton;
    private String barcode, categoryID, quantity, id, category_name, fileID;
    //    server setting
    private Handler mHandler = new Handler();
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    ProgressDialog pd;
//    for move item purpose
    private TextView takeActionMoveButton;
    private TextView takeActionBackPreviousButton;
    private int selectedID = -1;
    private boolean processDone = false;

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
        takeActionListViewAdapter = new ImportTakeActionListViewAdapter(this, takeActionObjectArrayList);
        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");

    }
    public void objectSetting(){
        subCategoryTakeActionDialogListView.setAdapter(takeActionListViewAdapter);
        subCategoryTakeActionDialogListView.setOnItemClickListener(this);
        takeActionMoveButton.setOnClickListener(this);
        takeActionBackButton.setOnClickListener(this);
        takeActionBackPreviousButton.setOnClickListener(this);
        takeActionMoveButton.setTextColor(getResources().getColor(R.color.default_background));
        takeActionBackPreviousButton.setTextColor(getResources().getColor(R.color.default_background));

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            barcode = bundle.getString("barcode");
            categoryID = bundle.getString("category_id");
            quantity = bundle.getString("quantity");
            fileID = bundle.getString("file_id");
            getExistedRecord();
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

    public void getExistedRecord(){
        pd.show();
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("take_action", "action"));
        apiDataObjectArrayList.add(new ApiDataObject("barcode", barcode));
        apiDataObjectArrayList.add(new ApiDataObject("category_id", categoryID));
        apiDataObjectArrayList.add(new ApiDataObject("file_id", fileID));
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager(false);
            }
        }, 200);
    }

    public void moveRecord(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("move_action", "action"));
        apiDataObjectArrayList.add(new ApiDataObject("quantity", quantity));
        apiDataObjectArrayList.add(new ApiDataObject("id", id));
        apiDataObjectArrayList.add(new ApiDataObject("category_id", categoryID));
        apiDataObjectArrayList.add(new ApiDataObject("barcode", barcode));
        apiDataObjectArrayList.add(new ApiDataObject("file_id", fileID));
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                takeActionObjectArrayList.clear();
                setAsyncTaskManager(true);
            }
        }, 200);
    }

    public void setAsyncTaskManager(boolean remove) {

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().importSubcategory,
                new ApiManager().getResultParameter(
                        "",
                        new ApiManager().setData(apiDataObjectArrayList),
                        ""
                )
        );
        asyncTaskManager.execute();

        if (!asyncTaskManager.isCancelled()) {
            try {
                jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);

                if (jsonObjectLoginResponse != null) {
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        JSONArray array = jsonObjectLoginResponse.getJSONArray("record");

                        for (int i = 0; i < array.length(); i++) {
                            takeActionObjectArrayList.add(new ImportTakeActionObject(
                                    array.getJSONObject(i).getString("id"),
                                    array.getJSONObject(i).getString("barcode"),
                                    array.getJSONObject(i).getString("quantity"),
                                    array.getJSONObject(i).getString("date_create"),
                                    array.getJSONObject(i).getString("time_create"),
                                    array.getJSONObject(i).getString("category_name"),
                                    array.getJSONObject(i).getString("category_id")
                            ));
                        }
                        takeActionListViewAdapter.notifyDataSetChanged();
//                        when remove item done
                        if(remove){
                            processDone = true;
                            takeActionMoveButton.setVisibility(View.GONE);
                            takeActionBackPreviousButton.setVisibility(View.VISIBLE);
                        }

                    }

                } else {
                    Toast.makeText(this, "Network Error!", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                Toast.makeText(this, "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(this, "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(this, "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(this, "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        pd.dismiss();
    }
}
