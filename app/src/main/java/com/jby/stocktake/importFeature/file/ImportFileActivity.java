package com.jby.stocktake.importFeature.file;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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

import com.jby.stocktake.home.HomeActivity;
import com.jby.stocktake.R;
import com.jby.stocktake.importFeature.category.ImportCategory;
import com.jby.stocktake.login.LoginActivity;
import com.jby.stocktake.others.CustomListView;
import com.jby.stocktake.others.SquareHeightLinearLayout;
import com.jby.stocktake.setting.SettingActivity;
import com.jby.stocktake.shareObject.AnimationUtility;
import com.jby.stocktake.shareObject.ApiDataObject;
import com.jby.stocktake.shareObject.ApiManager;
import com.jby.stocktake.shareObject.AsyncTaskManager;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ImportFileActivity extends AppCompatActivity implements View.OnClickListener,
        ImportFileInsertDialog.CreateDialogCallBack,ImportFileListViewAdapter.CategoryAdapterCallBack,
        ImportFileUpdateDialog.UpdateDialogCallBack,SwipeRefreshLayout.OnRefreshListener,
        AbsListView.MultiChoiceModeListener, AdapterView.OnItemClickListener,
        AbsListView.OnScrollListener, ImportFileMenuDialog.ImportCategoryMenuDialogCallBack {

    private TextView actionBarTitle;
    private SquareHeightLinearLayout actionBarSearch, actionbarSetting, actionbarBackButton, actionBarCancel;
    private View actionBarLayout;
    private ImportFileListViewAdapter importFileListViewAdapter;
    private ArrayList<ImportFileListViewObject> importFileListViewObjectArrayList;
    private CustomListView importFileListView;
    private ImageView importFileActivityFloatingButton;
    private LinearLayout ImportFileActivityResultNotFound;
    private ProgressDialog pd;
    private SwipeRefreshLayout categoryFragmentSwipeRefreshLayout;
    private ProgressBar importFileActivityProgressBar;
    private TextView importFileActivityLabelProgressBar;

    private Handler mHandler = new Handler();
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_file);
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
        categoryFragmentSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.activity_main_swipe_layout);

        importFileListView = (CustomListView) findViewById(R.id.fragment_category_list_view);
        importFileActivityFloatingButton = (ImageView) findViewById(R.id.fragment_category_floating_button);
        ImportFileActivityResultNotFound = (LinearLayout)findViewById(R.id.not_found);
        importFileActivityProgressBar = (ProgressBar)findViewById(R.id.activity_import_file_progress_bar);
        importFileActivityLabelProgressBar = (TextView)findViewById(R.id.activity_import_file_progress_bar_label);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        importFileListViewObjectArrayList = new ArrayList<>();
        importFileListViewAdapter = new ImportFileListViewAdapter(this, importFileListViewObjectArrayList, this);

        fm = getSupportFragmentManager();
        pd = new ProgressDialog(this);

    }
    private void objectSetting() {
        actionbarSetting.setOnClickListener(this);
        actionbarBackButton.setOnClickListener(this);
        actionBarCancel.setOnClickListener(this);
        actionBarSearch.setVisibility(View.GONE);
        categoryFragmentSwipeRefreshLayout.setOnRefreshListener(this);

        importFileListView.setAdapter(importFileListViewAdapter);
        importFileListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        importFileListView.setMultiChoiceModeListener(this);
        importFileListView.setOnItemClickListener(this);
        importFileListView.setOnScrollListener(this);
        importFileActivityFloatingButton.setOnClickListener(this);
        setActionBarTitle();
        pd.setMessage("Loading...");
    }
    @Override
    public void onStart() {
        super.onStart();
        if(!load){
            getAllCategoryItem(false);
            load = true;
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

    public void setActionBarTitle(){
        actionBarTitle.setText(R.string.activity_export_file_title);
    }

    public void hideActionbar(boolean hide){
        if(hide)
            new AnimationUtility().slideOut(this, actionBarLayout);
        else
            new AnimationUtility().minimize(this, actionBarLayout);
    }

    //    get Item
    public void getAllCategoryItem(final boolean clear){
        pd.show();
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("action_display","l"));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager(clear);
            }
        }, 200);
    }

    //    create new category
    public void createNewCategoryItem(String file_name){
        pd.show();
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("file_name",file_name));
        apiDataObjectArrayList.add(new ApiDataObject("action_create","1"));
        apiDataObjectArrayList.add(new ApiDataObject("list_size",String.valueOf(getArrayListSize())));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager(true);
            }
        }, 200);
    }

    public void updateCategoryItem(String file_name, String file_id){
        pd.show();
        newFileName = file_name;
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("new_file_name",file_name));
        apiDataObjectArrayList.add(new ApiDataObject("file_id",file_id));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager(false);
            }
        }, 200);
    }

    public void deleteCategoryItem(String file_id){
        pd.show();
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("file_id",file_id));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager(false);
            }
        }, 200);
    }

    public void setAsyncTaskManager(boolean clearArraylist){
        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().importFile,
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
                        JSONArray array = jsonObjectLoginResponse.getJSONArray("file");
                        if(clearArraylist)
                            importFileListViewObjectArrayList.clear();

                        for(int i = 0; i < array.length(); i++){
                            importFileListViewObjectArrayList.add(new ImportFileListViewObject(
                                    array.getJSONObject(i).getString("id"),
                                    array.getJSONObject(i).getString("file_name"),
                                    array.getJSONObject(i).getString("category_num")
                            ));
                        }
                        importFileListView.setVisibility(View.VISIBLE);
                        ImportFileActivityResultNotFound.setVisibility(View.GONE);

                    }
                    else if (jsonObjectLoginResponse.getString("status").equals("2")) {
                        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("4")){
                        Toast.makeText(this, "Something went wrong with server!", Toast.LENGTH_SHORT).show();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("5")){
                        Toast.makeText(this, "Existed!", Toast.LENGTH_SHORT).show();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("6")){
                        importFileListViewObjectArrayList.set(getUpdatePosition(), new ImportFileListViewObject(
                                getFileID(),
                                getNewFileName(),
                                getCategoryNum()));
                        importFileListViewAdapter.notifyDataSetChanged();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("7")){
                        for(int i=importFileListView.getCount()-1; i >= 0; i--){
                            if(importFileListViewAdapter.getSelectedIds().get(i)){
                                importFileListViewObjectArrayList.remove(i);
                            }
                        }
                        getActionMode().finish();
                        importFileListViewAdapter.notifyDataSetChanged();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("8")){
                        alertMessageWhenReachedTheLimitation();
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
        pd.dismiss();
        if(importFileListViewObjectArrayList.size() == 0){
            importFileListView.setVisibility(View.GONE);
            ImportFileActivityResultNotFound.setVisibility(View.VISIBLE);
        }
        setupProgressBar();
        importFileListViewAdapter.notifyDataSetChanged();
    }

    public void openInsertDialog(){
        dialogFragment = new ImportFileInsertDialog();
        dialogFragment.show(fm, "");
        showKeyBoard();
    }

    public void openUpdateDialog(String fileID, String fileName, int position, String category_num){
//        for update local purpose
        editPosition = position;
        this.fileID = fileID;
        categoryNum = category_num;


        dialogFragment = new ImportFileUpdateDialog();
        bundle = new Bundle();
        bundle.putString("file_id", fileID);
        bundle.putString("file_name", fileName);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
        showKeyBoard();
    }

    public void openMenuDialog(){
        dialogFragment = new ImportFileMenuDialog();
        dialogFragment.show(fm, "");
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
        final int checkedCount = importFileListView.getCheckedItemCount();
        // Set the  CAB title according to total checked items
        actionMode.setTitle(checkedCount + "  Selected");
        // Calls  toggleSelection method from ListViewAdapter Class
        importFileListViewAdapter.toggleSelection(position);
        checked = importFileListView.getCheckedItemPositions();
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
                final int checkedCount = importFileListViewObjectArrayList.size();

                importFileListViewAdapter.removeSelection();
                for (int i = 0; i < checkedCount; i++) {
                    importFileListView.setItemChecked(i, true);
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
        importFileListViewAdapter.removeSelection();
        list.clear();
    }

    public String getSelectedItemID(){
        for(int i = 0 ; i<importFileListView.getCount(); i++){
            if(checked.get(i)){
                list.add(importFileListViewObjectArrayList.get(i).getId());
            }
        }
        selectedItem=String.valueOf(list);
        selectedItem = selectedItem.replace("[","");
        selectedItem = selectedItem.replace("]","");
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
                        deleteCategoryItem(getSelectedItemID());
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


    public int getUpdatePosition(){
        return editPosition;
    }
    public String getNewFileName(){
        return newFileName;
    }

    public String getCategoryNum(){
        return categoryNum;
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
                selectedFileID = importFileListViewObjectArrayList.get(i).getId();
                selectedFileName = importFileListViewObjectArrayList.get(i).getFile();
                selectedPosition = i;
                Bundle bundle = new Bundle();
                intent = new Intent(this, ImportCategory.class);
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
        else if(resultCode == 3){
            logOutSetting();
        }
    }

    public void updateSelectedItemQuantity(String newQuantity){
        importFileListViewObjectArrayList.set(selectedPosition,new ImportFileListViewObject(
                selectedFileID,
                selectedFileName,
                newQuantity
        ));
        importFileListViewAdapter.notifyDataSetChanged();
    }

    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }
    @Override
    public void onRefresh() {
        importFileListViewObjectArrayList.clear();
        importFileListViewAdapter.notifyDataSetChanged();
        mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        categoryFragmentSwipeRefreshLayout.setRefreshing(false);
                        getAllCategoryItem(true);
                    }
                },50);
    }
    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }
    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        if (importFileListView.getChildAt(0) != null) {
            categoryFragmentSwipeRefreshLayout.setEnabled(importFileListView.getFirstVisiblePosition() == 0 && importFileListView.getChildAt(0).getTop() == 0);
        }
    }
    public int getArrayListSize(){
        if(importFileListViewObjectArrayList.size() > 0)
            return importFileListViewObjectArrayList.size();
        else
            return  -1;
    }

    public void setupProgressBar(){
        int fileMax = SharedPreferenceManager.getUserPackage(this);
        int fileStorage = importFileListViewObjectArrayList.size();
        importFileActivityProgressBar.setMax(fileMax);

        String fileStatus = "File Storage: " + String.valueOf(importFileListViewObjectArrayList.size()) + "/" + String.valueOf(fileMax);
        importFileActivityLabelProgressBar.setText(fileStatus);

        if(fileMax == fileStorage){
            importFileActivityProgressBar.setProgressDrawable(getApplicationContext().getResources().getDrawable(R.drawable.edit_progress_bar_when_full));
            importFileActivityProgressBar.setProgress(fileStorage);
        }
        else{
            importFileActivityProgressBar.setProgressDrawable(getApplicationContext().getResources().getDrawable(R.drawable.edit_progress_bar));
            if(fileStorage> fileMax/2)
                importFileActivityProgressBar.setSecondaryProgress(fileStorage);
            else{
                importFileActivityProgressBar.setProgress(fileStorage);
                importFileActivityProgressBar.setSecondaryProgress(fileStorage);
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
}
