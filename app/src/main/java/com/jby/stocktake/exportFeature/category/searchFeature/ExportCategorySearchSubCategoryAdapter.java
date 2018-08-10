package com.jby.stocktake.exportFeature.category.searchFeature;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jby.stocktake.R;

import java.util.ArrayList;

public class ExportCategorySearchSubCategoryAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ExportCategorySearchSubCategoryObject> exportCategorySearchSubCategoryObjectArrayList;

    public ExportCategorySearchSubCategoryAdapter(Context context, ArrayList<ExportCategorySearchSubCategoryObject> exportCategorySearchSubCategoryObjectArrayList)
    {
        this.context = context;
        this.exportCategorySearchSubCategoryObjectArrayList = exportCategorySearchSubCategoryObjectArrayList;
    }

    @Override
    public int getCount() {
        return exportCategorySearchSubCategoryObjectArrayList.size();
    }

    @Override
    public ExportCategorySearchSubCategoryObject getItem(int i) {
        return exportCategorySearchSubCategoryObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.activity_export_search_sub_category_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        ExportCategorySearchSubCategoryObject object = getItem(i);
        viewHolder.categoryName.setText(object.getCategory_Name());
        viewHolder.date.setText(object.getDate());
        viewHolder.barcode.setText(object.getBarcode());
        viewHolder.quantity.setText(object.getQuantity());

        return view;
    }
    private static class ViewHolder{
        private TextView categoryName, date, barcode, quantity;

        ViewHolder (View view){
            categoryName = (TextView)view.findViewById(R.id.activity_export_search_sub_category_list_view_category);
            date = (TextView)view.findViewById(R.id.activity_export_search_sub_category_list_view_date);
            barcode = (TextView)view.findViewById(R.id.activity_export_search_sub_category_list_view_barcode);
            quantity = (TextView)view.findViewById(R.id.activity_export_search_sub_category_list_view_quantity);

        }
    }
}
