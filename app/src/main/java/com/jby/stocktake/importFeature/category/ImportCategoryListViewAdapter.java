package com.jby.stocktake.importFeature.category;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.stocktake.R;

import java.util.ArrayList;

public class ImportCategoryListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ImportCategoryListViewObject> importCategoryListViewObjectArrayList;
    private ImportCategoryAdapterCallBack importCategoryAdapterCallBack;
    private SparseBooleanArray mSelectedItemsIds;

    public ImportCategoryListViewAdapter(Context context, ArrayList<ImportCategoryListViewObject> importCategoryListViewObjectArrayList, ImportCategoryAdapterCallBack importCategoryAdapterCallBack)
    {
        this.context = context;
        this.importCategoryListViewObjectArrayList = importCategoryListViewObjectArrayList;
        this.importCategoryAdapterCallBack = importCategoryAdapterCallBack;
        mSelectedItemsIds = new  SparseBooleanArray();
    }

    @Override
    public int getCount() {
        return importCategoryListViewObjectArrayList.size();
    }

    @Override
    public ImportCategoryListViewObject getItem(int i) {
        return importCategoryListViewObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.fragment_category_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        ImportCategoryListViewObject object = getItem(i);
        String quantity = object.getSubCategory_numb() + " Items";
        viewHolder.categoryName.setText(object.getCategory());
        viewHolder.subCategory_numb.setText(quantity);

        viewHolder.categoryEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importCategoryAdapterCallBack.openUpdateDialog(importCategoryListViewObjectArrayList.get(i).getId(),
                        importCategoryListViewObjectArrayList.get(i).getCategory(), i,
                        importCategoryListViewObjectArrayList.get(i).getSubCategory_numb());
            }
        });

            if(mSelectedItemsIds.get(i)){
                viewHolder.categorInnerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.list_view_background));
            }
            else{
                viewHolder.categorInnerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.default_background));
            }


        return view;
    }

    public void remove(int  position) {
        importCategoryListViewObjectArrayList.remove(position);
        notifyDataSetChanged();
    }

//    // get List after update or delete
//    public List<String> getMyList() {
//        return DataList;
//    }

    public void  toggleSelection(int position) {
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

    public interface ImportCategoryAdapterCallBack {
        void openUpdateDialog(String category_id, String category_name, int position, String subCategory_num);
    }


    private static class ViewHolder{
        private TextView categoryName, subCategory_numb, categoryEdit;
        private LinearLayout categorInnerLayout;

        ViewHolder (View view){
            categoryName = (TextView)view.findViewById(R.id.fragment_category_list_view_category);
            subCategory_numb = (TextView)view.findViewById(R.id.fragment_category_list_view_quantity);
            categoryEdit = (TextView)view.findViewById(R.id.fragment_category_list_view_edit);
            categorInnerLayout = (LinearLayout)view.findViewById(R.id.fragment_category_list_view_inner_layout);

        }
    }
}
