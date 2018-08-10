package com.jby.stocktake.importFeature.category;

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
import android.text.Editable;
import android.text.TextWatcher;
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
import com.jby.stocktake.importFeature.category.searchFeature.ImportCategorySearchDialog;
import com.jby.stocktake.importFeature.sub_category.subCategory.subCategory.subCategory.ImportSubCategoryActivity;
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

public class ImportCategory extends AppCompatActivity implements View.OnClickListener,
        TextWatcher, ImportCategoryInsertDialog.ImportCreateDialogCallBack, ImportCategoryListViewAdapter.ImportCategoryAdapterCallBack,
        ImportCategoryUpdateDialog.ImportUpdateDialogCallBack, AbsListView.MultiChoiceModeListener,
        AbsListView.OnScrollListener, CustomListView.OnDetectScrollListener,
        AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{

    private TextView actionBarTitle;
    private SquareHeightLinearLayout actionBarSearch, actionbarSetting, actionbarBackButton, actionBarCancel;
    private LinearLayout actionBarSearchLayout, actionBarDefaultLayout;
    private EditText actionBarSearchField;
    private View actionBarLayout;
    private ImportCategoryListViewAdapter categoryFragmentListViewAdapter;
    private ArrayList<ImportCategoryListViewObject> categoryFragmentListViewObjectArrayList;
    private CustomListView categoryFragmentListView;
    private ImageView categoryFragmentFloatingButton;
    private LinearLayout categoryFragmentResultNotFound;
    private SwipeRefreshLayout categoryFragmentSwipeRefreshLayout;
    private ProgressDialog pd;
    private String fileID;

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
    String newCategoryName;
    String subCategoryNum;
    String categoryID;
    //    load more data
    View listViewFooter;
    int page = 1;
    String path;
    boolean isScroll = false;
    boolean successToGetDataBefore = false;
    boolean finishLoadAll = false;
    boolean isLoading = true;
    Intent intent;
    //    actionbar purpose
    private InputMethodManager imm;
    //    update quantity purpose
    private int selectedPosition = 0;
    private String selectedCategoryID, selectedCategoryName;
    //    prevent reload the data
    private boolean load = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_category);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        actionBarTitle = (TextView)findViewById(R.id.actionBar_title);
        actionBarSearch = (SquareHeightLinearLayout)findViewById(R.id.actionBar_search);
        actionbarSetting = (SquareHeightLinearLayout)findViewById(R.id.actionBar_setting);
        actionbarBackButton = (SquareHeightLinearLayout)findViewById(R.id.actionBar_back_button);
        actionBarSearchLayout = (LinearLayout)findViewById(R.id.actionBar_search_layout);
        actionBarDefaultLayout = (LinearLayout)findViewById(R.id.actionBar_icon_layout);
        actionBarCancel = (SquareHeightLinearLayout) findViewById(R.id.actionBar_cancel);
        actionBarSearchField = (EditText) findViewById(R.id.action_bar_search_field);
        actionBarLayout = findViewById(R.id.activity_main_layout_action_bar);


        categoryFragmentListView = (CustomListView) findViewById(R.id.fragment_category_list_view);
        categoryFragmentFloatingButton = (ImageView) findViewById(R.id.fragment_category_floating_button);
        categoryFragmentResultNotFound = (LinearLayout)findViewById(R.id.not_found);
        categoryFragmentSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.activity_main_swipe_layout);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        categoryFragmentListViewObjectArrayList = new ArrayList<>();
        categoryFragmentListViewAdapter = new ImportCategoryListViewAdapter(this, categoryFragmentListViewObjectArrayList, this);
        listViewFooter = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.fragment_category_list_view_footer, null, false);

        fm = getSupportFragmentManager();
        pd = new ProgressDialog(this);

    }
    private void objectSetting() {
        actionBarSearch.setOnClickListener(this);
        actionbarSetting.setOnClickListener(this);
        actionbarBackButton.setOnClickListener(this);
        actionBarCancel.setOnClickListener(this);
        actionBarSearchField.addTextChangedListener(this);
        categoryFragmentSwipeRefreshLayout.setOnRefreshListener(this);

        categoryFragmentListView.setAdapter(categoryFragmentListViewAdapter);
        categoryFragmentListView.setOnScrollListener(this);
        categoryFragmentListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        categoryFragmentListView.setMultiChoiceModeListener(this);
        categoryFragmentListView.setOnDetectScrollListener(this);
        categoryFragmentListView.setOnItemClickListener(this);
        categoryFragmentFloatingButton.setOnClickListener(this);
        path = new ApiManager().importCategory + String.valueOf(page);
        pd.setMessage("Loading...");
    }
    @Override
    public void onStart() {
        super.onStart();
        if(!load) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                fileID = bundle.getString("file_id");
                String fileName = bundle.getString("file_name");
                setActionBarTitle(fileName);
                getAllCategoryItem(false);
                load = true;
            }
        }

    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.actionBar_search:
//                showSearchView(true);
                openSearchDialog();
                break;
            case R.id.actionBar_setting:
                openSetting();
                break;
            case R.id.actionBar_back_button:
                onBackPressed();
                break;
            case R.id.actionBar_cancel:
//                showSearchView(false);
                break;
            case R.id.fragment_category_floating_button:
                openInsertDialog();
                break;
        }
    }
    public void showSearchView(boolean show){
        if(show){
            new AnimationUtility().slideOut(this, actionBarDefaultLayout);
            new AnimationUtility().slideOut(this, actionBarTitle);
            new AnimationUtility().minimize(this, actionBarSearchLayout);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(imm != null)
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                    new AnimationUtility().slideOut(ImportCategory.this, categoryFragmentFloatingButton);
                }
            }, 200);
            actionBarSearchField.requestFocus();
        }
        else{
            new AnimationUtility().minimize(this, actionBarDefaultLayout);
            new AnimationUtility().minimize(this, actionBarTitle);
            new AnimationUtility().slideOut(this, actionBarSearchLayout);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(imm != null)
                        imm.hideSoftInputFromWindow(actionBarSearchField.getWindowToken(),0);
                    new AnimationUtility().fastFadeInVisible(ImportCategory.this, categoryFragmentFloatingButton);
                }
            }, 200);
            actionBarSearchField.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        if(actionBarSearchLayout.getVisibility() == View.VISIBLE)
        {
            showSearchView(false);
        }else{
            finish();
        }
    }

    public void setActionBarTitle(String file){
        actionBarTitle.setText(file);
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
        apiDataObjectArrayList.add(new ApiDataObject("file_id", fileID));
        apiDataObjectArrayList.add(new ApiDataObject("action_display","l"));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager(clear);
            }
        }, 200);
    }

    //    get Item
    public void searchCategoryItem(String keyword){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("keyword", keyword));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager(true);
            }
        }, 200);
    }
    //    create new category
    public void createNewCategoryItem(String category_name){
        pd.show();
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("file_id", fileID));
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("category_name",category_name));
        apiDataObjectArrayList.add(new ApiDataObject("action_create","action_create"));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager(true);
            }
        }, 200);
    }

    public void updateCategoryItem(String category_name, String category_id){
        pd.show();
        newCategoryName = category_name;
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("new_category_name",category_name));
        apiDataObjectArrayList.add(new ApiDataObject("category_id",category_id));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager(false);
            }
        }, 200);
    }

    public void deleteCategoryItem(String category_id){
        pd.show();
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("category_id",category_id));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager(false);
            }
        }, 200);
    }

    public void setAsyncTaskManager(boolean clearArraylist){
        if(clearArraylist){
            page = 1;
            path = new ApiManager().importCategory + String.valueOf(page);
            finishLoadAll = false;
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
                        if(clearArraylist)
                            categoryFragmentListViewObjectArrayList.clear();

                        for(int i = 0; i < array.length(); i++){
                            categoryFragmentListViewObjectArrayList.add(new ImportCategoryListViewObject(
                                    array.getJSONObject(i).getString("id"),
                                    array.getJSONObject(i).getString("category_name"),
                                    array.getJSONObject(i).getString("sub_category_num")
                            ));
                        }
//                        add page after one page is loaded
                        page++;
                        path = new ApiManager().importCategory + String.valueOf(page);
//                        check whether success to get data before or not
                        successToGetDataBefore = true;

                        categoryFragmentListView.removeFooterView(listViewFooter);
                        categoryFragmentListView.setVisibility(View.VISIBLE);
                        categoryFragmentResultNotFound.setVisibility(View.GONE);

//                        for controlling load at beginning
                        isLoading = false;

                    }
                    else if (jsonObjectLoginResponse.getString("status").equals("2")) {
                        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                    else if (jsonObjectLoginResponse.getString("status").equals("3")) {
                        if(successToGetDataBefore){
                            categoryFragmentListView.removeFooterView(listViewFooter);
                            finishLoadAll = true;
                        }
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("4")){
                        Toast.makeText(this, "Something went wrong with server!", Toast.LENGTH_SHORT).show();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("5")){
                        Toast.makeText(this, "Existed!", Toast.LENGTH_SHORT).show();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("6")){
                        categoryFragmentListViewObjectArrayList.set(getUpdatePosition(), new ImportCategoryListViewObject(
                                getCategoryID(),
                                getNewCategoryName(),
                                getSubCategoryNum()));
                        categoryFragmentListViewAdapter.notifyDataSetChanged();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("7")){
                        for(int i=categoryFragmentListView.getCount()-1; i >= 0; i--){
                            if(categoryFragmentListViewAdapter.getSelectedIds().get(i)){
                                categoryFragmentListViewObjectArrayList.remove(i);
                            }
                        }
                        getActionMode().finish();
                        categoryFragmentListViewAdapter.notifyDataSetChanged();

                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("8")){
                        forceUserLogout();
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
        if(categoryFragmentListViewObjectArrayList.size() == 0){
            categoryFragmentListView.setVisibility(View.GONE);
            categoryFragmentResultNotFound.setVisibility(View.VISIBLE);
        }
        categoryFragmentListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if(actionBarSearchField.getText().toString().trim().length() >= 1){
            searchCategoryItem(actionBarSearchField.getText().toString().trim());
        }
        else if(actionBarSearchField.getText().toString().trim().length() == 0){

            getAllCategoryItem(true);
        }
    }

    public void openInsertDialog(){
        dialogFragment = new ImportCategoryInsertDialog();
        dialogFragment.show(fm, "");
        showKeyBoard();
    }

    public void openUpdateDialog(String category_id, String category_name, int position, String subCategory_num){
//        for update local purpose
        editPosition = position;
        subCategoryNum = subCategory_num;
        categoryID = category_id;

        dialogFragment = new ImportCategoryUpdateDialog();
        bundle = new Bundle();
        bundle.putString("category_id", category_id);
        bundle.putString("category_name", category_name);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
        showKeyBoard();
    }

    public void openSearchDialog(){
        dialogFragment = new ImportCategorySearchDialog();
        bundle = new Bundle();
        bundle.putString("file_id", fileID);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
        final int checkedCount = categoryFragmentListView.getCheckedItemCount();
        // Set the  CAB title according to total checked items
        actionMode.setTitle(checkedCount + "  Selected");
        // Calls  toggleSelection method from ListViewAdapter Class
        categoryFragmentListViewAdapter.toggleSelection(position);
        checked = categoryFragmentListView.getCheckedItemPositions();
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
                final int checkedCount = categoryFragmentListViewObjectArrayList.size();

                categoryFragmentListViewAdapter.removeSelection();
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
        categoryFragmentListViewAdapter.removeSelection();
        list.clear();
    }

    public String getSelectedItemID(){
        for(int i = 0 ; i<categoryFragmentListView.getCount(); i++){
            if(checked.get(i)){
                list.add(categoryFragmentListViewObjectArrayList.get(i).getId());
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

    public void forceUserLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Denied");
        builder.setMessage("You don't currently have permission to access this system.");
        builder.setCancelable(false);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                intent = new Intent(ImportCategory.this, LoginActivity.class);
                SharedPreferenceManager.setUserID(ImportCategory.this, "default");
                startActivity(intent);
                finish();
            }
        });

        builder.setPositiveButton(
                "Log Out",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        intent = new Intent(ImportCategory.this, LoginActivity.class);
                        SharedPreferenceManager.setUserID(ImportCategory.this, "default");
                        startActivity(intent);
                        finish();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
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
                categoryFragmentListView.addFooterView(listViewFooter);
                getAllCategoryItem(false);
            }
        }

        if(!isScroll){
            if (categoryFragmentListView.getChildAt(0) != null) {
                categoryFragmentSwipeRefreshLayout.setEnabled(categoryFragmentListView.getFirstVisiblePosition() == 0 && categoryFragmentListView.getChildAt(0).getTop() == 0);
            }
        }
    }

    @Override
    public void onUpScrolling() {
        isScroll = false;
    }

    @Override
    public void onDownScrolling() {
        isScroll = true;
    }
    public int getUpdatePosition(){
        return editPosition;
    }
    public String getNewCategoryName(){
        return newCategoryName;
    }

    public String getSubCategoryNum(){
        return subCategoryNum;
    }
    public String getCategoryID(){
        return categoryID;
    }
    public ActionMode getActionMode(){
        return actionMode;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()){
            case R.id.fragment_category_list_view:
                clickEffect(view);
                selectedCategoryID = categoryFragmentListViewObjectArrayList.get(i).getId();
                selectedCategoryName = categoryFragmentListViewObjectArrayList.get(i).getCategory();
                selectedPosition = i;
                Bundle bundle = new Bundle();
                intent = new Intent(this, ImportSubCategoryActivity.class);
                bundle.putString("category_id", selectedCategoryID);
                bundle.putString("category_name", selectedCategoryName);
                bundle.putString("file_id", fileID);
                bundle.putString("fromListView", "categoryLV");
                intent.putExtras(bundle);
                startActivityForResult(intent, 2);
        }
    }

    @Override
    public void onRefresh() {
        isScroll = false;
        successToGetDataBefore = false;
        finishLoadAll = false;
        isLoading = true;
        categoryFragmentListViewObjectArrayList.clear();
        categoryFragmentListViewAdapter.notifyDataSetChanged();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                categoryFragmentSwipeRefreshLayout.setRefreshing(false);
                getAllCategoryItem(true);
            }
        },50);
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
            String newQuantity = data.getStringExtra("quantity");
            updateSelectedItemQuantity(newQuantity);
        }
        else if(resultCode == 3)
        {
            logOutSetting();
        }
    }

    public void updateSelectedItemQuantity(String newQuantity){
        categoryFragmentListViewObjectArrayList.set(selectedPosition,new ImportCategoryListViewObject(
                selectedCategoryID,
                selectedCategoryName,
                newQuantity
        ));
        categoryFragmentListViewAdapter.notifyDataSetChanged();
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
