package com.jby.stocktake.importFeature.sub_category.subCategory.subCategory.subCategory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.R;
import com.jby.stocktake.importFeature.sub_category.subCategory.subCategory.takeAction.ImportTakeActionActivity;
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

public class ImportSubCategoryActivity extends AppCompatActivity implements CustomListView.OnDetectScrollListener,
        AbsListView.OnScrollListener, View.OnClickListener, ImportSubCategoryUpdateDialog.ImportUpdateSubCategoryDialogCallBack,
        AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener,
        ImportSubCategoryExistedDialog.ImportExistedDialogCallBack, ImportSubCategoryInsertDialog.ImportCreateDialogCallBack,
        ImportSubCategoryNewRecordIsFoundDialog.ImportSubCategoryNewRecordIsFoundDialogCallBack, ImportSubCategoryFilterDialog.ImportSubCategoryFilterDialogCallBack{

    private TextView actionBarTitle;
    private SquareHeightLinearLayout actionBarSearch, actionbarSetting, actionbarBackButton, actionBarCancel, actionBarFilter;
    private LinearLayout actionBarSearchLayout, actionBarDefaultLayout;
    private EditText actionBarSearchField;
    private View actionBar;
    Intent intent;

    EditText subCategoryScanResult;
    CustomListView subCategoryListView;
    ImportSubCategoryListViewAdapter subCategoryListViewAdapter;
    ArrayList<ImportSubCategoryObject> subCategoryObjectArrayList;
    ImageView subCategoryFloatingButton;
    LinearLayout subCategoryNotFound, subCategoryLabelListView;

    //    server purpose
    private Handler mHandler = new Handler();
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    //    intent purpose
    String categoryID, categoryName, fromListView, selectedBarcode, fileID;
    Fragment fragment;
    //    actionbar purpose
    private InputMethodManager imm;
    //    paging purpose
    private String path;
    private int page;
    View listViewFooter;
    boolean isScroll = false;
    boolean successToGetDataBefore = false;
    boolean finishLoadAll = false;
    boolean isLoading = true;

    //    dialog
    DialogFragment dialogFragment;
    Bundle bundle;
    FragmentManager fm;
    ProgressDialog pd;
    boolean closeDialog = false;

    //    insert barcode
    int count = 0;
    boolean scanResultIsRunning = false;
    //    delete purpose
    SparseBooleanArray checked;
    String selectedItem;
    List<String> list = new ArrayList<String>();
    ActionMode actionMode;
    MediaPlayer mp;
    //    for update category quantity purpose
    private int initialQuantity = 0;
    private boolean firstAccess = true;
//    filter purpose
    private String filter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_sub_category);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {

        actionBarTitle = (TextView)findViewById(R.id.actionBar_title);
        actionBarSearch = (SquareHeightLinearLayout)findViewById(R.id.actionBar_search);
        actionbarSetting = (SquareHeightLinearLayout)findViewById(R.id.actionBar_setting);
        actionbarBackButton = (SquareHeightLinearLayout)findViewById(R.id.actionBar_back_button);
        actionBarFilter = (SquareHeightLinearLayout)findViewById(R.id.actionBar_filter);
        actionBar = findViewById(R.id.activity_main_layout_action_bar);
        actionBarSearchLayout = (LinearLayout)findViewById(R.id.actionBar_search_layout);
        actionBarDefaultLayout = (LinearLayout)findViewById(R.id.actionBar_icon_layout);
        actionBarCancel = (SquareHeightLinearLayout) findViewById(R.id.actionBar_cancel);
        actionBarSearchField = (EditText) findViewById(R.id.action_bar_search_field);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        subCategoryScanResult = (EditText) findViewById(R.id.scanResult);
        subCategoryListView = (CustomListView) findViewById(R.id.fragment_sub_category_list_view);
        subCategoryFloatingButton = (ImageView) findViewById(R.id.fragment_sub_category_floating_button);
        subCategoryNotFound = (LinearLayout) findViewById(R.id.not_found);
        subCategoryLabelListView = (LinearLayout) findViewById(R.id.fragment_sub_category_list_view_label);
        mp = MediaPlayer.create(this, R.raw.scanner_sound);

        subCategoryObjectArrayList = new ArrayList<>();
        subCategoryListViewAdapter = new ImportSubCategoryListViewAdapter(this, subCategoryObjectArrayList);
        fm = getSupportFragmentManager();

        listViewFooter = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.fragment_category_list_view_footer, null, false);
    }

    private void objectSetting() {
        actionbarBackButton.setOnClickListener(this);
        actionBarSearch.setOnClickListener(this);
        actionbarSetting.setOnClickListener(this);
        actionbarBackButton.setOnClickListener(this);
        actionBarFilter.setOnClickListener(this);
        actionBarCancel.setOnClickListener(this);
        actionbarBackButton.setVisibility(View.VISIBLE);
        actionBarFilter.setVisibility(View.VISIBLE);

        subCategoryListView.setAdapter(subCategoryListViewAdapter);
        subCategoryListView.setOnDetectScrollListener(this);
        subCategoryListView.setOnScrollListener(this);
        subCategoryListView.setOnItemClickListener(this);
        subCategoryListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        subCategoryListView.setMultiChoiceModeListener(this);
        subCategoryFloatingButton.setOnClickListener(this);
        subCategoryScanResult.setOnClickListener(this);

        subCategoryScanResult.addTextChangedListener(new ImportSubCategoryActivity.MyTextWatcher(subCategoryScanResult));
        actionBarSearchField.addTextChangedListener(new ImportSubCategoryActivity.MyTextWatcher(actionBarSearchField));

        pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                subCategoryScanResult.setEnabled(true);
                scanResultIsRunning = false;
            }
        });
        page = 1;
        path = new ApiManager().importSubcategory + String.valueOf(page);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            categoryID = bundle.getString("category_id");
            categoryName = bundle.getString("category_name");
            fromListView = bundle.getString("fromListView");
            selectedBarcode = bundle.getString("selected_barcode");
            fileID = bundle.getString("file_id");
            actionBarTitle.setText(categoryName);

            if(fromListView.equals("categoryLV"))
                getAllSubCategoryItem(true);
            else
                startSearchFunction(selectedBarcode);
        }

    }

    public void startSearchFunction(final String selectedBarcode){
        showSearchView(true);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                actionBarSearchField.setText(selectedBarcode);
            }
        },50);
    }

    @Override
    public void onBackPressed() {
        if(actionBarSearchLayout.getVisibility() == View.VISIBLE)
        {
            showSearchView(false);
        }else{
            if(initialQuantity != subCategoryObjectArrayList.size()){
                intent=new Intent();
                intent.putExtra("quantity", String.valueOf(subCategoryObjectArrayList.size()));
                setResult(2,intent);
            }
            finish();
        }
    }

    public void setActionBarHidden(boolean hide){
        if(hide)
            new AnimationUtility().slideOut(this, actionBar);

        else
            new AnimationUtility().minimize(this, actionBar);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.actionBar_search:
                showSearchView(true);
                break;
            case R.id.actionBar_setting:
                openSetting();
                break;
            case R.id.actionBar_back_button:
                onBackPressed();
                break;
            case R.id.actionBar_cancel:
                showSearchView(false);
                break;
            case R.id.fragment_sub_category_floating_button:
                openInsertDialog();
                break;
            case R.id.scanResult:
                final InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null)
                    imm.hideSoftInputFromWindow(subCategoryScanResult.getWindowToken(), 0);
                break;
            case R.id.actionBar_filter:
                openFilterDialog();
                break;
        }
    }

    public void showSearchView(boolean show){
        if(show){
            new AnimationUtility().slideOut(this, actionBarDefaultLayout);
            new AnimationUtility().slideOut(this, actionBarTitle);
            new AnimationUtility().slideOut(this, actionbarBackButton);
            new AnimationUtility().slideOut(this, subCategoryScanResult);
            new AnimationUtility().minimize(this, actionBarSearchLayout);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(imm != null)
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                    new AnimationUtility().slideOut(ImportSubCategoryActivity.this, subCategoryFloatingButton);

                }
            }, 200);
            actionBarSearchField.requestFocus();
        }
        else{
            new AnimationUtility().minimize(this, actionBarDefaultLayout);
            new AnimationUtility().minimize(this, actionBarTitle);
            new AnimationUtility().minimize(this, actionbarBackButton);
            new AnimationUtility().slideOut(this, actionBarSearchLayout);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(imm != null)
                        imm.hideSoftInputFromWindow(actionBarSearchField.getWindowToken(),0);
                    new AnimationUtility().fastFadeInVisible(ImportSubCategoryActivity.this, subCategoryFloatingButton);
                    new AnimationUtility().fastFadeInVisible(ImportSubCategoryActivity.this, subCategoryScanResult);
                }
            }, 200);
            actionBarSearchField.setText("");
            subCategoryScanResult.requestFocus();
        }
    }

    //    get Item
    public void getAllSubCategoryItem(boolean loading){
        if(loading)
            pd.show();
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("action_display","lol"));
        apiDataObjectArrayList.add(new ApiDataObject("category_id",categoryID));
        apiDataObjectArrayList.add(new ApiDataObject("filter",filter));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager(false, false);
                if(firstAccess)
                    initialQuantity = subCategoryObjectArrayList.size();
                firstAccess = false;

            }
        }, 100);
    }

    //    insert Item
    public void insertSubCategoryItem(String barcode, String quantity){
        pd.show();
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("category_id",(categoryID)));
        apiDataObjectArrayList.add(new ApiDataObject("file_id",fileID));
        apiDataObjectArrayList.add(new ApiDataObject("action_create","lol"));
        apiDataObjectArrayList.add(new ApiDataObject("barcode",barcode));
        apiDataObjectArrayList.add(new ApiDataObject("quantity",quantity));
        apiDataObjectArrayList.add(new ApiDataObject("reminder",SharedPreferenceManager.getReminder(this)));
        apiDataObjectArrayList.add(new ApiDataObject("count",String.valueOf(count)));
        apiDataObjectArrayList.add(new ApiDataObject("filter",filter));
        //  set page = 0
        resetPage();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishLoadAll = false;
                setAsyncTaskManager(true, false);
            }
        }, 200);
    }

    //    update Item
    public void updateSubCategoryItem(String barcode, String quantity, String selected_Id){
        pd.show();
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("id", selected_Id));
        apiDataObjectArrayList.add(new ApiDataObject("barcode", barcode));
        apiDataObjectArrayList.add(new ApiDataObject("quantity",quantity));
        apiDataObjectArrayList.add(new ApiDataObject("update_action", "update"));
        apiDataObjectArrayList.add(new ApiDataObject("count",String.valueOf(count)));
        apiDataObjectArrayList.add(new ApiDataObject("reminder", SharedPreferenceManager.getReminder(this)));
        apiDataObjectArrayList.add(new ApiDataObject("user_id",SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("category_id",categoryID));
        apiDataObjectArrayList.add(new ApiDataObject("file_id",fileID));
        apiDataObjectArrayList.add(new ApiDataObject("filter",filter));
        //  set page = 0
        resetPage();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishLoadAll = false;
                setAsyncTaskManager(true, false);
            }
        }, 200);
    }

    //    update Quantity Item
    public void updateSubCategoryItemQuantityOnly(String quantity, String selected_Id){
        pd.show();
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("id", selected_Id));
        apiDataObjectArrayList.add(new ApiDataObject("quantity",quantity));
        apiDataObjectArrayList.add(new ApiDataObject("update_quantity", "update"));
        apiDataObjectArrayList.add(new ApiDataObject("user_id",SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("category_id",categoryID));
        apiDataObjectArrayList.add(new ApiDataObject("filter",filter));
        //  set page = 0
        resetPage();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishLoadAll = false;
                setAsyncTaskManager(true, false);
            }
        }, 200);
    }

    //    delete Item
    public void deleteSubCategoryItem(){
        pd.show();
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("id", getSelectedItemID()));
        apiDataObjectArrayList.add(new ApiDataObject("delete_action", "delete"));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager(false, false);
            }
        }, 200);
    }

    //    get Item
    public void searchCategoryItem(String keyword){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("category_id", categoryID));
        apiDataObjectArrayList.add(new ApiDataObject("keyword", keyword));
        apiDataObjectArrayList.add(new ApiDataObject("filter",filter));
//        set page = 0
        resetPage();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager(false, true);
            }
        }, 50);
    }


    public void setAsyncTaskManager(boolean clearArrayList, boolean isSearching){
        if(isSearching){
            subCategoryObjectArrayList.clear();
        }

        asyncTaskManager = new AsyncTaskManager(
                this,
                path,
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
                        JSONArray array = jsonObjectLoginResponse.getJSONArray("item");
                        if(clearArrayList)
                            subCategoryObjectArrayList.clear();
                        for(int i = 0; i < array.length(); i++){
                            subCategoryObjectArrayList.add(new ImportSubCategoryObject(
                                    array.getJSONObject(i).getString("id"),
                                    array.getJSONObject(i).getString("barcode"),
                                    array.getJSONObject(i).getString("quantity"),
                                    array.getJSONObject(i).getString("date_create"),
                                    array.getJSONObject(i).getString("time_create"),
                                    array.getJSONObject(i).getString("item_code"),
                                    array.getJSONObject(i).getString("description"),
                                    array.getJSONObject(i).getString("system_quantity"),
                                    array.getJSONObject(i).getString("selling_price"),
                                    array.getJSONObject(i).getString("cost_price")
                            ));
                        }
//                        add page after one page is loaded
                        page++;
                        path = new ApiManager().importSubcategory + String.valueOf(page);
                        Log.d("CategoryFragment","Path:  " +path);
//                        check whether success to get data before or not
                        successToGetDataBefore = true;

                        subCategoryListView.removeFooterView(listViewFooter);

                        subCategoryListView.setVisibility(View.VISIBLE);
                        subCategoryLabelListView.setVisibility(View.VISIBLE);
                        subCategoryNotFound.setVisibility(View.GONE);

//                        for controlling load at beginning
                        isLoading = false;
//                        set count back to 0
                        count = 0;
                    }
                    else if (jsonObjectLoginResponse.getString("status").equals("2")) {
                        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                    else if (jsonObjectLoginResponse.getString("status").equals("3")) {
                        if(successToGetDataBefore){
                            subCategoryListView.removeFooterView(listViewFooter);
                            finishLoadAll = true;
                        }
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("4")){
                        Toast.makeText(this, "Something went wrong with server!", Toast.LENGTH_SHORT).show();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("5")){
                        openExistedDialog(jsonObjectLoginResponse.getString("barcode"), jsonObjectLoginResponse.getString("quantity"));
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("7")){
                        for(int i = subCategoryListView.getCount()-1; i >= 0; i--){
                            if(subCategoryListViewAdapter.getSelectedIds().get(i)){
                                subCategoryObjectArrayList.remove(i);
                            }
                        }
                        getActionMode().finish();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("8")){
//                        pop up a dialog when a new record is found
                        openNewRecordIsFoundDialog(jsonObjectLoginResponse.getString("barcode"), jsonObjectLoginResponse.getString("quantity"));
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
            pd.dismiss();
            if(!isSearching){
                subCategoryScanResult.postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        subCategoryScanResult.requestFocus();
                        scanResultIsRunning = false;
                    }
                },1000);
            }
            if(subCategoryObjectArrayList.size() == 0){
                subCategoryListView.setVisibility(View.GONE);
                subCategoryNotFound.setVisibility(View.VISIBLE);
                subCategoryLabelListView.setVisibility(View.GONE);
            }
            subCategoryListViewAdapter.notifyDataSetChanged();
        }

    }

    //list view scroll event
    @Override
    public void onUpScrolling() {
        isScroll = false;
    }

    @Override
    public void onDownScrolling() {
        isScroll = true;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int position = firstVisibleItem+visibleItemCount;
        if(!finishLoadAll){
            // Check if bottom has been reached
            if (position >= totalItemCount && totalItemCount > 0 && !isLoading && isScroll) {
                isLoading = true;
                subCategoryListView.addFooterView(listViewFooter);
                getAllSubCategoryItem(false);
            }
        }
    }

    //    list view onclick event
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()){
            case R.id.fragment_sub_category_list_view:
                clickEffect(view);
                openUpdateDialog(
                        subCategoryObjectArrayList.get(i).getBarcode(),
                        subCategoryObjectArrayList.get(i).getQuantity(),
                        subCategoryObjectArrayList.get(i).getId(),
                        subCategoryObjectArrayList.get(i).getDate()+ " " +subCategoryObjectArrayList.get(i).getTime(),
                        subCategoryObjectArrayList.get(i).getItem_code(),
                        subCategoryObjectArrayList.get(i).getSystem_quantity(),
                        subCategoryObjectArrayList.get(i).getDescription(),
                        subCategoryObjectArrayList.get(i).getSelling_price(),
                        subCategoryObjectArrayList.get(i).getCost_price());

                break;
        }
    }

    //    open insert dialog
    public void openInsertDialog(){
        dialogFragment = new ImportSubCategoryInsertDialog();
        if(!subCategoryScanResult.getText().toString().equals("")){
            bundle = new Bundle();
            bundle.putString("barcode", subCategoryScanResult.getText().toString());
            dialogFragment.setArguments(bundle);
        }
        dialogFragment.show(fm, "");
    }

    //    update dialog
    public void openUpdateDialog(String barcode, String quantity, String selectedID, String date, String itemCode,
                                 String systemQuantity, String description, String sellingPrice, String costPrice){
        dialogFragment = new ImportSubCategoryUpdateDialog();
        bundle = new Bundle();
        bundle.putString("barcode", barcode);
        bundle.putString("quantity", quantity);
        bundle.putString("selectID",selectedID );
        bundle.putString("date",date );
        bundle.putString("itemCode",itemCode );
        bundle.putString("systemQuantity",systemQuantity );
        bundle.putString("description",description );
        bundle.putString("sellingPrice",sellingPrice );
        bundle.putString("costPrice",costPrice );
        if(systemQuantity.equals(""))
            bundle.putString("editBarcode", "able");
        else
            bundle.putString("editBarcode", "disable");
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    //    existed dialog
    public void openExistedDialog(String barcode, String quantity){
        dialogFragment = new ImportSubCategoryExistedDialog();
        bundle = new Bundle();
        bundle.putString("barcode", barcode);
        bundle.putString("quantity", quantity);
        bundle.putString("category_id", (categoryID));
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    //    open filter dialog
    public void openFilterDialog(){
        dialogFragment = new ImportSubCategoryFilterDialog();
        bundle = new Bundle();

        bundle.putString("current_filter", filter);
        dialogFragment.setArguments(bundle);

        dialogFragment.show(fm, "");
    }

    //    new record is found dialog
    public void openNewRecordIsFoundDialog(String barcode, String quantity){
        dialogFragment = new ImportSubCategoryNewRecordIsFoundDialog();
        bundle = new Bundle();
        bundle.putString("barcode", barcode);
        bundle.putString("quantity", quantity);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    //    list view delete event
    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
        final int checkedCount = subCategoryListView.getCheckedItemCount();
        // Set the  CAB title according to total checked items
        actionMode.setTitle(checkedCount + "  Selected");
        // Calls  toggleSelection method from ListViewAdapter Class
        subCategoryListViewAdapter.toggleSelection(position);
        checked = subCategoryListView.getCheckedItemPositions();
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        subCategoryListViewAdapter.setDeleting(true);
        setActionBarHidden(true);
        actionMode.getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.selectAll:
                //
                final int checkedCount = subCategoryObjectArrayList.size();

                subCategoryListViewAdapter.removeSelection();
                for (int i = 0; i < checkedCount; i++) {
                    subCategoryListView.setItemChecked(i, true);
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
        setActionBarHidden(false);
        subCategoryListViewAdapter.removeSelection();
        subCategoryListViewAdapter.setDeleting(false);
    }

    public String getSelectedItemID(){
        for(int i = 0 ; i<subCategoryListView.getCount(); i++){
            if(checked.get(i)){
                list.add(subCategoryObjectArrayList.get(i).getId());
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
                        deleteSubCategoryItem();
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

    public ActionMode getActionMode(){
        return actionMode;
    }

    public void floatingButtonSetting(boolean hide){
        if(hide)
            new AnimationUtility().fadeOutGone(this, subCategoryFloatingButton);


        else
            new AnimationUtility().fadeInVisible(this, subCategoryFloatingButton);
    }

    public void setCount(){
        this.count = 1;
    }

    @Override
    public void takeActionDialog(String barcode, String quantity, String categoryID) {
        intent = new Intent(this, ImportTakeActionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("barcode", barcode);
        bundle.putString("quantity", quantity);
        bundle.putString("category_id", categoryID);
        bundle.putString("file_id", fileID);
        intent.putExtras(bundle);
        startActivityForResult(intent, 2);
    }

    public void closeDialog(){
        dialogFragment.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(closeDialog)
            closeDialog();
        closeDialog = false;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void closeExistedDialog(){
        dialogFragment.dismiss();
    }

    @Override
    public void filter(String filter) {
        if(!this.filter.equals(filter)){
            this.filter = filter;
            resetPage();
            subCategoryObjectArrayList.clear();
            getAllSubCategoryItem(true);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch (view.getId()){
                case R.id.scanResult:
                    if(!SharedPreferenceManager.getScanSound(ImportSubCategoryActivity.this).equals("0"))
                        mp.start();
                    break;
            }
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.scanResult:
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!subCategoryScanResult.getText().toString().equals("") && !scanResultIsRunning)
                            {
                                subCategoryScanResult.setEnabled(false);
                                if(SharedPreferenceManager.getQuickScan(ImportSubCategoryActivity.this).equals("0")){
                                    openInsertDialog();
                                }
                                else{
                                    insertSubCategoryItem(subCategoryScanResult.getText().toString(), SharedPreferenceManager.getQuickScanQuantity(ImportSubCategoryActivity.this));
                                }
                                scanResultIsRunning = true;
                                subCategoryScanResult.setText("");
                            }
                        }
                    },150);
                    break;
                case R.id.action_bar_search_field:
                    resetPage();
                    if(actionBarSearchField.getText().toString().trim().length() >= 1) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(actionBarSearchField.getText().toString().trim().length() >= 1)
                                    searchCategoryItem(actionBarSearchField.getText().toString().trim());
                            }
                        },200);
                    }
                    else if(actionBarSearchField.getText().toString().trim().length() == 0){
//                        reset all thing as default
                        successToGetDataBefore = false;
                        finishLoadAll = false;
                        subCategoryObjectArrayList.clear();
                        getAllSubCategoryItem(false);
                    }
                    break;
            }
        }
    }

    public void resetPage(){
        page = 1;
        path = new ApiManager().importSubcategory + String.valueOf(page);
    }

    public void clearScanResult(){
        scanResultIsRunning = false;
    }

    public void showKeyBoard(){
        final InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
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
            //do the things u wanted
            closeExistedDialog();
        }
        else if(resultCode == 3){
            logOutSetting();
        }
    }

    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
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
