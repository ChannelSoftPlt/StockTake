package com.jby.stocktake.exportFeature.subcategory.subcategory;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.R;
import com.jby.stocktake.database.CustomSqliteHelper;

import java.util.ArrayList;

public class SubCategoryListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<SubCategoryObject> subCategoryObjectArrayList;
    private SparseBooleanArray mSelectedItemsIds;
    private CustomSqliteHelper customSqliteHelper;
    private int totalRow = 0;
    private String categoryID;

    public SubCategoryListViewAdapter(Context context, ArrayList<SubCategoryObject> subCategoryObjectArrayList, String categoryID)
    {
        this.context = context;
        this.subCategoryObjectArrayList = subCategoryObjectArrayList;
        mSelectedItemsIds = new  SparseBooleanArray();
        customSqliteHelper = new CustomSqliteHelper(context);
        this.categoryID = categoryID;
    }

    @Override
    public int getCount() {
        return subCategoryObjectArrayList.size();
    }

    @Override
    public SubCategoryObject getItem(int i) {
        return subCategoryObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.fragment_sub_category_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        SubCategoryObject object = getItem(i);
        totalRow = customSqliteHelper.countSubCategoryRow(getCategoryID());
        totalRow = totalRow - i;

        viewHolder.number.setText(String.valueOf(totalRow));
        viewHolder.barcode.setText(object.getBarcode());
        viewHolder.quantity.setText(object.getQuantity());
        viewHolder.date.setText(object.getDate());
        viewHolder.time.setText(object.getTime());


        if(mSelectedItemsIds.get(i)){
            viewHolder.subCategoryInnerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.list_view_background));
        }
        else{
            viewHolder.subCategoryInnerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.default_background));
        }


        return view;
    }
    public String getCategoryID(){
        return categoryID;
    }

    public void remove(int  position) {
        subCategoryObjectArrayList.remove(position);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    // Remove selection after unchecked
    public void  removeSelection() {
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

    // Get number of selected item
    public int  getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    private static class ViewHolder{
        private TextView number, barcode, quantity, date, time;
        private LinearLayout subCategoryInnerLayout;

        ViewHolder (View view){
            number = (TextView)view.findViewById(R.id.fragment_sub_category_list_view_no);
            barcode = (TextView)view.findViewById(R.id.fragment_sub_category_list_view_barcode);
            quantity = (TextView)view.findViewById(R.id.fragment_sub_category_list_view_quantity);
            date = (TextView)view.findViewById(R.id.fragment_sub_category_list_view_date);
            time = (TextView)view.findViewById(R.id.fragment_sub_category_list_view_time);
            subCategoryInnerLayout = (LinearLayout)view.findViewById(R.id.fragment_sub_category_list_view_inner_layout);

        }
    }
}
