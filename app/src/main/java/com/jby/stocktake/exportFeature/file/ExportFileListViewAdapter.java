package com.jby.stocktake.exportFeature.file;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.stocktake.R;
import com.jby.stocktake.exportFeature.category.ExportCategoryListViewObject;
import com.jby.stocktake.others.SquareHeightLinearLayout;

import java.util.ArrayList;

public class ExportFileListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ExportFileListViewObject> exportFileListViewObjectArrayList;
    private CategoryAdapterCallBack categoryAdapterCallBack;

    ExportFileListViewAdapter(Context context, ArrayList<ExportFileListViewObject> exportFileListViewObjectArrayList, CategoryAdapterCallBack categoryAdapterCallBack)
    {
        this.context = context;
        this.exportFileListViewObjectArrayList = exportFileListViewObjectArrayList;
        this.categoryAdapterCallBack = categoryAdapterCallBack;
    }

    @Override
    public int getCount() {
        return exportFileListViewObjectArrayList.size();
    }

    @Override
    public ExportFileListViewObject getItem(int i) {
        return exportFileListViewObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.activity_file_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final ExportFileListViewObject object = getItem(i);
        String quantity = object.getCategory_numb() + " Items";
        viewHolder.fileName.setText(object.getFile());
        viewHolder.numCategory.setText(quantity);

        viewHolder.syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryAdapterCallBack.downloadRequestDialog(object.getId(), object.getFile());
            }
        });
        return view;
    }

    public interface CategoryAdapterCallBack {
        void downloadRequestDialog(String id, String fileName);
    }


    private static class ViewHolder{
        private TextView fileName, numCategory ;
        private SquareHeightLinearLayout syncButton, exportButton;

        ViewHolder (View view){
            fileName = (TextView)view.findViewById(R.id.activity_file_list_view_item_file_name);
            numCategory = (TextView)view.findViewById(R.id.activity_file_list_view_item_num_category);

            syncButton = view.findViewById(R.id.activity_file_list_view_item_sync);
            exportButton = view.findViewById(R.id.activity_file_list_view_item_export);
        }
    }
}
