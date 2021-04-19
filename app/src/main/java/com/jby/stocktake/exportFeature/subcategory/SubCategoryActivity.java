package com.jby.stocktake.exportFeature.subcategory;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.R;
import com.jby.stocktake.database.CustomSqliteHelper;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryFilterDialog;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryMoveItemDialog;
import com.jby.stocktake.login.LoginActivity;
import com.jby.stocktake.others.CSVWriter;
import com.jby.stocktake.others.CustomListView;
import com.jby.stocktake.others.SquareHeightLinearLayout;
import com.jby.stocktake.setting.SettingActivity;
import com.jby.stocktake.shareObject.AnimationUtility;
import com.jby.stocktake.shareObject.ApiManager;
import com.jby.stocktake.shareObject.CustomToast;
import com.jby.stocktake.shareObject.NetworkConnection;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryExistedDialog;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryInsertDialog;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryListViewAdapter;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryObject;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryUpdateDialog;
import com.jby.stocktake.exportFeature.subcategory.subcategory.takeAction.TakeActionActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.jby.stocktake.login.LoginActivity.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

public class SubCategoryActivity extends AppCompatActivity implements CustomListView.OnDetectScrollListener,
        AbsListView.OnScrollListener, View.OnClickListener, SubCategoryUpdateDialog.SubCategoryUpdateDialogCallBack,
        AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener,
        SubCategoryExistedDialog.ExistedDialogCallBack, SubCategoryInsertDialog.CreateDialogCallBack,
        SubCategoryListViewAdapter.SubCategoryListViewAdapterCallBack, SubCategoryMoveItemDialog.SubCategoryMoveItemDialogCallBack,
        SubCategoryFilterDialog.ImportSubCategoryFilterDialogCallBack {

    private TextView actionBarTitle;
    private ImageView actionBarSearch, actionbarSetting, actionbarBackButton, actionBarCancel, actionBarMove, actionBarFilter;
    private LinearLayout actionBarSearchLayout, actionBarDefaultLayout;
    private EditText actionBarSearchField;
    private View actionBar;
    Intent intent;

    EditText subCategoryScanResult;
    CustomListView subCategoryListView;
    SubCategoryListViewAdapter subCategoryListViewAdapter;
    ArrayList<SubCategoryObject> subCategoryObjectArrayList;
    ImageView subCategoryFloatingButton, subCategoryExportButton;
    LinearLayout subCategoryNotFound, subCategoryLabelListView;

    private Handler mHandler = new Handler();
    //    SQLite purpose
    CustomSqliteHelper customSqliteHelper;
    //    intent purpose
    String categoryID, categoryName, selectedBarcode, fromListView = "categoryLV", fileID;
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
    //    insert barcode
    int count = 0;
    //    delete purpose
    SparseBooleanArray checkDeleteItem;
    List<String> list = new ArrayList<String>();
    ActionMode actionMode;
    MediaPlayer scanSound, errorSound;
    //    prevent reload the data
    private boolean load = false;
    //prevent scan too fast
    private boolean enableScan = true;
    //move purpose
    private RelativeLayout subCategoryActivityMoveLayout;
    private TextView subCategoryActivityMoveLabel;
    private boolean showMoveLayout = false;
    //all file or category
    private boolean readAllFile = false;
    //filter purpose
    private String filter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        actionBarTitle = (TextView) findViewById(R.id.actionBar_title);
        actionBarSearch =  findViewById(R.id.actionBar_search);
        actionbarSetting =  findViewById(R.id.actionBar_setting);
        actionbarBackButton = findViewById(R.id.actionBar_back_button);
        actionBarCancel =  findViewById(R.id.actionBar_cancel);
        actionBar = findViewById(R.id.activity_main_layout_action_bar);
        actionBarSearchLayout = (LinearLayout) findViewById(R.id.actionBar_search_layout);
        actionBarDefaultLayout = (LinearLayout) findViewById(R.id.actionBar_icon_layout);
        actionBarSearchField = (EditText) findViewById(R.id.action_bar_search_field);
        actionBarMove = findViewById(R.id.actionBar_move);
        actionBarFilter = findViewById(R.id.actionBar_filter);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        subCategoryScanResult = (EditText) findViewById(R.id.scanResult);
        subCategoryListView = (CustomListView) findViewById(R.id.fragment_sub_category_list_view);
        subCategoryFloatingButton = (ImageView) findViewById(R.id.fragment_sub_category_floating_button);
        subCategoryNotFound = (LinearLayout) findViewById(R.id.not_found);
        subCategoryLabelListView = (LinearLayout) findViewById(R.id.fragment_sub_category_list_view_label);

        subCategoryExportButton = findViewById(R.id.fragment_sub_category_export_button);

        subCategoryActivityMoveLayout = findViewById(R.id.activity_sub_category_move_layout);
        subCategoryActivityMoveLabel = findViewById(R.id.activity_sub_category_label_selected);
        scanSound = MediaPlayer.create(this, R.raw.scanner_sound);
        errorSound = MediaPlayer.create(this, R.raw.error_sound);

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
        actionBarMove.setOnClickListener(this);
        actionBarFilter.setOnClickListener(this);

        actionbarBackButton.setVisibility(View.VISIBLE);
        actionBarMove.setVisibility(View.VISIBLE);
        actionBarFilter.setVisibility(View.VISIBLE);

        subCategoryListView.setOnDetectScrollListener(this);
        subCategoryListView.setOnScrollListener(this);
        subCategoryListView.setOnItemClickListener(this);
        subCategoryListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        subCategoryListView.setMultiChoiceModeListener(this);
        subCategoryFloatingButton.setOnClickListener(this);
        subCategoryExportButton.setOnClickListener(this);
        subCategoryScanResult.setOnClickListener(this);

        subCategoryScanResult.addTextChangedListener(new MyTextWatcher(subCategoryScanResult));
        actionBarSearchField.addTextChangedListener(new MyTextWatcher(actionBarSearchField));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!load) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                readAllFile = bundle.getBoolean("readAllFile");
                if (readAllFile) {
                    categoryName = "All File";
                } else {
                    categoryID = bundle.getString("category_id");
                    fromListView = bundle.getString("fromListView");
                    selectedBarcode = bundle.getString("selected_barcode");
                    categoryName = bundle.getString("category_name");
                }
                fileID = bundle.getString("file_id");
                actionBarTitle.setText(categoryName);
                setUpListView();
                if (fromListView.equals("categoryLV"))
                    fetchAllSubCategoryData();
                else
                    startSearchFunction(selectedBarcode);
                load = true;
                subCategoryScanResult.requestFocus();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (actionBarSearchLayout.getVisibility() == View.VISIBLE) showSearchView(false);
        else if (subCategoryActivityMoveLayout.getVisibility() == View.VISIBLE) showMoveLayout();
        else super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
            case R.id.actionBar_move:
                showMoveLayout();
                break;
            case R.id.actionBar_filter:
                openFilterDialog();
                break;
            case R.id.fragment_sub_category_floating_button:
                view.setEnabled(false);
                openInsertDialog();
                break;
            case R.id.scanResult:
                final InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(subCategoryScanResult.getWindowToken(), 0);
                break;
            case R.id.fragment_sub_category_export_button:
                if (new NetworkConnection(this).checkNetworkConnection()) uploadFileDialog();
                else
                    Toast.makeText(this, "Please connect to a network!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        customSqliteHelper.close();
        super.onDestroy();
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch (view.getId()) {
                case R.id.scanResult:
                    if (!SharedPreferenceManager.getScanSound(SubCategoryActivity.this).equals("0"))
                        scanSound.start();
                    break;
            }
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.scanResult:
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!subCategoryScanResult.getText().toString().equals("")) {
                                subCategoryScanResult.setEnabled(false);
                                if (SharedPreferenceManager.getQuickScan(SubCategoryActivity.this).equals("0")) {
                                    openInsertDialog();
                                } else {
                                    if (enableScan)
                                        insertSubCategoryItem(subCategoryScanResult.getText().toString(), SharedPreferenceManager.getQuickScanQuantity(SubCategoryActivity.this));
                                    enableScan = false;
                                }
                                subCategoryScanResult.setText("");
                            }
                        }
                    }, 600);
                    break;
                case R.id.action_bar_search_field:
                    page = 1;
                    if (actionBarSearchField.getText().toString().trim().length() >= 1) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (actionBarSearchField.getText().toString().trim().length() >= 1)
                                    searchSubCategoryItem(actionBarSearchField.getText().toString().trim());
                            }
                        }, 200);
                    } else if (actionBarSearchField.getText().toString().trim().length() == 0) {
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

    /*---------------------------------------------------------take action dialog purpose-------------------------------------------------------------------*/
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

    public void closeExistedDialog() {
        dialogFragment.dismiss();
    }

    public void openExistedDialog(final String barcode, final String quantity) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogFragment = new SubCategoryExistedDialog();
                bundle = new Bundle();
                bundle.putString("barcode", barcode);
                bundle.putString("quantity", quantity);
                bundle.putString("category_id", (categoryID));
                dialogFragment.setArguments(bundle);
                dialogFragment.show(fm, "");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (resultCode == 2) {
            //do the things u wanted
            closeExistedDialog();
        } else if (resultCode == 3) {
            logOutSetting();
        }
    }

    public void setCount() {
        this.count = 1;
    }

    /*---------------------------------------------------------multiple delete purpose---------------------------------------------------------------------*/
    //    list view delete event
    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
        final int checkDeleteItemCount = subCategoryListView.getCheckedItemCount();
        // Set the  CAB title according to total checkDeleteItem items
        actionMode.setTitle(checkDeleteItemCount + "  Selected");
        // Calls  toggleSelection method from ListViewAdapter Class
        subCategoryListViewAdapter.toggleSelection(position);
        checkDeleteItem = subCategoryListView.getCheckedItemPositions();
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
                final int checkDeleteItemCount = subCategoryObjectArrayList.size();

                subCategoryListViewAdapter.removeSelection();
                for (int i = 0; i < checkDeleteItemCount; i++) {
                    subCategoryListView.setItemChecked(i, true);
                }
                actionMode.setTitle(checkDeleteItemCount + "  Selected");
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

    public void alertMessage() {
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

    public ActionMode getActionMode() {
        return actionMode;
    }

    public List getDeleteItem() {
        for (int i = 0; i < subCategoryListView.getCount(); i++) {
            if (checkDeleteItem.get(i)) {
                list.add(subCategoryObjectArrayList.get(i).getId());
            }
        }
        return list;
    }

    public void deleteSubCategoryItem() {
        boolean deleteSubCategory = customSqliteHelper.deleteSubCategory(getDeleteItem());
        if (deleteSubCategory) {
            for (int i = subCategoryListView.getCount() - 1; i >= 0; i--) {
                if (subCategoryListViewAdapter.getSelectedIds().get(i)) {
                    subCategoryObjectArrayList.remove(i);
                }
            }
            getActionMode().finish();
            subCategoryListViewAdapter.notifyDataSetChanged();
            setListViewVisibility();
        } else
            Toast.makeText(this, "Failed to delete this file", Toast.LENGTH_SHORT).show();
        getActionMode().finish();
    }

    /*--------------------------------------------------------paging purpose-------------------------------------------------------------------------------*/
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
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                subCategoryListView.setClickable(true);
            }
        }, 100);
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, final int totalItemCount) {
        final int position = firstVisibleItem + visibleItemCount;
        if (!finishLoadAll) {
            // Check if bottom has been reached
            if (position >= totalItemCount && totalItemCount > 0 && !isLoading && isScroll) {
                isLoading = true;
                page++;
                fetchAllSubCategoryData();
            }
        }
        subCategoryListView.setClickable(false);
    }

    /*-----------------------------------------------------------category CRUD purpose--------------------------------------------------------------------*/
    @Override
    public void insertSubCategoryItem(final String barcode, final String quantity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final double scanQuantity = Double.valueOf(quantity);
                long insertSubCategory = customSqliteHelper.saveSubCategory(readAllFile, fileID, count, categoryID, barcode, scanQuantity);

                if (insertSubCategory == 1) {
                    reFetchDateAfterAction();
                }
                //record existed
                else if (insertSubCategory == 3) {
                    errorSound();
                    openExistedDialog(barcode, quantity);
                }
                //failed to store
                else if (insertSubCategory == 4)
                    new CustomToast(SubCategoryActivity.this, "Failed to store this record");
                    //new record is found (only for scan in all file)
                else {
                    errorSound();
                    recordWithoutCategoryDialog(String.valueOf(insertSubCategory), barcode);
                }
                requestFocus(true);
                enableScan = true;
            }
        }).start();
    }

    //    open insert dialog
    public void openInsertDialog() {
        dialogFragment = new SubCategoryInsertDialog();
        if (!subCategoryScanResult.getText().toString().equals("")) {
            bundle = new Bundle();
            bundle.putString("barcode", subCategoryScanResult.getText().toString());
            dialogFragment.setArguments(bundle);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogFragment.show(fm, "");
                subCategoryFloatingButton.setEnabled(true);
            }
        }, 200);
    }

    @Override
    public void updateSubCategoryItem(final String quantity, final String subCategoryId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int updateSubCategory = customSqliteHelper.updateSubCategory(Double.valueOf(quantity), subCategoryId);
                if (updateSubCategory == 1) {
                    reFetchDateAfterAction();
                } else if (updateSubCategory == 2)
                    new CustomToast(SubCategoryActivity.this, "Failed to update this record");
                requestFocus(true);
            }
        }).start();
    }

    private void reFetchDateAfterAction(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reset();
                        fetchAllSubCategoryData();
                    }
                }, 20);
            }
        });
    }

    //    update dialog
    public void openUpdateDialog(String id) {
        dialogFragment = new SubCategoryUpdateDialog();
        bundle = new Bundle();
        bundle.putString("id", id);
        dialogFragment.setArguments(bundle);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogFragment.show(fm, "");
            }
        }, 200);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                subCategoryListView.setEnabled(true);
            }
        }, 250);

    }

    public void recordWithoutCategoryDialog(final String lastInsertId, final String barcode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SubCategoryActivity.this);
                builder.setTitle("Warning");
                builder.setMessage("New record for this barcode " + barcode + "! Move it into a category now!");
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "Move",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, final int id) {
                                moveDialog("default", lastInsertId);
                                dialog.dismiss();
                            }
                        });

                builder.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        reset();
                        fetchAllSubCategoryData();
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void errorSound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!SharedPreferenceManager.getScanSound(SubCategoryActivity.this).equals("0"))
                    errorSound.start();
            }
        });
    }

    /*-------------------------------------------------------list view purpose----------------------------------------------------------------------------*/

    public void setUpListView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                subCategoryListViewAdapter = new SubCategoryListViewAdapter(SubCategoryActivity.this,
                        subCategoryObjectArrayList,
                        categoryID,
                        readAllFile,
                        fileID,
                        SubCategoryActivity.this);
                subCategoryListView.setAdapter(subCategoryListViewAdapter);
            }
        });
    }

    public void fetchAllSubCategoryData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONArray jsonArray = customSqliteHelper.fetchAllSubCategory(readAllFile, categoryID, filter, fileID, page).getJSONArray("sub_category");
                    Log.d("haha", "haha: " + jsonArray);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    try {
                                        subCategoryObjectArrayList.add(new SubCategoryObject(
                                                jsonArray.getJSONObject(i).getString("id"),
                                                jsonArray.getJSONObject(i).getString("barcode"),
                                                jsonArray.getJSONObject(i).getString("checkQuantity"),
                                                jsonArray.getJSONObject(i).getString("systemQuantity"),
                                                jsonArray.getJSONObject(i).getString("date"),
                                                jsonArray.getJSONObject(i).getString("time")
                                        ));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    isLoading = true;
                                }
                            } else {
                                finishLoadAll = true;
                            }
                            setListViewVisibility();
                            isLoading = false;
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setListViewVisibility() {
        if (subCategoryObjectArrayList.size() > 0) {
//            if data found
            subCategoryListView.setVisibility(View.VISIBLE);
            subCategoryNotFound.setVisibility(View.GONE);
            subCategoryLabelListView.setVisibility(View.VISIBLE);
        } else {
//            if not found
            subCategoryListView.setVisibility(View.INVISIBLE);
            subCategoryNotFound.setVisibility(View.VISIBLE);
            subCategoryLabelListView.setVisibility(View.GONE);
        }
        subCategoryListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        clickEffect(view);
        switch (adapterView.getId()) {
            case R.id.fragment_sub_category_list_view:
                if (!showMoveLayout) {
                    adapterView.setEnabled(false);
                    openUpdateDialog(subCategoryObjectArrayList.get(i).getId());
                }
                break;
        }
    }

    /*----------------------------------------------------search purpose---------------------------------------------------------------------------------*/
    public void startSearchFunction(final String selectedBarcode) {
        showSearchView(true);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                actionBarSearchField.setText(selectedBarcode);
            }
        }, 50);
    }

    public void searchSubCategoryItem(String keyword) {
        subCategoryObjectArrayList.clear();
        subCategoryObjectArrayList = customSqliteHelper.searchAllSubCategoryByQuery(readAllFile, categoryID, filter, fileID, page, subCategoryObjectArrayList, keyword);
        subCategoryListViewAdapter.notifyDataSetChanged();
    }

    /*----------------------------------------------------------------move purpose------------------------------------------------------------------------*/
    private void showMoveLayout() {
        showMoveLayout = !showMoveLayout;
        subCategoryActivityMoveLayout.setVisibility(showMoveLayout ? View.VISIBLE : View.GONE);
        subCategoryScanResult.setVisibility(showMoveLayout ? View.INVISIBLE : View.VISIBLE);

        //show check box in adapter
        subCategoryListViewAdapter.showCheckBox();
        if (!showMoveLayout) {
            moveLayoutDismiss();
        }
    }

    public void selectAllItem(View view) {
        for (int i = 0; i < subCategoryObjectArrayList.size(); i++) {
            if (!subCategoryListViewAdapter.getMoveItem().get(i))
                subCategoryListViewAdapter.toggleMoveSelection(i);
        }
        subCategoryListViewAdapter.notifyDataSetChanged();
        selectMoveItem();
    }

    public void selectMoveItem() {
        String label = subCategoryListViewAdapter.getMoveItemCount() + " Selected";
        subCategoryActivityMoveLabel.setText(label);
    }

    private void moveLayoutDismiss() {
        subCategoryListViewAdapter.removeMoveSelection();
        subCategoryActivityMoveLabel.setText("0 Selected");
        requestFocus(true);
    }

    //for button onclick
    public void openMoveDialog(View view) {
        moveDialog("manual", null);
    }

    private void moveDialog(String type, String selectedIdByDefault) {
        bundle = new Bundle();
        bundle.putString("file_id", fileID);
        if (type.equals("manual")) bundle.putString("move_item_id", getMoveItemID());
        else bundle.putString("move_item_id", selectedIdByDefault);

        dialogFragment = new SubCategoryMoveItemDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    private String getMoveItemID() {
        StringBuilder moveItemId = new StringBuilder();
        for (int i = 0; i < subCategoryObjectArrayList.size(); i++) {
            if (subCategoryListViewAdapter.moveItem.get(i)) {
                moveItemId.append(subCategoryObjectArrayList.get(i).getId());
                //check if not last item then add ","
                moveItemId.append(",");
            }
        }
        return moveItemId.toString().substring(0, moveItemId.toString().length() - 1);
    }

    public void afterMove() {
        reset();
        if(showMoveLayout) showMoveLayout();
        fetchAllSubCategoryData();
    }

    /*------------------------------------------------------------------export file----------------------------------------------------------------------*/
    public void checkUserStatus() {
        if(!SharedPreferenceManager.getUserStatus(this).equals("1")) readPhonePermission();
        else userExpiredDialog();
    }

    private void userExpiredDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Your account activation is no longer valid!\nPlease contact with the person in charge in order to renew your membership");
        builder.setCancelable(true);

        builder.setNegativeButton(
                "I Got It",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void readPhonePermission() {
        if (checkReadStatePermission()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                exportCSV();
            }
        }
    }

    public boolean checkReadStatePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    exportCSV();
                }
            } else {
                checkReadStatePermission();
            }
        }
    }

    public void exportCSV() {
        subCategoryExportButton.setEnabled(false);
        Toast.makeText(this, "Uploading file...", Toast.LENGTH_SHORT).show();
//      create directory
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Chafor");
        try {
            if (dir.mkdir()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Directory is not created");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        exportCsvFile(dir);
    }

    private void exportCsvFile(final File dir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String fileName = SharedPreferenceManager.getDeviceName(SubCategoryActivity.this) + "_" + customSqliteHelper.fileName(fileID) + ".csv";
                File file = new File(dir.getAbsolutePath(), fileName);
                try {
                    CSVWriter csvWriter = customSqliteHelper.createCSVFile(categoryID, fileID, new FileWriter(file));
                    csvWriter.close();

                    uploadFile(file);
                } catch (Exception sqlEx) {
                    new CustomToast(SubCategoryActivity.this, "Something Went Wrong!");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        subCategoryExportButton.setEnabled(true);
                    }
                });
            }
        }).start();
    }

    private void uploadFile(File file) {
        String fileExtension = getFileExtension(file.getPath());
        RequestBody file_body = RequestBody.create(MediaType.parse(fileExtension), file);

        RequestBody request_body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("extension", fileExtension)
                .addFormDataPart("user_id", SharedPreferenceManager.getUserID(this))
                .addFormDataPart("uploaded_file", file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/") + 1), file_body)
                .build();

        Request request = new Request.Builder()
                .url(new ApiManager().upload)
                .post(request_body)
                .build();

        try {
            Response response = new OkHttpClient().newCall(request).execute();
            try {
                String status = new JSONObject(response.body().string()).getString("status");
                Log.d("haha", "haha: " + status);
                switch (status) {
                    case "1":
                        new CustomToast(this, "Upload Successfully!");
                        break;
                    case "3":
                        reachedMaxNumOfFile();
                        break;
                    default:
                        new CustomToast(this, "Failed to Upload!");
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!response.isSuccessful()) {
                new CustomToast(SubCategoryActivity.this, "Failed to Upload!");
                throw new IOException("Error : " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileExtension(String path) {
        String[] paths = path.split("\\.");
        return paths[paths.length - 1];
    }

    public void uploadFileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure that you want to export and upload your file to cloud now?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "I'm Sure",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        checkUserStatus();
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

    public void reachedMaxNumOfFile() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SubCategoryActivity.this);
                builder.setTitle("Warning");
                builder.setMessage(R.string.file_max);
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "I'Got It",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    /*----------------------------------------------------------------------filter purpose---------------------------------------------------------------*/
    @Override
    public void filter(String selectedFilter) {
        filter = selectedFilter;
        reset();
        fetchAllSubCategoryData();
    }

    //    open filter dialog
    public void openFilterDialog() {
        dialogFragment = new SubCategoryFilterDialog();
        bundle = new Bundle();
        bundle.putString("current_filter", filter);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    /*--------------------------------------------------------other--------------------------------------------------------------------------------------*/

    public void showSearchView(boolean show) {
        if (show) {
            new AnimationUtility().slideOut(this, actionBarDefaultLayout);
            new AnimationUtility().slideOut(this, actionBarTitle);
            new AnimationUtility().slideOut(this, actionbarBackButton);
            new AnimationUtility().slideOut(this, subCategoryScanResult);
            new AnimationUtility().minimize(this, actionBarSearchLayout);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (imm != null)
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    new AnimationUtility().slideOut(SubCategoryActivity.this, subCategoryFloatingButton);
                    new AnimationUtility().slideOut(SubCategoryActivity.this, subCategoryExportButton);

                }
            }, 200);
            actionBarSearchField.requestFocus();
        } else {
            new AnimationUtility().minimize(this, actionBarDefaultLayout);
            new AnimationUtility().minimize(this, actionBarTitle);
            new AnimationUtility().minimize(this, actionbarBackButton);
            new AnimationUtility().slideOut(this, actionBarSearchLayout);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (imm != null)
                        imm.hideSoftInputFromWindow(actionBarSearchField.getWindowToken(), 0);
                    new AnimationUtility().fastFadeInVisible(SubCategoryActivity.this, subCategoryFloatingButton);
                    new AnimationUtility().fastFadeInVisible(SubCategoryActivity.this, subCategoryExportButton);
                    new AnimationUtility().fastFadeInVisible(SubCategoryActivity.this, subCategoryScanResult);
                    requestFocus(true);
                }
            }, 200);
            actionBarSearchField.setText("");
        }
    }

    public void hideFloatingButton() {
        if (!isScroll) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new AnimationUtility().slideOut(getApplicationContext(), subCategoryFloatingButton);
                }
            }, 100);
        }
    }

    public void showFloatingButton() {
        if (isScroll) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    subCategoryFloatingButton.setVisibility(View.VISIBLE);
                }
            }, 100);
        }
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

    public void setActionBarHidden(boolean hide) {
        if (hide)
            new AnimationUtility().slideOut(this, actionBar);
        else
            new AnimationUtility().minimize(this, actionBar);
    }

    public void requestFocus(final boolean focus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                subCategoryScanResult.setEnabled(focus);
                if (focus) subCategoryScanResult.requestFocus();
            }
        });
    }

    public void reset() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                page = 1;
                finishLoadAll = false;
                subCategoryObjectArrayList.clear();
                subCategoryListViewAdapter.notifyDataSetChanged();
            }
        });
    }

}