package com.jby.stocktake.exportFeature.subcategory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
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
import com.jby.stocktake.database.CustomSqliteHelper;
import com.jby.stocktake.login.LoginActivity;
import com.jby.stocktake.others.CustomListView;
import com.jby.stocktake.others.SquareHeightLinearLayout;
import com.jby.stocktake.setting.SettingActivity;
import com.jby.stocktake.shareObject.AnimationUtility;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryExistedDialog;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryInsertDialog;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryListViewAdapter;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryObject;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryUpdateDialog;
import com.jby.stocktake.exportFeature.subcategory.subcategory.takeAction.TakeActionActivity;
import java.util.ArrayList;
import java.util.List;

public class SubCategoryActivity extends AppCompatActivity implements CustomListView.OnDetectScrollListener,
        AbsListView.OnScrollListener, View.OnClickListener, SubCategoryUpdateDialog.UpdateSubCategoryDialogCallBack,
        AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener,
        SubCategoryExistedDialog.ExistedDialogCallBack, SubCategoryInsertDialog.CreateDialogCallBack {

    private TextView actionBarTitle;
    private SquareHeightLinearLayout actionBarSearch, actionbarSetting, actionbarBackButton, actionBarCancel;
    private LinearLayout actionBarSearchLayout, actionBarDefaultLayout;
    private EditText actionBarSearchField;
    private View actionBar;
    Intent intent;

    EditText subCategoryScanResult;
    CustomListView subCategoryListView;
    SubCategoryListViewAdapter subCategoryListViewAdapter;
    ArrayList<SubCategoryObject> subCategoryObjectArrayList;
    ImageView subCategoryFloatingButton;
    LinearLayout subCategoryNotFound, subCategoryLabelListView;

    private Handler mHandler = new Handler();
    //    SQLite purpose
    CustomSqliteHelper customSqliteHelper;
//    intent purpose
    String categoryID, categoryName, selectedBarcode, fromListView, fileID;
    Fragment fragment;
    //    actionbar purpose
    private InputMethodManager imm;
    //    paging purpose
    private int page = 1;
    boolean isScroll = false;
    boolean successToGetDataBefore = false;
    boolean finishLoadAll = false;
    boolean isLoading = true;

    //    dialog
    DialogFragment dialogFragment;
    Bundle bundle;
    FragmentManager fm;
    boolean closeDialog = false;

    //    insert barcode
    int count = 0;
    //    delete purpose
    SparseBooleanArray checked;
    List<String> list = new ArrayList<String>();
    ActionMode actionMode;
    MediaPlayer mp;
//    for update category quantity purpose
    private int initialQuantity = 0;
    private boolean firstAccess = true;
    //    prevent reload the data
    private boolean load = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {

        actionBarTitle = (TextView)findViewById(R.id.actionBar_title);
        actionBarSearch = (SquareHeightLinearLayout)findViewById(R.id.actionBar_search);
        actionbarSetting = (SquareHeightLinearLayout)findViewById(R.id.actionBar_setting);
        actionbarBackButton = (SquareHeightLinearLayout)findViewById(R.id.actionBar_back_button);
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

        fm = getSupportFragmentManager();

        customSqliteHelper = new CustomSqliteHelper(this);

    }

    private void objectSetting() {
        actionbarBackButton.setOnClickListener(this);
        actionBarSearch.setOnClickListener(this);
        actionbarSetting.setOnClickListener(this);
        actionbarBackButton.setOnClickListener(this);
        actionBarCancel.setOnClickListener(this);
        actionbarBackButton.setVisibility(View.VISIBLE);

        subCategoryListView.setOnDetectScrollListener(this);
        subCategoryListView.setOnScrollListener(this);
        subCategoryListView.setOnItemClickListener(this);
        subCategoryListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        subCategoryListView.setMultiChoiceModeListener(this);
        subCategoryFloatingButton.setOnClickListener(this);
        subCategoryScanResult.setOnClickListener(this);

        subCategoryScanResult.addTextChangedListener(new MyTextWatcher(subCategoryScanResult));
        actionBarSearchField.addTextChangedListener(new MyTextWatcher(actionBarSearchField));
    }
    @Override
    public void onStart() {
        super.onStart();
        if(!load){
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                categoryID = bundle.getString("category_id");
                categoryName = bundle.getString("category_name");
                fromListView = bundle.getString("fromListView");
                selectedBarcode = bundle.getString("selected_barcode");
                fileID = bundle.getString("file_id");
                actionBarTitle.setText(categoryName);
                setUpListView();

                if(fromListView.equals("categoryLV"))
                    fetchAllSubCategoryData();

                else
                    startSearchFunction(selectedBarcode);
                load = true;
                subCategoryScanResult.requestFocus();
            }
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
                    new AnimationUtility().slideOut(SubCategoryActivity.this, subCategoryFloatingButton);

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
                    new AnimationUtility().fastFadeInVisible(SubCategoryActivity.this, subCategoryFloatingButton);
                    new AnimationUtility().fastFadeInVisible(SubCategoryActivity.this, subCategoryScanResult);
                }
            }, 200);
            actionBarSearchField.setText("");
            subCategoryScanResult.requestFocus();
        }
    }

    //list view scroll event
    @Override
    public void onUpScrolling() {
        showFloatingButton();
        isScroll = false;
    }

    @Override
    public void onDownScrolling() {
        hideFloatingButton();
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
                page++;
                fetchAllSubCategoryData();
                subCategoryListViewAdapter.notifyDataSetChanged();
            }
        }
    }

    public void hideFloatingButton(){
        if(!isScroll)
        {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new AnimationUtility().slideOut(getApplicationContext(), subCategoryFloatingButton);
                }
            },100);
        }

    }

    public void showFloatingButton(){
        if(isScroll){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    subCategoryFloatingButton.setVisibility(View.VISIBLE);
                }
            },100);
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
                        subCategoryObjectArrayList.get(i).getDate()+ " " +subCategoryObjectArrayList.get(i).getTime());

                break;
        }
    }

    @Override
    protected void onDestroy() {
        customSqliteHelper.close();
        super.onDestroy();
    }

    //    open insert dialog
    public void openInsertDialog(){
        dialogFragment = new SubCategoryInsertDialog();
        if(!subCategoryScanResult.getText().toString().equals("")){
            bundle = new Bundle();
            bundle.putString("barcode", subCategoryScanResult.getText().toString());
            dialogFragment.setArguments(bundle);
        }
        dialogFragment.show(fm, "");
        showKeyBoard();
    }

    //    update dialog
    public void openUpdateDialog(String barcode, String quantity, String selectedID, String date){
        dialogFragment = new SubCategoryUpdateDialog();
        bundle = new Bundle();
        bundle.putString("barcode", barcode);
        bundle.putString("quantity", quantity);
        bundle.putString("selectID",selectedID );
        bundle.putString("date",date );
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
        showKeyBoard();
    }

    //    existed dialog
    public void openExistedDialog(String barcode, String quantity){
        dialogFragment = new SubCategoryExistedDialog();
        bundle = new Bundle();
        bundle.putString("barcode", barcode);
        bundle.putString("quantity", quantity);
        bundle.putString("category_id", (categoryID));
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
        list.clear();
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

    public void setCount(){
        this.count = 1;
    }

    @Override
    public void takeActionDialog(String barcode, String quantity, String categoryID) {
        intent = new Intent(this, TakeActionActivity.class);
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
                    if(!SharedPreferenceManager.getScanSound(SubCategoryActivity.this).equals("0"))
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
                            if(!subCategoryScanResult.getText().toString().equals(""))
                            {
                                subCategoryScanResult.setEnabled(false);
                                if(SharedPreferenceManager.getQuickScan(SubCategoryActivity.this).equals("0")){
                                    openInsertDialog();
                                }
                                else{
                                    insertSubCategoryItem(subCategoryScanResult.getText().toString(), SharedPreferenceManager.getQuickScanQuantity(SubCategoryActivity.this));
                                }
                                subCategoryScanResult.setText("");
                            }
                        }
                    },150);
                    break;
                case R.id.action_bar_search_field:
                    page = 1;
                    if(actionBarSearchField.getText().toString().trim().length() >= 1){
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(actionBarSearchField.getText().toString().trim().length() >= 1)
                                    searchSubCategoryItem(actionBarSearchField.getText().toString().trim());
                            }
                        },200);
                    }
                    else if(actionBarSearchField.getText().toString().trim().length() == 0){
//                        reset all thing as default
                        successToGetDataBefore = false;
                        finishLoadAll = false;
                        subCategoryObjectArrayList.clear();
                        fetchAllSubCategoryData();
                    }
                    break;
            }
        }
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
        else if(resultCode == 3)
        {
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
    public void setUpListView(){
        subCategoryListViewAdapter = new SubCategoryListViewAdapter(this, subCategoryObjectArrayList, categoryID);
        subCategoryListView.setAdapter(subCategoryListViewAdapter);
    }
    public void fetchAllSubCategoryData(){
        isLoading = false;
        subCategoryObjectArrayList = customSqliteHelper.fetchAllSubCategory(categoryID, page, subCategoryObjectArrayList);
        setListViewVisibility();
    }

    public void setListViewVisibility(){
        if(subCategoryObjectArrayList.size() > 0){
//            if data found
            subCategoryListView.setVisibility(View.VISIBLE);
            subCategoryNotFound.setVisibility(View.GONE);
        }
        else{
//            if not found
            subCategoryListView.setVisibility(View.INVISIBLE);
            subCategoryNotFound.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateSubCategoryItem(String barcode, String quantity, String selectedID) {
        int updateQuantity = Integer.valueOf(quantity);
        int updateSubCategory = customSqliteHelper.updateSubCategory(fileID, count, categoryID, barcode, updateQuantity, selectedID);
        if(updateSubCategory == 1){
            subCategoryObjectArrayList.clear();
            fetchAllSubCategoryData();
            subCategoryListViewAdapter.notifyDataSetChanged();
        }
        else if(updateSubCategory == 2)
            Toast.makeText(this, "Failed to update this record", Toast.LENGTH_SHORT).show();
        else
            openExistedDialog(barcode, quantity);

        subCategoryScanResult.requestFocus();
    }

    @Override
    public void insertSubCategoryItem(String barcode, String quantity) {
        int scanQuantity = Integer.valueOf(quantity);
        int insertSubCategory = customSqliteHelper.saveSubCategory(fileID, count, categoryID, barcode, scanQuantity);
        if(insertSubCategory == 1){
            subCategoryObjectArrayList.clear();
            fetchAllSubCategoryData();
            subCategoryListViewAdapter.notifyDataSetChanged();
        }

        else if(insertSubCategory == 2)
            Toast.makeText(this, "Failed to store this record", Toast.LENGTH_SHORT).show();
        else
            openExistedDialog(barcode, quantity);

        subCategoryScanResult.setEnabled(true);
        subCategoryScanResult.requestFocus();
    }

    public List getDeleteItem(){
        for(int i = 0 ; i<subCategoryListView.getCount(); i++){
            if(checked.get(i)){
                list.add(subCategoryObjectArrayList.get(i).getId());
            }
        }
        return list;
    }

    public void deleteSubCategoryItem(){
        boolean deleteSubCategory = customSqliteHelper.deleteSubCategory(getDeleteItem());
        if(deleteSubCategory){
            for(int i=subCategoryListView.getCount()-1; i >= 0; i--){
                if(subCategoryListViewAdapter.getSelectedIds().get(i)){
                    subCategoryObjectArrayList.remove(i);
                }
            }
            getActionMode().finish();
            subCategoryListViewAdapter.notifyDataSetChanged();
            setListViewVisibility();
        }
        else
            Toast.makeText(this, "Failed to delete this file", Toast.LENGTH_SHORT).show();
        getActionMode().finish();
    }

    public void searchSubCategoryItem(String keyword){
        subCategoryObjectArrayList.clear();
        subCategoryObjectArrayList  = customSqliteHelper.searchAllSubCategoryByQuery(categoryID, page, subCategoryObjectArrayList, keyword);
        subCategoryListViewAdapter.notifyDataSetChanged();
    }
}
