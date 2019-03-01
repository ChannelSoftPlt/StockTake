package com.jby.stocktake.exportFeature.category;

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
import com.jby.stocktake.exportFeature.category.searchFeature.ExportCategorySearchDialog;
import com.jby.stocktake.login.LoginActivity;
import com.jby.stocktake.others.CustomListView;
import com.jby.stocktake.others.SquareHeightLinearLayout;
import com.jby.stocktake.setting.SettingActivity;
import com.jby.stocktake.shareObject.AnimationUtility;
import com.jby.stocktake.shareObject.CustomToast;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;
import com.jby.stocktake.exportFeature.subcategory.SubCategoryActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.jby.stocktake.database.CustomSqliteHelper.TB_SUB_CATEGORY;

public class ExportCategoryActivity extends AppCompatActivity implements View.OnClickListener,
        ExportCategoryInsertDialog.CreateDialogCallBack, ExportCategoryListViewAdapter.CategoryAdapterCallBack,
        ExportCategoryUpdateDialog.UpdateDialogCallBack,
        AbsListView.MultiChoiceModeListener, AbsListView.OnScrollListener,
        CustomListView.OnDetectScrollListener, AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private TextView actionBarTitle;
    private ImageView actionBarSearch, actionbarSetting, actionbarBackButton, actionBarCancel;
    private LinearLayout actionBarSearchLayout, actionBarDefaultLayout;
    private EditText actionBarSearchField;
    private View actionBarLayout;
    //list view
    private ExportCategoryListViewAdapter categoryFragmentListViewAdapter;
    private ArrayList<ExportCategoryListViewObject> categoryFragmentListViewObjectArrayList;
    private CustomListView categoryFragmentListView;

    private ImageView categoryFragmentFloatingButton;
    private LinearLayout categoryFragmentResultNotFound, categoryActivityAllFileLayout;
    private TextView categoryActivityAllFileQuantity;
    private SwipeRefreshLayout categoryFragmentSwipeRefreshLayout;
    private ProgressDialog pd;
    private String fileID;

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
    String subCategoryNum;
    String categoryID;
    //    load more data
    View listViewFooter;
    int page = 1;
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
    CustomSqliteHelper customSqliteHelper;
    //all file or category purpose
    private boolean readAllFile = false;
    //prevent double click
    private boolean click = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        actionBarTitle = (TextView) findViewById(R.id.actionBar_title);
        actionBarSearch =  findViewById(R.id.actionBar_search);
        actionbarSetting =  findViewById(R.id.actionBar_setting);
        actionbarBackButton =  findViewById(R.id.actionBar_back_button);
        actionBarCancel = findViewById(R.id.actionBar_cancel);
        actionBarSearchLayout = (LinearLayout) findViewById(R.id.actionBar_search_layout);
        actionBarDefaultLayout = (LinearLayout) findViewById(R.id.actionBar_icon_layout);
        actionBarSearchField = (EditText) findViewById(R.id.action_bar_search_field);
        actionBarLayout = findViewById(R.id.activity_main_layout_action_bar);

        categoryFragmentListView = (CustomListView) findViewById(R.id.fragment_category_list_view);
        categoryFragmentFloatingButton = (ImageView) findViewById(R.id.fragment_category_floating_button);
        categoryFragmentResultNotFound = (LinearLayout) findViewById(R.id.not_found);
        categoryFragmentSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_layout);
        categoryActivityAllFileLayout = findViewById(R.id.activity_category_all_file_layout);
        categoryActivityAllFileQuantity = findViewById(R.id.activity_category_all_file_quantity);

        categoryFragmentListViewObjectArrayList = new ArrayList<>();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        listViewFooter = ((LayoutInflater) Objects.requireNonNull(this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)))
                .inflate(R.layout.fragment_category_list_view_footer, null, false);

        fm = getSupportFragmentManager();
        pd = new ProgressDialog(this);

        customSqliteHelper = new CustomSqliteHelper(this);
    }

    private void objectSetting() {
        actionBarSearch.setOnClickListener(this);
        actionbarSetting.setOnClickListener(this);
        actionbarBackButton.setOnClickListener(this);
        actionBarCancel.setOnClickListener(this);
        categoryActivityAllFileLayout.setOnClickListener(this);
        categoryFragmentSwipeRefreshLayout.setOnRefreshListener(this);

        categoryFragmentListView.setOnScrollListener(this);
        categoryFragmentListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        categoryFragmentListView.setMultiChoiceModeListener(this);
        categoryFragmentListView.setOnDetectScrollListener(this);
        categoryFragmentListView.setOnItemClickListener(this);
        categoryFragmentFloatingButton.setOnClickListener(this);
        pd.setMessage("Loading...");
        pd.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!load) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                fileID = bundle.getString("file_id");
                String fileName = bundle.getString("file_name");
                setActionBarTitle(fileName);
                setUpListView();
                fetchAll();
                load = true;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
            case R.id.activity_category_all_file_layout:
                clickEffect(view);
                openAllFile();
        }
    }

    @Override
    public void onBackPressed() {
        if (actionBarSearchLayout.getVisibility() == View.VISIBLE) {
            showSearchView(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        customSqliteHelper.close();
        super.onDestroy();
    }

    public void setActionBarTitle(String title) {
        actionBarTitle.setText(title);
    }

    /*---------------------------------------------------------multiple delete purpose---------------------------------------------------------------------*/
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
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        hideActionbar(true);
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

    public List getDeleteItem() {
        for (int i = 0; i < categoryFragmentListViewObjectArrayList.size(); i++) {
            if (checked.get(i)) {
                list.add(categoryFragmentListViewObjectArrayList.get(i).getId());
            }
        }
        return list;
    }

    public void deleteCategoryItem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean deleteCategory = customSqliteHelper.deleteCategory(getDeleteItem());
                if (deleteCategory) {
                    for (int i = categoryFragmentListView.getCount() - 1; i >= 0; i--) {
                        if (categoryFragmentListViewAdapter.getSelectedIds().get(i)) {
                            categoryFragmentListViewObjectArrayList.remove(i);
                        }
                    }
                    //update view after update
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getActionMode().finish();
                        }
                    });
                    setListViewVisibility();
                } else
                    new CustomToast(ExportCategoryActivity.this, "Failed to delete this file");
            }
        }).start();
    }

    public void alertMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure you want to delete these item?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "I'm Sure",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteCategoryItem();
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

    /*--------------------------------------------------------paging purpose--------------------------------------------------------------------------------------------*/
    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                categoryFragmentListView.setClickable(true);
            }
        },100);
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
        final int position = firstVisibleItem + visibleItemCount;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!finishLoadAll) {
                    // Check if bottom has been reached
                    if (position >= totalItemCount && totalItemCount > 0 && !isLoading && isScroll) {
                        isLoading = true;
                        page++;
                        fetchAll();
                    }
                }
            }
        }, 10);
        if (!isScroll) {
            if (categoryFragmentListView.getChildAt(0) != null) {
                categoryFragmentSwipeRefreshLayout.setEnabled(categoryFragmentListView.getFirstVisiblePosition() == 0 && categoryFragmentListView.getChildAt(0).getTop() == 0);
            }
        }
        categoryFragmentListView.setClickable(false);
    }

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

    public ActionMode getActionMode() {
        return actionMode;
    }

    @Override
    public void onRefresh() {
        pd.show();
        //reset all setting
        isScroll = false;
        successToGetDataBefore = false;
        finishLoadAll = false;
        isLoading = true;
        page = 1;
        //list purpose
        categoryFragmentListViewObjectArrayList.clear();
        categoryFragmentSwipeRefreshLayout.setRefreshing(false);
        fetchAll();
        categoryFragmentListViewAdapter.notifyDataSetChanged();
    }

    public void showKeyBoard() {
        final InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (resultCode == 2) {
            String newQuantity = data.getStringExtra("quantity");
            updateSelectedItemQuantity(newQuantity);
        } else if (resultCode == 3) {
            logOutSetting();
        }
    }

    /*-----------------------------------------------------------category CRUD purpose--------------------------------------------------------------------*/
    //create purpose
    public void openInsertDialog() {
        dialogFragment = new ExportCategoryInsertDialog();
        dialogFragment.show(fm, "");
        showKeyBoard();
    }

    @Override
    public void createNewCategoryItem(String category_name) {
        int saveCategory = customSqliteHelper.saveCategory(category_name, fileID);
        if (saveCategory == 1) {
            categoryFragmentListViewObjectArrayList.clear();
            page = 1;
            fetchAll();
            categoryFragmentListViewAdapter.notifyDataSetChanged();
        } else if (saveCategory == 2)
            alertMessageCategoryExisted();
        else
            Toast.makeText(this, "Failed to store this category", Toast.LENGTH_SHORT).show();
    }

    //update purpose
    @Override
    public void updateCategoryItem(String category_name, String category_id) {
        int updateCategory = customSqliteHelper.updateCategory(category_name, category_id);
        if (updateCategory == 1) {
            categoryFragmentListViewObjectArrayList.set(getUpdatePosition(), new ExportCategoryListViewObject(
                    getCategoryID(),
                    category_name,
                    getSubCategoryNum()));
            categoryFragmentListViewAdapter.notifyDataSetChanged();
        } else if (updateCategory == 2)
            alertMessageCategoryExisted();
        else
            Toast.makeText(this, "Failed to update this category", Toast.LENGTH_SHORT).show();
    }

    public void openUpdateDialog(String category_id, String category_name, int position, String subCategory_num) {
//        for update local purpose
        editPosition = position;
        subCategoryNum = subCategory_num;
        categoryID = category_id;

        dialogFragment = new ExportCategoryUpdateDialog();
        bundle = new Bundle();
        bundle.putString("category_id", category_id);
        bundle.putString("category_name", category_name);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
        showKeyBoard();
    }

    public int getUpdatePosition() {
        return editPosition;
    }

    public void updateSelectedItemQuantity(String newQuantity) {
        categoryFragmentListViewObjectArrayList.set(selectedPosition, new ExportCategoryListViewObject(
                selectedCategoryID,
                selectedCategoryName,
                newQuantity
        ));
        categoryFragmentListViewAdapter.notifyDataSetChanged();
    }

    public String getSubCategoryNum() {
        return subCategoryNum;
    }

    public String getCategoryID() {
        return categoryID;
    }

    /*-------------------------------------------------------list view purpose----------------------------------------------------------------------------*/
    public void fetchAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //        initialize and assign data into array list
                    JSONArray jsonArray = customSqliteHelper.fetchAllCategory(fileID, page).getJSONArray("category");
                    if(jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            categoryFragmentListViewObjectArrayList.add(new ExportCategoryListViewObject(
                                    jsonArray.getJSONObject(i).getString("id"),
                                    jsonArray.getJSONObject(i).getString("category"),
                                    jsonArray.getJSONObject(i).getString("subCategory_numb")
                            ));
                        }
                    }
                    else finishLoadAll = true;
                    setListViewVisibility();
                    isLoading = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void countAllFileQuantity(){
        final int[] count = {0};
        new Thread(new Runnable() {
            @Override
            public void run() {
                count[0] = customSqliteHelper.countAllFileQuantity(fileID);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        categoryActivityAllFileQuantity.setText(String.valueOf(count[0]) + " Items");
                    }
                });
            }
        }).start();

    }

    public void setUpListView() {
        categoryFragmentListViewAdapter = new ExportCategoryListViewAdapter(this, categoryFragmentListViewObjectArrayList, this);
        categoryFragmentListView.setAdapter(categoryFragmentListViewAdapter);
    }

    public void setListViewVisibility() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (categoryFragmentListViewObjectArrayList.size() > 0) {
//            if data found
                    categoryFragmentListView.setVisibility(View.VISIBLE);
                    categoryActivityAllFileLayout.setVisibility(View.VISIBLE);
                    categoryFragmentResultNotFound.setVisibility(View.GONE);
                    countAllFileQuantity();
                } else {
//            if not found
                    categoryFragmentListView.setVisibility(View.INVISIBLE);
                    categoryActivityAllFileLayout.setVisibility(View.GONE);
                    categoryFragmentResultNotFound.setVisibility(View.VISIBLE);

                }
                categoryFragmentListViewAdapter.notifyDataSetChanged();
                pd.dismiss();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.fragment_category_list_view:
                if(click){
                    click = false;
                    clickEffect(view);
                    selectedCategoryID = categoryFragmentListViewObjectArrayList.get(i).getId();
                    selectedCategoryName = categoryFragmentListViewObjectArrayList.get(i).getCategory();
                    selectedPosition = i;
                    readAllFile = false;

                    bundle = new Bundle();
                    bundle.putString("category_id", selectedCategoryID);
                    bundle.putString("category_name", selectedCategoryName);
                    bundle.putString("file_id", fileID);
                    bundle.putString("fromListView", "categoryLV");
                    bundle.putBoolean("readAllFile", readAllFile);
                    openSubCategory();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            click = true;
                        }
                    },300);
                }
        }
    }

    private void openAllFile(){
        readAllFile = true;

        bundle = new Bundle();
        bundle.putBoolean("readAllFile", readAllFile);
        bundle.putString("file_id", fileID);
        openSubCategory();
    }

    private void openSubCategory(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivityForResult(new Intent(ExportCategoryActivity.this, SubCategoryActivity.class).putExtras(bundle), 2);
            }
        }, 50);
    }

    /*----------------------------------------------------search purpose---------------------------------------------------------------------------------*/
    public void openSearchDialog() {
        dialogFragment = new ExportCategorySearchDialog();
        bundle = new Bundle();
        bundle.putString("file_id", fileID);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    public void showSearchView(boolean show) {
        if (show) {
            new AnimationUtility().slideOut(this, actionBarDefaultLayout);
            new AnimationUtility().slideOut(this, actionBarTitle);
            new AnimationUtility().minimize(this, actionBarSearchLayout);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (imm != null)
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    new AnimationUtility().slideOut(ExportCategoryActivity.this, categoryFragmentFloatingButton);
                }
            }, 200);
            actionBarSearchField.requestFocus();
        } else {
            new AnimationUtility().minimize(this, actionBarDefaultLayout);
            new AnimationUtility().minimize(this, actionBarTitle);
            new AnimationUtility().slideOut(this, actionBarSearchLayout);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (imm != null)
                        imm.hideSoftInputFromWindow(actionBarSearchField.getWindowToken(), 0);
                    new AnimationUtility().fastFadeInVisible(ExportCategoryActivity.this, categoryFragmentFloatingButton);
                }
            }, 200);
            actionBarSearchField.setText("");
        }
    }

    /*--------------------------------------------------------other--------------------------------------------------------------------------------------*/

    public void alertMessageCategoryExisted() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("This category is existed!");
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

    public void hideFloatingButton() {
        if (!isScroll) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new AnimationUtility().slideOut(getApplicationContext(), categoryFragmentFloatingButton);
                    actionBarLayout.setVisibility(View.GONE);
                }
            }, 100);
        }

    }

    public void showFloatingButton() {
        if (isScroll) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    categoryFragmentFloatingButton.setVisibility(View.VISIBLE);
                    new AnimationUtility().fadeInVisible(getApplicationContext(), actionBarLayout);
                }
            }, 100);
        }
    }

    public void hideActionbar(boolean hide) {
        if (hide)
            new AnimationUtility().slideOut(this, actionBarLayout);
        else
            new AnimationUtility().minimize(this, actionBarLayout);
    }

    public void clickEffect(View view) {
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    public void logOutSetting() {
        SharedPreferenceManager.setUserID(this, "default");
        SharedPreferenceManager.setUserPassword(this, "default");
        SharedPreferenceManager.setDeviceToken(this, "default");
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void openSetting() {
        intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, 3);
    }

}
