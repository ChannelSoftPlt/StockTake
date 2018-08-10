package com.jby.stocktake.exportFeature.subcategory.subcategory.takeAction;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jby.stocktake.R;

import java.util.ArrayList;

public class TakeActionListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<TakeActionObject> takeActionObjectArrayList;
    private int position = -1;

    TakeActionListViewAdapter(Context context, ArrayList<TakeActionObject> takeActionObjectArrayList)
    {
        this.context = context;
        this.takeActionObjectArrayList = takeActionObjectArrayList;
    }

    @Override
    public int getCount() {
        return takeActionObjectArrayList.size();
    }

    @Override
    public TakeActionObject getItem(int i) {
        return takeActionObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.fragment_sub_category_take_action_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        TakeActionObject object = getItem(i);

        String time = "Created at: " + object.getDate() + " " +object.getTime();
        String barcode = "Barcode: " + object.getBarcode();
        String quantity = "Quantity: "+object.getQuantity();
        String category = object.getCategoryName();
        viewHolder.category.setText(category);
        viewHolder.barcode.setText(barcode);
        viewHolder.quantity.setText(quantity);
        viewHolder.time.setText(time);

        if(i == getPosition()){
            viewHolder.parentLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.list_view_background));
        }
        else{
            viewHolder.parentLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.default_background));
        }

        return view;
    }

    void setSelectedViewPosition(int position){
        this.position = position;
    }

    int getPosition(){
        return position;
    }
    private static class ViewHolder{
        private TextView category, barcode, quantity, time;
        private LinearLayout parentLayout;

        ViewHolder (View view){
            category = (TextView)view.findViewById(R.id.fragment_sub_category_take_action_list_view_category);
            barcode = (TextView)view.findViewById(R.id.fragment_sub_category_take_action_list_view_barcode);
            quantity = (TextView)view.findViewById(R.id.fragment_sub_category_take_action_list_view_quantity);
            time = (TextView)view.findViewById(R.id.fragment_sub_category_take_action_list_view_time);
            parentLayout = (LinearLayout)view.findViewById(R.id.fragment_sub_category_take_action_list_view_parent_layout);

        }
    }
}
