package com.jby.stocktake.importFeature.category.searchFeature;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.R;
import com.jby.stocktake.exportFeature.subcategory.SubCategoryActivity;
import com.jby.stocktake.importFeature.category.ImportCategoryUpdateDialog;
import com.jby.stocktake.importFeature.sub_category.subCategory.subCategory.subCategory.ImportSubCategoryActivity;
import com.jby.stocktake.others.ExpandableHeightListView;
import com.jby.stocktake.others.SquareHeightLinearLayout;
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

public class ImportCategorySearchDialog extends DialogFragment implements ImportCategorySearchCategoryAdapter.CategoryAdapterCallBack,
        TextWatcher, View.OnClickListener, AbsListView.MultiChoiceModeListener, ImportCategoryUpdateDialog.ImportUpdateDialogCallBack,
        AdapterView.OnItemClickListener{
    View rootView;
    private EditText importCategorySearchQuery;
    private ExpandableHeightListView categoryListView, subCategoryListView ;
    private ImportCategorySearchCategoryAdapter importCategorySearchCategoryAdapter;
    private ImportCategorySearchSubCategoryAdapter importCategorySearchSubCategoryAdapter;
    private ArrayList<ImportCategorySearchCategoryObject> importCategorySearchCategoryObjectArrayList;
    private ArrayList<ImportCategorySearchSubCategoryObject> importCategorySearchSubCategoryAdapterArrayList;
    private TextView importCategoryLabelCategory, importCategoryLabelSubCategory, importCategoryResultNotFound;
    private SquareHeightLinearLayout importCategorySearchBackButton;
    private LinearLayout importCategorySearchLayout;
    private String fileID;

    private Handler mHandler = new Handler();
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private ProgressDialog pd;
    //update
    int editPosition;
    String newCategoryName;
    String subCategoryNum;
    String categoryID;
    //for delete purpose
    List<String> list = new ArrayList<String>();
    SparseBooleanArray checked;
    String selectedItem;
    ActionMode actionMode;
    //    dialog
    DialogFragment dialogFragment;
    Bundle bundle;
    FragmentManager fm;
//    intent purpose
    String selectedCategoryID, selectedCategoryName, selectedBarcode;
    Intent intent;
    public ImportCategorySearchDialog() {
        setStyle(DialogFragment.STYLE_NORMAL,R.style.DialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_export_category_search_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void objectInitialize() {
        importCategorySearchQuery = (EditText) rootView.findViewById(R.id.export_search_category_list_view_search_field);
        categoryListView = (ExpandableHeightListView) rootView.findViewById(R.id.export_search_category_list_view);
        subCategoryListView = (ExpandableHeightListView) rootView.findViewById(R.id.export_search_sub_category_list_view);
        importCategoryLabelCategory = (TextView) rootView.findViewById(R.id.export_search_category_label_list_view);
        importCategoryLabelSubCategory = (TextView) rootView.findViewById(R.id.export_search_sub_category_label_list_view);
        importCategorySearchBackButton = (SquareHeightLinearLayout)rootView.findViewById(R.id.export_search_category_list_view_back_button);
        importCategorySearchLayout = (LinearLayout) rootView.findViewById(R.id.export_search_category_list_view_search_layout);
        importCategoryResultNotFound = (TextView) rootView.findViewById(R.id.activity_export_category_search_dialog_label_result_not_found);
        importCategorySearchCategoryObjectArrayList = new ArrayList<>();
        importCategorySearchSubCategoryAdapterArrayList = new ArrayList<>();
        pd = new ProgressDialog(getActivity());
        fm = getActivity().getSupportFragmentManager();
//        createDialogCallBack = (CreateDialogCallBack) getActivity();

    }
    public void objectSetting(){
        importCategorySearchSubCategoryAdapter = new ImportCategorySearchSubCategoryAdapter(getActivity(), importCategorySearchSubCategoryAdapterArrayList);
        importCategorySearchCategoryAdapter = new ImportCategorySearchCategoryAdapter(getActivity(), importCategorySearchCategoryObjectArrayList, this);
        categoryListView.setAdapter(importCategorySearchCategoryAdapter);
        subCategoryListView.setAdapter(importCategorySearchSubCategoryAdapter);

        importCategorySearchBackButton.setOnClickListener(this);
        importCategorySearchQuery.addTextChangedListener(this);

        categoryListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        categoryListView.setMultiChoiceModeListener(this);
        categoryListView.setOnItemClickListener(this);
        categoryListView.setExpanded(true);

        subCategoryListView.setExpanded(true);
        subCategoryListView.setOnItemClickListener(this);

        pd.setMessage("Loading...");

        Bundle mArgs = getArguments();
        if(mArgs != null)
            fileID = mArgs.getString("file_id");

        showKeyBoard();
    }

    @Override
    public void openUpdateDialog(String category_id, String category_name, int position, String subCategory_num) {
        //        for update local purpose
        editPosition = position;
        subCategoryNum = subCategory_num;
        categoryID = category_id;
        dialogFragment = new ImportCategoryUpdateDialog();
        bundle = new Bundle();
        bundle.putString("category_id", category_id);
        bundle.putString("category_name", category_name);
        bundle.putString("dialog", "import");
        dialogFragment.setArguments(bundle);
        dialogFragment.setTargetFragment(ImportCategorySearchDialog.this, 300);
        dialogFragment.show(getFragmentManager(), "fragment_edit_name");
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {

        if(importCategorySearchQuery.getText().toString().trim().length() >= 2) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(importCategorySearchQuery.getText().toString().trim().length() >= 2)
                        searchCategoryItem(importCategorySearchQuery.getText().toString().trim());
                }
            },200);
        }
        else{
            categoryListView.setVisibility(View.GONE);
            subCategoryListView.setVisibility(View.GONE);
            importCategoryLabelSubCategory.setVisibility(View.GONE);
            importCategoryLabelCategory.setVisibility(View.GONE);
            importCategoryResultNotFound.setVisibility(View.GONE);
            importCategorySearchCategoryObjectArrayList.clear();
            importCategorySearchSubCategoryAdapterArrayList.clear();
            importCategorySearchSubCategoryAdapter.notifyDataSetChanged();
            importCategorySearchCategoryAdapter.notifyDataSetChanged();

        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.export_search_category_list_view_back_button:
                closeKeyBoard();
                dismiss();
                break;
        }
    }

    //    get Item
    public void searchCategoryItem(String keyword){
        pd.show();
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(getActivity())));
        apiDataObjectArrayList.add(new ApiDataObject("keyword", keyword));
        apiDataObjectArrayList.add(new ApiDataObject("file_id", fileID));

        mHandler.postDelayed(
                new Runnable() {
            @Override
            public void run() {
                importCategorySearchCategoryObjectArrayList.clear();
                importCategorySearchSubCategoryAdapterArrayList.clear();
                importCategorySearchSubCategoryAdapter.notifyDataSetChanged();
                importCategorySearchCategoryAdapter.notifyDataSetChanged();
                setAsyncTaskManager();
            }
        }, 50);
    }

    public void deleteCategoryItem(String category_id){
        pd.show();
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(getActivity())));
        apiDataObjectArrayList.add(new ApiDataObject("category_id",category_id));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager();
            }
        }, 200);
    }

    public void updateCategoryItem(String category_name, String category_id){
        pd.show();
        newCategoryName = category_name;
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(getActivity())));
        apiDataObjectArrayList.add(new ApiDataObject("new_category_name",category_name));
        apiDataObjectArrayList.add(new ApiDataObject("category_id",category_id));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAsyncTaskManager();
            }
        }, 200);
    }

    public void setAsyncTaskManager(){

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().importCategorySearch,
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

                        JSONArray subCategoryArray = jsonObjectLoginResponse.getJSONArray("subcategory");
                        if(subCategoryArray.length() > 0){
                            for(int i = 0; i < subCategoryArray.length(); i++){
                                importCategorySearchSubCategoryAdapterArrayList.add(new ImportCategorySearchSubCategoryObject(
                                        subCategoryArray.getJSONObject(i).getString("barcode"),
                                        subCategoryArray.getJSONObject(i).getString("quantity"),
                                        subCategoryArray.getJSONObject(i).getString("category_name"),
                                        subCategoryArray.getJSONObject(i).getString("category_id"),
                                        subCategoryArray.getJSONObject(i).getString("date_create")
                                ));
                            }
                            subCategoryListView.setVisibility(View.VISIBLE);
                            importCategorySearchSubCategoryAdapter.notifyDataSetChanged();
                        }

                        JSONArray categoryArray = jsonObjectLoginResponse.getJSONArray("item");
                        if(categoryArray.length() > 0){
                            for(int i = 0; i < categoryArray.length(); i++){
                                importCategorySearchCategoryObjectArrayList.add(new ImportCategorySearchCategoryObject(
                                        categoryArray.getJSONObject(i).getString("id"),
                                        categoryArray.getJSONObject(i).getString("category_name"),
                                        categoryArray.getJSONObject(i).getString("sub_category_num")
                                ));
                            }
                            categoryListView.setVisibility(View.VISIBLE);
                            importCategorySearchCategoryAdapter.notifyDataSetChanged();
                        }
                    }

                    else if (jsonObjectLoginResponse.getString("status").equals("2")) {
                        Toast.makeText(getActivity(), "Failed!", Toast.LENGTH_SHORT).show();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("4")){
                        Toast.makeText(getActivity(), "Something went wrong with server!", Toast.LENGTH_SHORT).show();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("5")){
                        Toast.makeText(getActivity(), "Existed!", Toast.LENGTH_SHORT).show();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("6")){
                        importCategorySearchCategoryObjectArrayList.set(getUpdatePosition(), new ImportCategorySearchCategoryObject(
                                getCategoryID(),
                                getNewCategoryName(),
                                getSubCategoryNum()));
                        importCategorySearchCategoryAdapter.notifyDataSetChanged();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("7")){
                        for(int i=categoryListView.getCount()-1; i >= 0; i--){
                            if(importCategorySearchCategoryAdapter.getSelectedIds().get(i)){
                                importCategorySearchCategoryObjectArrayList.remove(i);
                            }
                        }
                        getActionMode().finish();
                        importCategorySearchCategoryAdapter.notifyDataSetChanged();

                    }
                }
                else {
                    Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                Toast.makeText(getActivity(), "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(getActivity(), "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(getActivity(), "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        if(importCategorySearchSubCategoryAdapterArrayList.size() == 0){
            subCategoryListView.setVisibility(View.GONE);
            importCategoryLabelSubCategory.setVisibility(View.GONE);
//            categoryFragmentResultNotFound.setVisibility(View.VISIBLE);
        }
        else
            importCategoryLabelSubCategory.setVisibility(View.VISIBLE);

        if(importCategorySearchCategoryObjectArrayList.size() == 0){
            categoryListView.setVisibility(View.GONE);
            importCategoryLabelCategory.setVisibility(View.GONE);
//            categoryFragmentResultNotFound.setVisibility(View.VISIBLE);
        }
        else
            importCategoryLabelCategory.setVisibility(View.VISIBLE);

        if(importCategorySearchSubCategoryAdapterArrayList.size() == 0 && importCategorySearchCategoryObjectArrayList.size() == 0){
            importCategoryResultNotFound.setVisibility(View.VISIBLE);
            String notFoundText = getContext().getString(R.string.activity_export_category_search_dialog_label_result_not_found)
                    + " '" + importCategorySearchQuery.getText().toString().trim() + "'";

            importCategoryResultNotFound.setText(notFoundText);
        }
        else{
            importCategoryResultNotFound.setVisibility(View.GONE);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                importCategorySearchSubCategoryAdapter.notifyDataSetChanged();
                importCategorySearchCategoryAdapter.notifyDataSetChanged();
                pd.dismiss();
            }
        },50);

    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
        final int checkedCount = categoryListView.getCheckedItemCount();
        // Set the  CAB title according to total checked items
        actionMode.setTitle(checkedCount + "  Selected");
        // Calls  toggleSelection method from ListViewAdapter Class
        importCategorySearchCategoryAdapter.toggleSelection(position);
        checked = categoryListView.getCheckedItemPositions();
        for(int i = 0 ; i<categoryListView.getCount(); i++){
            if(checked.get(i)){
                list.add(importCategorySearchCategoryObjectArrayList.get(i).getId());
            }
        }
        selectedItem=String.valueOf(list);
        selectedItem = selectedItem.replace("[","");
        selectedItem = selectedItem.replace("]","");
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
                final int checkedCount = importCategorySearchCategoryObjectArrayList.size();

                importCategorySearchCategoryAdapter.removeSelection();
                for (int i = 0; i < checkedCount; i++) {
                    categoryListView.setItemChecked(i, true);
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
        importCategorySearchCategoryAdapter.removeSelection();
    }

    public void alertMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
    public void hideActionbar(boolean hide){
        if(hide)
            new AnimationUtility().slideOut(getActivity(), importCategorySearchLayout);
        else
            new AnimationUtility().minimize(getActivity(), importCategorySearchLayout);
    }
    public String getSelectedItemID(){
        return selectedItem;
    }
    public String getSubCategoryNum(){
        return subCategoryNum;
    }
    public String getCategoryID(){
        return categoryID;
    }
    public String getNewCategoryName(){
        return newCategoryName;
    }
    public int getUpdatePosition(){
        return editPosition;
    }
    public ActionMode getActionMode(){
        return actionMode;
    }
    public void showKeyBoard(){
        final InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }
    public void closeKeyBoard(){
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()){
            case R.id.export_search_category_list_view:
                selectedCategoryID = importCategorySearchCategoryObjectArrayList.get(i).getId();
                selectedCategoryName = importCategorySearchCategoryObjectArrayList.get(i).getCategory();
                bundle = new Bundle();
                intent = new Intent(getActivity(), ImportSubCategoryActivity.class);
                bundle.putString("category_id", selectedCategoryID);
                bundle.putString("category_name", selectedCategoryName);
                bundle.putString("fromListView", "categoryLV");
                intent.putExtras(bundle);
                startActivityForResult(intent, 2);
                closeKeyBoard();
                break;

            case R.id.export_search_sub_category_list_view:
                selectedCategoryID = importCategorySearchSubCategoryAdapterArrayList.get(i).getCategory_Id();
                selectedBarcode = importCategorySearchSubCategoryAdapterArrayList.get(i).getBarcode();
                selectedCategoryName = importCategorySearchSubCategoryAdapterArrayList.get(i).getCategory_Name();
                bundle = new Bundle();
                intent = new Intent(getActivity(), ImportSubCategoryActivity.class);
                bundle.putString("category_id", selectedCategoryID);
                bundle.putString("selected_barcode", selectedBarcode);
                bundle.putString("category_name", selectedCategoryName);
                bundle.putString("fromListView", "subcategoryLV");
                intent.putExtras(bundle);
                startActivityForResult(intent, 2);
                closeKeyBoard();
                break;
        }
    }
}