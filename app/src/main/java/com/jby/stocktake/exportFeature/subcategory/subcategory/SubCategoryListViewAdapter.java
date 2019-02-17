package com.jby.stocktake.exportFeature.subcategory.subcategory;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.stocktake.R;
import com.jby.stocktake.database.CustomSqliteHelper;

import java.util.ArrayList;

import static android.graphics.Color.YELLOW;

public class SubCategoryListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<SubCategoryObject> subCategoryObjectArrayList;
    public SparseBooleanArray deleteItem, moveItem;
    private CustomSqliteHelper customSqliteHelper;
    private SubCategoryListViewAdapterCallBack subCategoryListViewAdapterCallBack;
    private String categoryID;
    private boolean move = false;

    public SubCategoryListViewAdapter(Context context, ArrayList<SubCategoryObject> subCategoryObjectArrayList, String categoryID, SubCategoryListViewAdapterCallBack subCategoryListViewAdapterCallBack) {
        this.context = context;
        this.subCategoryObjectArrayList = subCategoryObjectArrayList;
        customSqliteHelper = new CustomSqliteHelper(context);
        this.categoryID = categoryID;
        this.subCategoryListViewAdapterCallBack = subCategoryListViewAdapterCallBack;

        deleteItem = new SparseBooleanArray();
        moveItem = new SparseBooleanArray();
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
        if (view == null) {
            view = View.inflate(this.context, R.layout.fragment_sub_category_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        SubCategoryObject object = getItem(i);
        int totalRow = customSqliteHelper.countSubCategoryRow(getCategoryID());
        totalRow = totalRow - i;

        viewHolder.number.setText(String.valueOf(totalRow));
        viewHolder.barcode.setText(object.getBarcode());
        viewHolder.systemQuantity.setText(object.getSystemQuantity());
        viewHolder.checkQuantity.setText(object.getCheckQuantity());
        viewHolder.date.setText(object.getDate());
        viewHolder.time.setText(object.getTime());
        viewHolder.status.setText(getStatus(Double.valueOf(object.getCheckQuantity()), Double.valueOf(object.getSystemQuantity())));
        ;

        if (deleteItem.size() > 0) {
            if (deleteItem.get(i)) {
                viewHolder.subCategoryInnerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.list_view_background));
            } else {
                viewHolder.subCategoryInnerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.default_background));
            }
        } else {
            if (Double.valueOf(object.getCheckQuantity()) > 0)
                viewHolder.subCategoryInnerLayout.setBackgroundColor(YELLOW);
            else
                viewHolder.subCategoryInnerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.default_background));
        }

        if (move) {
            hide(viewHolder, true);
            setCheckMoveItem(viewHolder, i);
        } else hide(viewHolder, false);

        return view;
    }

    private String getCategoryID() {
        return categoryID;
    }

    /*-------------------------------------------------------------------delete purpose-------------------------------------------------------------------*/
    public void remove(int position) {
        subCategoryObjectArrayList.remove(position);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !deleteItem.get(position));
    }

    // Remove selection after unchecked
    public void removeSelection() {
        deleteItem = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Item checked on selection
    private void selectView(int position, boolean value) {
        if (value) {
            deleteItem.put(position, true);
        } else {
            deleteItem.delete(position);
        }
        notifyDataSetChanged();
    }

    // Get number of selected item
    public int getSelectedCount() {
        return deleteItem.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return deleteItem;
    }

    /*--------------------------------------------------------------move item purpose-----------------------------------------------------------------------*/
    public void showCheckBox() {
        move = !move;
        notifyDataSetChanged();
    }

    public void toggleMoveSelection(int position) {
        selectMoveView(position, !moveItem.get(position));
    }

    // Item checked on selection
    public void selectMoveView(int position, boolean value) {
        if (value) {
            moveItem.put(position, true);
        } else {
            moveItem.delete(position);
        }
        notifyDataSetChanged();
    }

    // Get number of selected item
    public int getMoveItemCount() {
        return moveItem.size();
    }

    // Remove selection after unchecked
    public void removeMoveSelection() {
        moveItem = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    private void hide(ViewHolder viewHolder, boolean showCheckBox) {
        viewHolder.checkBox.setVisibility(showCheckBox ? View.VISIBLE : View.GONE);
    }

    private void setCheckMoveItem(final ViewHolder viewHolder, final int position) {
        viewHolder.subCategoryInnerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMoveSelection(position);
                subCategoryListViewAdapterCallBack.selectMoveItem();
            }
        });
        //check item
        viewHolder.checkBox.setChecked(moveItem.get(position));
    }

    /*-----------------------------------------------------------check quantity status-----------------------------------------------------------------------*/
    private String getStatus(double checkQuantity, double systemQuantity) {
        if (checkQuantity > systemQuantity)
            return context.getResources().getString(R.string.activity_import_sub_category_status_more);
        else if (checkQuantity < systemQuantity)
            return context.getResources().getString(R.string.activity_import_sub_category_status_less);
        else
            return context.getResources().getString(R.string.activity_import_sub_category_status_balance);
    }

    public interface SubCategoryListViewAdapterCallBack {
        void selectMoveItem();
    }

    private static class ViewHolder {
        private TextView number, barcode, systemQuantity, checkQuantity, date, time, status;
        private LinearLayout subCategoryInnerLayout;
        private CheckBox checkBox;

        ViewHolder(View view) {
            number = (TextView) view.findViewById(R.id.sub_category_list_view_no);
            barcode = (TextView) view.findViewById(R.id.sub_category_list_view_barcode);

            systemQuantity = (TextView) view.findViewById(R.id.sub_category_list_view_system_quantity);
            checkQuantity = (TextView) view.findViewById(R.id.sub_category_list_view_check_quantity);
            status = view.findViewById(R.id.sub_category_list_view_system_status);

            date = (TextView) view.findViewById(R.id.sub_category_list_view_date);
            time = (TextView) view.findViewById(R.id.sub_category_list_view_time);

            subCategoryInnerLayout = (LinearLayout) view.findViewById(R.id.sub_category_list_view_inner_layout);

            checkBox = view.findViewById(R.id.sub_category_list_view_check_box);
        }
    }
}
