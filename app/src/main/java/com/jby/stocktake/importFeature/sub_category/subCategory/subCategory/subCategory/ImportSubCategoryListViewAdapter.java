package com.jby.stocktake.importFeature.sub_category.subCategory.subCategory.subCategory;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.stocktake.R;
import com.jby.stocktake.exportFeature.subcategory.subcategory.SubCategoryObject;

import java.util.ArrayList;

public class ImportSubCategoryListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ImportSubCategoryObject> importSubCategoryObjectArrayList;
    private SparseBooleanArray mSelectedItemsIds;
    private boolean isDeleting = false;

    ImportSubCategoryListViewAdapter(Context context, ArrayList<ImportSubCategoryObject> importSubCategoryObjectArrayList)
    {
        this.context = context;
        this.importSubCategoryObjectArrayList = importSubCategoryObjectArrayList;
        mSelectedItemsIds = new  SparseBooleanArray();
    }

    @Override
    public int getCount() {
        return importSubCategoryObjectArrayList.size();
    }

    @Override
    public ImportSubCategoryObject getItem(int i) {
        return importSubCategoryObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        int number;
        if (view == null){
            view = View.inflate(this.context, R.layout.activity_import_sub_category_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        ImportSubCategoryObject object = getItem(i);
        number = i+1;

        viewHolder.number.setText(String.valueOf(number));
        viewHolder.barcode.setText(object.getBarcode());
        viewHolder.date.setText(object.getDate());
        viewHolder.time.setText(object.getTime());

//        system quantity purpose
        String system_Quantity = object.getSystem_quantity();
        int systemQuantity;
        if(system_Quantity.length() <= 0 || !system_Quantity.matches("^\\d+(\\.\\d+)?") || system_Quantity.contains("."))
        {
            viewHolder.subCategoryInnerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.warning));
            systemQuantity = 0;
        }
        else{
//            if match
            viewHolder.subCategoryInnerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.default_background));
            systemQuantity = Integer.valueOf(system_Quantity);
        }

//        check quantity purpose
        String quantity = object.getQuantity();
        int checkQuantity;
        if(quantity.length() <= 0 || !quantity.matches("^\\d+(\\.\\d+)?"))
            checkQuantity = 0;
        else
            checkQuantity = Integer.valueOf(quantity);

        viewHolder.system_quantity.setText(String.valueOf(systemQuantity));
        viewHolder.quantity.setText(String.valueOf(checkQuantity));


//        checking status purpose
        if(checkQuantity > systemQuantity)
            viewHolder.status.setText(context.getResources().getString(R.string.activity_import_sub_category_status_more));
        else if(checkQuantity < systemQuantity)
            viewHolder.status.setText(context.getResources().getString(R.string.activity_import_sub_category_status_less));
        else
            viewHolder.status.setText(context.getResources().getString(R.string.activity_import_sub_category_status_balance));

        if(isDeleting){
            if(mSelectedItemsIds.get(i))
                viewHolder.subCategoryInnerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.list_view_background));
            else
                viewHolder.subCategoryInnerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.default_background));
        }


        return view;
    }

    public void remove(int  position) {
        importSubCategoryObjectArrayList.remove(position);
        notifyDataSetChanged();
    }

    void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    // Remove selection after unchecked
    void  removeSelection() {
        mSelectedItemsIds = new  SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Item checked on selection
    private void selectView(int position, boolean value) {
        if (value){
            mSelectedItemsIds.put(position,  true);
        }
        else{
            mSelectedItemsIds.delete(position);
        }
        notifyDataSetChanged();
    }
    public boolean isDeleting(){
        return isDeleting;
    }

    void setDeleting(boolean isDeleting){
        this.isDeleting = isDeleting;
    }

    // Get number of selected item
    public int  getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    private static class ViewHolder{
        private TextView number, barcode, quantity, date, time, system_quantity, status;
        private LinearLayout subCategoryInnerLayout;

        ViewHolder (View view){
            number = (TextView)view.findViewById(R.id.fragment_sub_category_list_view_no);
            barcode = (TextView)view.findViewById(R.id.fragment_sub_category_list_view_barcode);
            quantity = (TextView)view.findViewById(R.id.fragment_sub_category_list_view_quantity);
            system_quantity = (TextView)view.findViewById(R.id.fragment_sub_category_list_view_system_quantity);
            date = (TextView)view.findViewById(R.id.fragment_sub_category_list_view_date);
            time = (TextView)view.findViewById(R.id.fragment_sub_category_list_view_time);
            status = (TextView)view.findViewById(R.id.fragment_sub_category_list_view_system_status);
            subCategoryInnerLayout = (LinearLayout)view.findViewById(R.id.fragment_sub_category_list_view_inner_layout);

        }
    }
}
