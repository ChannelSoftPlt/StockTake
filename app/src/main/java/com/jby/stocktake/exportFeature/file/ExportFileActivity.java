package com.jby.stocktake.exportFeature.file;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.database.CustomSqliteHelper;
import com.jby.stocktake.home.HomeActivity;
import com.jby.stocktake.R;
import com.jby.stocktake.exportFeature.category.ExportCategoryActivity;
import com.jby.stocktake.login.LoginActivity;
import com.jby.stocktake.others.CustomListView;
import com.jby.stocktake.others.SquareHeightLinearLayout;
import com.jby.stocktake.setting.SettingActivity;
import com.jby.stocktake.shareObject.AnimationUtility;
import com.jby.stocktake.shareObject.ApiDataObject;
import com.jby.stocktake.shareObject.ApiManager;
import com.jby.stocktake.shareObject.AsyncTaskManager;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExportFileActivity extends AppCompatActivity implements View.OnClickListener,
       ExportFileInsertDialog.CreateDialogCallBack,ExportFileListViewAdapter.CategoryAdapterCallBack,
        ExportFileUpdateDialog.UpdateDialogCallBack,SwipeRefreshLayout.OnRefreshListener,
        AbsListView.MultiChoiceModeListener, AdapterView.OnItemClickListener,
        ExportFileMenuDialog.ExportCategoryMenuDialogCallBack, AbsListView.OnScrollListener{

    private TextView actionBarTitle;
    private SquareHeightLinearLayout actionBarSearch, actionbarSetting, actionbarBackButton, actionBarCancel;
    private View actionBarLayout;
    private ExportFileListViewAdapter exportFileListViewAdapter;
    private ArrayList<ExportFileListViewObject> exportFileListViewObjectArrayList;
    private CustomListView categoryFragmentListView;
    private ImageView categoryFragmentFloatingButton;
    private LinearLayout categoryFragmentResultNotFound;
    private ProgressDialog pd;
    private SwipeRefreshLayout categoryFragmentSwipeRefreshLayout;
    private ProgressBar exportFileActivityProgressBar;
    private TextView exportFileActivityLabelProgressBar;
    private CustomSqliteHelper customSqliteHelper;

    private Handler mHandler = new Handler();
    //    dialog
    DialogFragment dialogFragment;
    Bundle bundle;
    FragmentManager fm;
    //for delete purpose
    List<String> list = new ArrayList<String>();
    SparseBooleanArray checked;
    String selectedItem;
    ActionMode actionMode;
    //    for update purpose
    int editPosition;
    String newFileName;
    String categoryNum;
    String fileID;
    //    load more data
    Intent intent;
    //    actionbar purpose
    private InputMethodManager imm;
    //    update quantity purpose
    private int selectedPosition = 0;
    private String selectedFileID, selectedFileName;
    //    prevent reload the data
    private boolean load = false;
//    upload purpose
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private SquareHeightLinearLayout actionBarUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_file);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        actionBarTitle = (TextView)findViewById(R.id.actionBar_title);
        actionBarSearch = (SquareHeightLinearLayout)findViewById(R.id.actionBar_search);
        actionbarSetting = (SquareHeightLinearLayout)findViewById(R.id.actionBar_setting);
        actionbarBackButton = (SquareHeightLinearLayout)findViewById(R.id.actionBar_back_button);
        actionBarCancel = (SquareHeightLinearLayout) findViewById(R.id.actionBar_cancel);
        actionBarLayout = findViewById(R.id.activity_main_layout_action_bar);
        actionBarUpload = (SquareHeightLinearLayout)findViewById(R.id.actionBar_upload);


        categoryFragmentListView = (CustomListView) findViewById(R.id.fragment_category_list_view);
        categoryFragmentFloatingButton = (ImageView) findViewById(R.id.fragment_category_floating_button);
        categoryFragmentResultNotFound = (LinearLayout)findViewById(R.id.not_found);
        exportFileActivityProgressBar = (ProgressBar)findViewById(R.id.activity_export_file_progress_bar);
        exportFileActivityLabelProgressBar = (TextView)findViewById(R.id.activity_export_file_progress_bar_label);
        categoryFragmentSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.activity_main_swipe_layout);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        customSqliteHelper = new CustomSqliteHelper(this);
        fm = getSupportFragmentManager();
        pd = new ProgressDialog(this);

    }
    private void objectSetting() {
        actionbarSetting.setOnClickListener(this);
        actionbarBackButton.setOnClickListener(this);
        actionBarCancel.setOnClickListener(this);
        actionBarUpload.setOnClickListener(this);
        actionBarUpload.setVisibility(View.VISIBLE);
        actionBarSearch.setVisibility(View.GONE);
        categoryFragmentSwipeRefreshLayout.setOnRefreshListener(this);

        categoryFragmentListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        categoryFragmentListView.setMultiChoiceModeListener(this);
        categoryFragmentListView.setOnItemClickListener(this);
        categoryFragmentListView.setOnScrollListener(this);
        categoryFragmentFloatingButton.setOnClickListener(this);
        setActionBarTitle();
        pd.setMessage("Loading...");
    }
    @Override
    public void onStart() {
        super.onStart();
        if(!load){
            load = true;
            fetchAll();

        }

    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.actionBar_setting:
                openMenuDialog();
                break;
            case R.id.actionBar_back_button:
                onBackPressed();
                break;
            case R.id.fragment_category_floating_button:
                openInsertDialog();
                break;
            case R.id.actionBar_upload:
                uploadDialog();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        intent = new Intent(this, HomeActivity.class);
        bundle = new Bundle();
        bundle.putInt("status", 1);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();

    }

    public void fetchAll(){
//        initialize and assign data into array list
        exportFileListViewObjectArrayList = new ArrayList<>();
        exportFileListViewObjectArrayList = customSqliteHelper.fetchAll();

        if(exportFileListViewObjectArrayList.size() > 0){
            categoryFragmentListView.setVisibility(View.VISIBLE);
            categoryFragmentResultNotFound.setVisibility(View.GONE);
        }
        else{
//            if not found
            categoryFragmentListView.setVisibility(View.INVISIBLE);
            categoryFragmentResultNotFound.setVisibility(View.VISIBLE);
        }

        // setup listview whatever the data is found or not
        exportFileListViewAdapter = new ExportFileListViewAdapter(this, exportFileListViewObjectArrayList, this);
        categoryFragmentListView.setAdapter(exportFileListViewAdapter);

        setupProgressBar();
    }

    public void setActionBarTitle(){
        actionBarTitle.setText(R.string.activity_export_file_title);
    }

    public void hideActionbar(boolean hide){
        if(hide)
            new AnimationUtility().slideOut(this, actionBarLayout);
        else
            new AnimationUtility().minimize(this, actionBarLayout);
    }

    public void openInsertDialog(){
        dialogFragment = new ExportFileInsertDialog();
        dialogFragment.show(fm, "");
        showKeyBoard();
    }

    public void openUpdateDialog(String fileID, String fileName, int position, String category_num){
//        for update local purpose
        editPosition = position;
        this.fileID = fileID;
        categoryNum = category_num;


        dialogFragment = new ExportFileUpdateDialog();
        bundle = new Bundle();
        bundle.putString("file_id", fileID);
        bundle.putString("file_name", fileName);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
        showKeyBoard();
    }

    public void openMenuDialog(){
        dialogFragment = new ExportFileMenuDialog();
        dialogFragment.show(fm, "");
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
        final int checkedCount = categoryFragmentListView.getCheckedItemCount();
        // Set the  CAB title according to total checked items
        actionMode.setTitle(checkedCount + "  Selected");
        // Calls  toggleSelection method from ListViewAdapter Class
        exportFileListViewAdapter.toggleSelection(position);
        checked = categoryFragmentListView.getCheckedItemPositions();

//        selectedItem=String.valueOf(list);
//        selectedItem = selectedItem.replace("[","");
//        selectedItem = selectedItem.replace("]","");
    }

    @Override
    public boolean onCreateActionMode (ActionMode actionMode, Menu menu){
        hideActionbar(true);
        actionMode.getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode (ActionMode actionMode, Menu menu){
        return false;
    }

    @Override
    public boolean onActionItemClicked (ActionMode actionMode, MenuItem menuItem){
        switch (menuItem.getItemId()) {
            case R.id.selectAll:
                //
                final int checkedCount = exportFileListViewObjectArrayList.size();

                exportFileListViewAdapter.removeSelection();
                for (int i = 0; i < checkedCount; i++) {
                    categoryFragmentListView.setItemChecked(i, true);
                }
                actionMode.setTitle(checkedCount + "  Selected");
                return true;

            case R.id.delete:
                alertMessage();
                this.actionMode = actionMode;
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        hideActionbar(false);
        exportFileListViewAdapter.removeSelection();
        list.clear();
    }

    public String getSelectedItemID(){
        return selectedItem;
    }

    public void alertMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure you want to delete these item?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "I'm Sure",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteSelectedFile();
                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void alertMessageWhenReachedTheLimitation(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("You have reached your maximum number of file!");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "I Got It",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void alertMessageFileExisted(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("This file is existed!");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "I Got It",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public String getFileID(){
        return fileID;
    }
    public ActionMode getActionMode(){
        return actionMode;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()){
            case R.id.fragment_category_list_view:
                clickEffect(view);
                selectedFileID = exportFileListViewObjectArrayList.get(i).getId();
                selectedFileName = exportFileListViewObjectArrayList.get(i).getFile();
                selectedPosition = i;
                Bundle bundle = new Bundle();
                intent = new Intent(this, ExportCategoryActivity.class);
                bundle.putString("file_id", selectedFileID);
                bundle.putString("file_name", selectedFileName);
                intent.putExtras(bundle);
                startActivityForResult(intent, 2);
        }
    }

    public void showKeyBoard(){
        imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(resultCode==2)
        {
            String newQuantity = data.getStringExtra("quantity");
            updateSelectedItemQuantity(newQuantity);
        }
        else if(resultCode == 3)
        {
            logOutSetting();
        }
    }

    public void updateSelectedItemQuantity(String newQuantity){
        exportFileListViewObjectArrayList.set(selectedPosition,new ExportFileListViewObject(
                selectedFileID,
                selectedFileName,
                newQuantity
        ));
        exportFileListViewAdapter.notifyDataSetChanged();
    }

    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    @Override
    public void onRefresh() {
        exportFileListViewObjectArrayList.clear();
        exportFileListViewAdapter.notifyDataSetChanged();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                categoryFragmentSwipeRefreshLayout.setRefreshing(false);
                fetchAll();
            }
        },50);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        if (categoryFragmentListView.getChildAt(0) != null) {
            categoryFragmentSwipeRefreshLayout.setEnabled(categoryFragmentListView.getFirstVisiblePosition() == 0 && categoryFragmentListView.getChildAt(0).getTop() == 0);
        }
    }

    public void setupProgressBar(){
        int fileMax = SharedPreferenceManager.getUserPackage(this);
        int fileStorage = exportFileListViewObjectArrayList.size();
        exportFileActivityProgressBar.setMax(fileMax);

        String fileStatus = "File Storage: " + String.valueOf(exportFileListViewObjectArrayList.size()) + "/" + String.valueOf(fileMax);
        exportFileActivityLabelProgressBar.setText(fileStatus);

        if(fileMax == fileStorage){
            exportFileActivityProgressBar.setProgressDrawable(getApplicationContext().getResources().getDrawable(R.drawable.edit_progress_bar_when_full));
            exportFileActivityProgressBar.setProgress(fileStorage);
        }
        else{
            exportFileActivityProgressBar.setProgressDrawable(getApplicationContext().getResources().getDrawable(R.drawable.edit_progress_bar));
            if(fileStorage> fileMax/2)
                exportFileActivityProgressBar.setSecondaryProgress(fileStorage);
            else{
                exportFileActivityProgressBar.setProgress(fileStorage);
                exportFileActivityProgressBar.setSecondaryProgress(fileStorage);
            }
        }
    }

    public void logOutSetting(){
        SharedPreferenceManager.setUserID(this, "default");
        SharedPreferenceManager.setUserPassword(this, "default");
        SharedPreferenceManager.setDeviceToken(this, "default");
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void openSetting(){
        intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, 3);
    }

    public void createNewFile(String file_name){
        int currentSize = SharedPreferenceManager.getUserPackage(this);
        if(exportFileListViewObjectArrayList.size() < currentSize)
        {
            int saveFile = customSqliteHelper.saveFile(file_name);
            if(saveFile == 1)
                fetchAll();
            else if(saveFile ==2)
                alertMessageFileExisted();
            else
                Toast.makeText(this, "Failed to store this file", Toast.LENGTH_SHORT).show();
        }
        else
            alertMessageWhenReachedTheLimitation();


    }
    public void updateFile(String file_name, String file_id){
        boolean updateFile = customSqliteHelper.updateFile(file_name, file_id);
        if(updateFile)
            fetchAll();
        else
            Toast.makeText(this, "Failed to update this file", Toast.LENGTH_SHORT).show();
    }

    public List getDeleteItem(){
        for(int i = 0 ; i<categoryFragmentListView.getCount(); i++){
            if(checked.get(i)){
                list.add(exportFileListViewObjectArrayList.get(i).getId());
            }
        }
        return list;
    }

    public void deleteSelectedFile(){
        boolean deleteFile = customSqliteHelper.deleteFile(getDeleteItem());
        if(deleteFile)
            fetchAll();
        else
            Toast.makeText(this, "Failed to delete this file", Toast.LENGTH_SHORT).show();
        getActionMode().finish();
    }

    @Override
    protected void onDestroy() {
        customSqliteHelper.close();
        super.onDestroy();
    }

    public void uploadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure you want to upload your file? It may take some time to complete this action.");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        settingData();
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

    public void settingData(){
        pd.setMessage("Uploading file...");
        pd.show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CustomSqliteHelper customSqliteHelper = new CustomSqliteHelper(getApplicationContext());
                String categoryData = customSqliteHelper.convertCategoryIntoString();
                String subCategoryData = customSqliteHelper.convertSubCategoryIntoString();
                String fileData = customSqliteHelper.convertFileIntoString();

                Log.d("export", "category: " +categoryData);
//                Log.d("export", "subcategory:  " +subCategoryData);
//                Log.d("export", "file:  " +fileData);

                uploadData(fileData, categoryData, subCategoryData);
            }
        },200);
    }

    public void uploadData(String fileData, String categoryData, String subCategoryData){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("device_name", SharedPreferenceManager.getDeviceName(this)));
        apiDataObjectArrayList.add(new ApiDataObject("file_data", fileData));
        apiDataObjectArrayList.add(new ApiDataObject("category_data", categoryData));
        apiDataObjectArrayList.add(new ApiDataObject("sub_category_data", subCategoryData));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().upload,
                new ApiManager().getResultParameter(
                        "",
                        new ApiManager().setData(apiDataObjectArrayList),
                        ""
                )
        );
        doingInBackground();
    }

    public void doingInBackground(){

        asyncTaskManager.execute();

        if (!asyncTaskManager.isCancelled()) {
            try {
                jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);

                if (jsonObjectLoginResponse != null) {
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        pd.dismiss();
                    }
                }
                else {
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
        if(pd.isShowing())
            pd.dismiss();
    }


}