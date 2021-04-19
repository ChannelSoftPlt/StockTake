package com.jby.stocktake.exportFeature.file;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.jby.stocktake.database.CustomSqliteHelper;
import com.jby.stocktake.database.FrameworkClass;
import com.jby.stocktake.database.ResultCallBack;
import com.jby.stocktake.exportFeature.category.ExportCategoryActivity;
import com.jby.stocktake.R;
import com.jby.stocktake.home.HomeExpireDialog;
import com.jby.stocktake.login.LoginActivity;
import com.jby.stocktake.others.CustomListView;
import com.jby.stocktake.setting.DeviceNameDialog;
import com.jby.stocktake.setting.SettingActivity;
import com.jby.stocktake.shareObject.ApiDataObject;
import com.jby.stocktake.shareObject.ApiManager;
import com.jby.stocktake.shareObject.AsyncTaskManager;
import com.jby.stocktake.shareObject.CustomToast;
import com.jby.stocktake.shareObject.NetworkConnection;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.stocktake.database.CustomSqliteHelper.TB_CATEGORY;
import static com.jby.stocktake.database.CustomSqliteHelper.TB_FILE;
import static com.jby.stocktake.database.CustomSqliteHelper.TB_SUB_CATEGORY;

public class ExportFileActivity extends AppCompatActivity implements View.OnClickListener,
        ExportFileListViewAdapter.CategoryAdapterCallBack, SwipeRefreshLayout.OnRefreshListener,
        AdapterView.OnItemClickListener, ResultCallBack,
        DeviceNameDialog.DeviceNameDialogCallback {

    private TextView actionBarTitle;
    private ImageView actionBarSearch, actionbarSetting, actionbarBackButton, actionBarCancel;
    private View actionBarLayout;

    private ExportFileListViewAdapter exportFileListViewAdapter;
    private ArrayList<ExportFileListViewObject> exportFileListViewObjectArrayList;
    private CustomListView categoryFragmentListView;
    private SwipeRefreshLayout categoryFragmentSwipeRefreshLayout;
    //download layout
    private ProgressBar exportFileActivityDownloadProgress, exportFileActivityProgressBar;
    private LinearLayout exportFileActivityDownloadLayout;
    private TextView exportFileActivityDownloadMaxNumber, exportFileActivityDownloadCurrentNumber;
    private int currentProgress = 0, maxProgress = 0;
    private String fileID;
    //activation
    int checkUserActivation = 0;
    //database purpose
    private CustomSqliteHelper customSqliteHelper;
    private FrameworkClass categoryTable, subCategoryTable, fileTable;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler mHandler = new Handler();
    //exit
    boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_file);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        actionBarTitle = (TextView) findViewById(R.id.actionBar_title);
        actionBarSearch =  findViewById(R.id.actionBar_search);
        actionbarSetting = findViewById(R.id.actionBar_setting);
        actionbarBackButton =  findViewById(R.id.actionBar_back_button);
        actionBarCancel =  findViewById(R.id.actionBar_cancel);
        actionBarLayout = findViewById(R.id.activity_main_layout_action_bar);

        categoryFragmentListView = (CustomListView) findViewById(R.id.fragment_category_list_view);
        categoryFragmentSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_layout);

        exportFileActivityDownloadProgress = findViewById(R.id.activity_file_progress_bar);
        exportFileActivityProgressBar = findViewById(R.id.activity_file_loading_bar);
        exportFileActivityDownloadLayout = findViewById(R.id.activity_export_file_download_layout);
        exportFileActivityDownloadMaxNumber = findViewById(R.id.activity_file_download_max_num);
        exportFileActivityDownloadCurrentNumber = findViewById(R.id.activity_file_download_current_num);
        //database purpose
        customSqliteHelper = new CustomSqliteHelper(this);
        fileTable = new FrameworkClass(this, this, customSqliteHelper, TB_FILE);
        categoryTable = new FrameworkClass(this, this, customSqliteHelper, TB_CATEGORY);
        subCategoryTable = new FrameworkClass(this, this, customSqliteHelper, TB_SUB_CATEGORY);
        Stetho.initializeWithDefaults(this);
    }

    private void objectSetting() {
        actionbarSetting.setOnClickListener(this);
        actionBarCancel.setOnClickListener(this);
        actionBarSearch.setVisibility(View.GONE);
        actionbarBackButton.setVisibility(View.GONE);

        categoryFragmentSwipeRefreshLayout.setOnRefreshListener(this);

        categoryFragmentListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        categoryFragmentListView.setOnItemClickListener(this);

        setActionBarTitle();
        checkingFileDetail();
        checkingSetting();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void checkingFileDetail() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int numFile = fileTable.new Read("id").count();
                //first time login then create two new file for user
                if (numFile < 2) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            downloadFile();
                        }
                    }).start();
                } else
                    setupFileDetail();
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.actionBar_setting:
                if(exportFileActivityProgressBar.getVisibility() != View.VISIBLE) openSetting();
                break;
        }
    }

    public void setupFileDetail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                exportFileListViewObjectArrayList = new ArrayList<>();
                exportFileListViewObjectArrayList = new CustomSqliteHelper(ExportFileActivity.this).fetchAll();
                exportFileListViewAdapter = new ExportFileListViewAdapter(ExportFileActivity.this, exportFileListViewObjectArrayList, ExportFileActivity.this);
                categoryFragmentListView.setAdapter(exportFileListViewAdapter);
                exportFileListViewAdapter.notifyDataSetChanged();
            }
        });
    }

    public void setActionBarTitle() {
        actionBarTitle.setText(R.string.activity_export_file_title);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(exportFileActivityProgressBar.getVisibility() != View.VISIBLE){
            switch (adapterView.getId()) {
                case R.id.fragment_category_list_view:
                    if (!exportFileListViewObjectArrayList.get(i).getCategory_numb().equals("0")) {
                        Bundle bundle = new Bundle();
                        bundle.putString("file_id", exportFileListViewObjectArrayList.get(i).getId());
                        startActivity(new Intent(this, ExportCategoryActivity.class).putExtras(bundle));
                        clickEffect(view);
                    } else Toast.makeText(this, "Blank File!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void clickEffect(View view) {
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
                setupFileDetail();
                categoryFragmentSwipeRefreshLayout.setRefreshing(false);
            }
        }, 50);
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
        startActivityForResult(new Intent(this, SettingActivity.class), 3);
    }

    @Override
    protected void onDestroy() {
        customSqliteHelper.close();
        fileTable.close();
        categoryTable.close();
        subCategoryTable.close();
        super.onDestroy();
    }

    /*--------------------------------------------------------------------------create file---------------------------------------------------------------*/
    private void downloadFile() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().download,
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
                    Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("file");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            fileTable.new create(
                                    "file_name, created_at",
                                    "File " + (i + 1) + "," + jsonArray.getJSONObject(i).getString("created_at"))
                                    .perform();
                        }
                    }
                } else {
                    new CustomToast(this, "Network Error!");
                    showProgressBar(false);
                }

            } catch (InterruptedException e) {
                new CustomToast(this, "Interrupted Exception!");
                showProgressBar(false);
                e.printStackTrace();
            } catch (ExecutionException e) {
                new CustomToast(this, "Execution Exception!");
                showProgressBar(false);
                e.printStackTrace();
            } catch (JSONException e) {
                new CustomToast(this, "JSON Exception!!");
                showProgressBar(false);
                e.printStackTrace();
            } catch (TimeoutException e) {
                new CustomToast(this, "Connection Time Out!");
                showProgressBar(false);
                e.printStackTrace();
            }
        }
        setupFileDetail();
    }

    /*-------------------------------------------------------------------------exit purpose-----------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        exit();
    }

    public void exit() {
        if (exit) {
            System.exit(0); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }
    /*-------------------------------------------------------------------------initial checking-----------------------------------------------------------------*/

    public void checkingSetting() {
        String deviceName = SharedPreferenceManager.getDeviceName(this);
        if (!deviceName.equals("")) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null)
                checkUserActivation = bundle.getInt("status");

            if (checkUserActivation == 0) {
                if (new NetworkConnection(this).checkNetworkConnection()) {
                    startUp(checkUserActivation);
                }
            }
        } else popOutDeviceNameDialog();
    }

    public void startUp(int status) {
        if (status != 1) {
            showProgressBar(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    checkUserActivation();
                }
            }).start();
        }
    }

    public void checkUserActivation() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("token", SharedPreferenceManager.getDeviceToken(this)));
        apiDataObjectArrayList.add(new ApiDataObject("version", SharedPreferenceManager.getVersion(this)));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().home,
                new ApiManager().getResultParameter(
                        "",
                        new ApiManager().setData(apiDataObjectArrayList),
                        ""
                )
        );
        doingInBackground();
    }

    public void doingInBackground() {

        asyncTaskManager.execute();

        if (!asyncTaskManager.isCancelled()) {
            try {
                jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);

                if (jsonObjectLoginResponse != null) {
                    int userPackage = jsonObjectLoginResponse.getInt("package");
                    JSONArray fileDate = jsonObjectLoginResponse.getJSONArray("file_date");
                    SharedPreferenceManager.setUserPackage(this, userPackage);
                    //update user status based on activation date
                    if(jsonObjectLoginResponse.getString("status").equals("3")) SharedPreferenceManager.setUserStatus(this, "1");
                    else SharedPreferenceManager.setUserStatus(this, "0");

                    switch (jsonObjectLoginResponse.getString("status")) {
                        case "2":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    almostExpired();
                                }
                            });
                            break;
                        case "3":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    expiredDialog();
                                }
                            });
                            break;
                        case "4":
                            new CustomToast(this, "Something error with server! Try it later!");

                            break;
                        case "5":
                            getNewVersion(jsonObjectLoginResponse.getString("url"));
                            break;
                    }
                    //check file update purpose
                    checkFileDate(fileDate);
                } else {
                    new CustomToast(this, "Network Error!");
                }

            } catch (InterruptedException e) {
                new CustomToast(this, "Interrupted Exception!");
                e.printStackTrace();
            } catch (ExecutionException e) {
                new CustomToast(this, "Execution Exception!");
                e.printStackTrace();
            } catch (JSONException e) {
                new CustomToast(this, "JSON Exception!");
                e.printStackTrace();
            } catch (TimeoutException e) {
                new CustomToast(this, "Connection Time Out!");
                e.printStackTrace();
            }
        }
        showProgressBar(false);
    }

    public void almostExpired() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notice");
        builder.setMessage("Hi, your member activation period is almost to has expired! Remember to renew it before your account is expired!");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "I Got It",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void expiredDialog() {
        DialogFragment dialogFragment = new HomeExpireDialog();
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    public void getNewVersion(final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ExportFileActivity.this);
                builder.setTitle("Notice");
                builder.setMessage("A new version is ready now!");
                builder.setCancelable(false);

                builder.setPositiveButton(
                        "Get It Now",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(browserIntent);
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public void checkFileDate(JSONArray jsonArray) {
        StringBuilder message = new StringBuilder("We detect that there are some changes from your original file.\n");
        boolean isUpdate = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                for (int j = 0; j < jsonArray.length(); j++) {
                    if (exportFileListViewObjectArrayList.get(j).getId().equals(jsonArray.getJSONObject(i).getString("file_id")) && !exportFileListViewObjectArrayList.get(j).getCategory_numb().equals("0")) {
                        if (!exportFileListViewObjectArrayList.get(j).getDate().equals(jsonArray.getJSONObject(i).getString("created_at"))) {
                            message.append(i + 1).append(") ").append("File ").append(exportFileListViewObjectArrayList.get(j).getId()).append(" was changed!\n");
                            isUpdate = true;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (isUpdate) detectFileUpdatedDialog(message.toString());
    }

    public void detectFileUpdatedDialog(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ExportFileActivity.this);
                builder.setTitle("Notice");
                builder.setMessage(message);
                builder.setCancelable(true);
                builder.setPositiveButton(
                        "I Got It",
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
    /*-------------------------------------------------------------------sync purpose---------------------------------------------------------------------------*/

    public void downloadRequestDialog(final String fileID, String fileName) {
        this.fileID = fileID;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Update " + fileName + " ? *Once confirm your previous data will be removed! ");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int id) {
                        //setup download layout
                        showProgressBar(true);
                        clearPreviousRecord(fileID);
                        dialog.dismiss();
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

    private void downloadFileContent(boolean category, String fileID) {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));

        if (category) {
            apiDataObjectArrayList.add(new ApiDataObject("file_id", fileID));
            apiDataObjectArrayList.add(new ApiDataObject("category", "1"));
        } else {
            apiDataObjectArrayList.add(new ApiDataObject("file_id", fileID));
            apiDataObjectArrayList.add(new ApiDataObject("sub_category", "1"));
        }

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().download,
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
                    Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        if (category) {
                            fileTable.new Update("created_at", jsonObjectLoginResponse.getString("file_date")).where("id = ?", fileID).perform();
                            storeCategory(jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("category"));
                        } else
                            storeSubCategory(jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("sub_category"));
                    } else {
                        showProgressBar(false);
                        new CustomToast(this, "This File is Empty!");
                    }
                } else {
                    new CustomToast(this, "Network Error!");
                    showProgressBar(false);
                }

            } catch (InterruptedException e) {
                new CustomToast(this, "Interrupted Exception!");
                showProgressBar(false);
                e.printStackTrace();
            } catch (ExecutionException e) {
                new CustomToast(this, "Execution Exception!");
                showProgressBar(false);
                e.printStackTrace();
            } catch (JSONException e) {
                new CustomToast(this, "JSON Exception!!");
                showProgressBar(false);
                e.printStackTrace();
            } catch (TimeoutException e) {
                new CustomToast(this, "Connection Time Out!");
                showProgressBar(false);
                e.printStackTrace();
            }
        }
    }

    private void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                exportFileActivityProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void clearPreviousRecord(final String fileID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (customSqliteHelper.deleteFileItem(fileID)) {
                    downloadFileContent(true, fileID);
                }
            }
        }).start();
    }

    private void storeCategory(JSONArray jsonArray) {
        maxProgress = jsonArray.length();
        setUpDownloadLayout();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                categoryTable.new create("id, category_name, file_id, created_at",
                        jsonArray.getJSONObject(i).getString("id") + "," +
                                jsonArray.getJSONObject(i).getString("category_name") + " ," +
                                fileID + " ," +
                                jsonArray.getJSONObject(i).getString("created_at")).perform();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //start to download subcategory
        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadFileContent(false, fileID);
            }
        }).start();

    }

    private void storeSubCategory(JSONArray jsonArray) {
        maxProgress = jsonArray.length();
        setUpDownloadLayout();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                subCategoryTable.new create("description, barcode, item_code, check_quantity, system_quantity, category_id, selling_price, cost_price, date_create, time_create, priority, file_id",
                        jsonArray.getJSONObject(i).getString("description") + "," +
                                jsonArray.getJSONObject(i).getString("barcode") + " ," +
                                jsonArray.getJSONObject(i).getString("item_code") + " ," +
                                jsonArray.getJSONObject(i).getString("check_quantity") + " ," +
                                jsonArray.getJSONObject(i).getString("system_quantity") + " ," +
                                jsonArray.getJSONObject(i).getString("category_id") + " ," +
                                jsonArray.getJSONObject(i).getString("selling_price") + " ," +
                                jsonArray.getJSONObject(i).getString("cost_price") + " ," +
                                jsonArray.getJSONObject(i).getString("date_create") + " ," +
                                jsonArray.getJSONObject(i).getString("time_create") + " ," +
                                jsonArray.getJSONObject(i).getString("priority") + " ," +
                                fileID).perform();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        new CustomToast(this, "Download Successfully!");
        setupFileDetail();
        showProgressBar(false);
    }

    //--------------------------------------------------------------------download layout purpose-------------------------------------------------------------------
    private void setUpDownloadLayout() {
        runOnUiThread(new Runnable() {
            public void run() {
                exportFileActivityDownloadLayout.setVisibility(View.VISIBLE);
                exportFileActivityDownloadMaxNumber.setText("/" + maxProgress);
                exportFileActivityDownloadProgress.setMax(maxProgress);
            }
        });
    }

    private void setCurrentProgress() {
        exportFileActivityDownloadCurrentNumber.setText(String.valueOf(currentProgress));
        exportFileActivityDownloadProgress.setProgress(currentProgress);
        if (maxProgress == currentProgress) {
            exportFileActivityDownloadLayout.setVisibility(View.GONE);
            currentProgress = 0;
        }
    }

    /*--------------------------------------------------------------------device name----------------------------------------------------------------------------*/
    @Override
    public void deviceNameSetting() {

    }

    public void popOutDeviceNameDialog() {
        DialogFragment dialogFragment = new DeviceNameDialog();
        Bundle bundle = new Bundle();
        bundle.putString("from", "home");
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    /*----------------------------------------------------------------------framework purpose----------------------------------------------------------------------*/
    @Override
    public void createResult(String status) {
        runOnUiThread(new Runnable() {
            public void run() {
                currentProgress++;
                setCurrentProgress();
            }
        });
    }

    @Override
    public void readResult(String result) {
    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {
    }

    /*-------------------------------------------------------------------logt out------------------------------------------------------------------------------*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 3)
            logOutSetting();
    }
}