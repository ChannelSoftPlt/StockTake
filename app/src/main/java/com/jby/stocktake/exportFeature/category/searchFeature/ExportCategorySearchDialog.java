package com.jby.stocktake.exportFeature.category.searchFeature;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.jby.stocktake.database.CustomSqliteHelper;
import com.jby.stocktake.exportFeature.category.ExportCategoryUpdateDialog;
import com.jby.stocktake.exportFeature.subcategory.SubCategoryActivity;
import com.jby.stocktake.others.ExpandableHeightListView;
import com.jby.stocktake.others.SquareHeightLinearLayout;
import com.jby.stocktake.shareObject.AnimationUtility;
import java.util.ArrayList;
import java.util.List;

public class ExportCategorySearchDialog extends DialogFragment implements ExportCategorySearchCategoryAdapter.CategoryAdapterCallBack,
        TextWatcher, View.OnClickListener, AbsListView.MultiChoiceModeListener, ExportCategoryUpdateDialog.UpdateDialogCallBack,
        AdapterView.OnItemClickListener{
    View rootView;
    private EditText exportCategorySearchQuery;
    private ExpandableHeightListView categoryListView, subCategoryListView ;
    private ExportCategorySearchCategoryAdapter exportCategorySearchCategoryAdapter;
    private ExportCategorySearchSubCategoryAdapter exportCategorySearchSubCategoryAdapter;
    private ArrayList<ExportCategorySearchCategoryObject> exportCategorySearchCategoryObjectArrayList;
    private ArrayList<ExportCategorySearchSubCategoryObject> exportCategorySearchSubCategoryAdapterArrayList;
    private TextView exportCategoryLabelCategory, exportCategoryLabelSubCategory, exportCategoryResultNotFound;
    private SquareHeightLinearLayout exportCategorySearchBackButton;
    private LinearLayout exportCategorySearchLayout;
    private String fileID;

    private Handler mHandler = new Handler();
    private ProgressDialog pd;
    private CustomSqliteHelper customSqliteHelper;
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
    public ExportCategorySearchDialog() {
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
        exportCategorySearchQuery = (EditText) rootView.findViewById(R.id.export_search_category_list_view_search_field);
        categoryListView = (ExpandableHeightListView) rootView.findViewById(R.id.export_search_category_list_view);
        subCategoryListView = (ExpandableHeightListView) rootView.findViewById(R.id.export_search_sub_category_list_view);
        exportCategoryLabelCategory = (TextView) rootView.findViewById(R.id.export_search_category_label_list_view);
        exportCategoryLabelSubCategory = (TextView) rootView.findViewById(R.id.export_search_sub_category_label_list_view);
        exportCategorySearchBackButton = (SquareHeightLinearLayout)rootView.findViewById(R.id.export_search_category_list_view_back_button);
        exportCategorySearchLayout = (LinearLayout) rootView.findViewById(R.id.export_search_category_list_view_search_layout);
        exportCategoryResultNotFound = (TextView) rootView.findViewById(R.id.activity_export_category_search_dialog_label_result_not_found);
        exportCategorySearchCategoryObjectArrayList = new ArrayList<>();
        exportCategorySearchSubCategoryAdapterArrayList = new ArrayList<>();
        pd = new ProgressDialog(getActivity());
        fm = getActivity().getSupportFragmentManager();
        customSqliteHelper = new CustomSqliteHelper(getActivity());
    }
    public void objectSetting(){
        exportCategorySearchQuery.addTextChangedListener(this);
        exportCategorySearchBackButton.setOnClickListener(this);

        exportCategorySearchSubCategoryAdapter = new ExportCategorySearchSubCategoryAdapter(getActivity(), exportCategorySearchSubCategoryAdapterArrayList);
        subCategoryListView.setAdapter(exportCategorySearchSubCategoryAdapter);
        subCategoryListView.setExpanded(true);

        categoryListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        categoryListView.setMultiChoiceModeListener(this);
        categoryListView.setOnItemClickListener(this);
        exportCategorySearchCategoryAdapter = new ExportCategorySearchCategoryAdapter(getActivity(), exportCategorySearchCategoryObjectArrayList, this);
        categoryListView.setAdapter(exportCategorySearchCategoryAdapter);
        categoryListView.setExpanded(true);

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
        dialogFragment = new ExportCategoryUpdateDialog();
        bundle = new Bundle();
        bundle.putString("category_id", category_id);
        bundle.putString("category_name", category_name);
        bundle.putString("dialog", "export");
        dialogFragment.setArguments(bundle);
        dialogFragment.setTargetFragment(ExportCategorySearchDialog.this, 300);
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
        resetListView();
        if(exportCategorySearchQuery.getText().toString().trim().length() >= 2) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(exportCategorySearchQuery.getText().toString().trim().length() >= 2)
                        new SearchData().execute(exportCategorySearchQuery.getText().toString().trim(), null, null);
                }
            },200);
        }
        else{
                categoryListView.setVisibility(View.GONE);
                subCategoryListView.setVisibility(View.GONE);
                exportCategoryLabelSubCategory.setVisibility(View.GONE);
                exportCategoryLabelCategory.setVisibility(View.GONE);
                exportCategoryResultNotFound.setVisibility(View.GONE);
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

    public void resetListView(){
        exportCategorySearchCategoryObjectArrayList.clear();
        exportCategorySearchSubCategoryAdapterArrayList.clear();
    }

    //    get Item
    public void searchCategoryItem(final String keyword){

                resetListView();
                exportCategorySearchCategoryObjectArrayList = customSqliteHelper.searchAllCategoryByQuery(fileID, exportCategorySearchCategoryObjectArrayList, keyword);
                exportCategorySearchSubCategoryAdapterArrayList = customSqliteHelper.searchAllSubCategoryFromCategory(fileID, exportCategorySearchSubCategoryAdapterArrayList, keyword);

    }

    public void deleteCategoryItem(){
        boolean deleteCategory = customSqliteHelper.deleteCategory(list);
        if(deleteCategory){
            for(int i=categoryListView.getCount()-1; i >= 0; i--){
                if(exportCategorySearchCategoryAdapter.getSelectedIds().get(i)){
                    exportCategorySearchCategoryObjectArrayList.remove(i);
                }
            }
            getActionMode().finish();
            exportCategorySearchCategoryAdapter.notifyDataSetChanged();
        }
        else
            Toast.makeText(getActivity(), "Failed to delete this file", Toast.LENGTH_SHORT).show();
        getActionMode().finish();
    }

    public void updateCategoryItem(String category_name, String category_id){
        int updateCategory = customSqliteHelper.updateCategory(category_name, category_id);
        if(updateCategory == 1)
        {
            exportCategorySearchCategoryObjectArrayList.set(getUpdatePosition(), new ExportCategorySearchCategoryObject(
                    getCategoryID(),
                    category_name,
                    getSubCategoryNum()));
            exportCategorySearchCategoryAdapter.notifyDataSetChanged();
        }
        else if(updateCategory == 2)
            alertMessageCategoryExisted();
        else
            Toast.makeText(getActivity(), "Failed to update this category", Toast.LENGTH_SHORT).show();
    }

    public void alertMessageCategoryExisted(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
        final int checkedCount = categoryListView.getCheckedItemCount();
        // Set the  CAB title according to total checked items
        actionMode.setTitle(checkedCount + "  Selected");
        // Calls  toggleSelection method from ListViewAdapter Class
        exportCategorySearchCategoryAdapter.toggleSelection(position);
        checked = categoryListView.getCheckedItemPositions();
        for(int i = 0 ; i<categoryListView.getCount(); i++){
            if(checked.get(i)){
                list.add(exportCategorySearchCategoryObjectArrayList.get(i).getId());
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
                final int checkedCount = exportCategorySearchCategoryObjectArrayList.size();

                exportCategorySearchCategoryAdapter.removeSelection();
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
        exportCategorySearchCategoryAdapter.removeSelection();
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
    public void hideActionbar(boolean hide){
        if(hide)
            new AnimationUtility().slideOut(getActivity(), exportCategorySearchLayout);
        else
            new AnimationUtility().minimize(getActivity(), exportCategorySearchLayout);
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
                selectedCategoryID = exportCategorySearchCategoryObjectArrayList.get(i).getId();
                selectedCategoryName = exportCategorySearchCategoryObjectArrayList.get(i).getCategory();
                bundle = new Bundle();
                intent = new Intent(getActivity(), SubCategoryActivity.class);
                bundle.putString("category_id", selectedCategoryID);
                bundle.putString("category_name", selectedCategoryName);
                bundle.putString("fromListView", "categoryLV");
                intent.putExtras(bundle);
                startActivityForResult(intent, 2);
                closeKeyBoard();
                break;

            case R.id.export_search_sub_category_list_view:
                selectedCategoryID = exportCategorySearchSubCategoryAdapterArrayList.get(i).getCategory_Id();
                selectedBarcode = exportCategorySearchSubCategoryAdapterArrayList.get(i).getBarcode();
                selectedCategoryName = exportCategorySearchSubCategoryAdapterArrayList.get(i).getCategory_Name();
                bundle = new Bundle();
                intent = new Intent(getActivity(), SubCategoryActivity.class);
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
    private class SearchData extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            searchCategoryItem(params[0]);
            return null;
        }
        protected void onPostExecute(String result) {
            exportCategorySearchCategoryAdapter.notifyDataSetChanged();
            exportCategorySearchSubCategoryAdapter.notifyDataSetChanged();
            setUpListView();
        }
    }

    public void setUpListView(){
        if(exportCategorySearchCategoryObjectArrayList.size() > 0){
            categoryListView.setVisibility(View.VISIBLE);
            exportCategoryLabelCategory.setVisibility(View.VISIBLE);
        }
        else{
            categoryListView.setVisibility(View.GONE);
            exportCategoryLabelCategory.setVisibility(View.GONE);
        }

        if(exportCategorySearchSubCategoryAdapterArrayList.size() > 0){
            subCategoryListView.setVisibility(View.VISIBLE);
            exportCategoryLabelSubCategory.setVisibility(View.VISIBLE);
        }
        else{
            subCategoryListView.setVisibility(View.GONE);
            exportCategoryLabelSubCategory.setVisibility(View.GONE);
        }

        if(exportCategorySearchCategoryObjectArrayList.size() < 1 && exportCategorySearchSubCategoryAdapterArrayList.size() < 1)
            exportCategoryResultNotFound.setVisibility(View.VISIBLE);

    }
}