package com.ider.factorytest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ider.factorytest.db.BT;

import java.util.List;

/**
 * Created by Eric on 2017/9/8.
 */

public class BtAdapter extends ArrayAdapter<BT> {


    public BtAdapter(Context context, int textViewResourceId, List<BT> objects){
        super(context,textViewResourceId,objects);
    }
    @Override
    public View getView(int posetion, View convertView, ViewGroup parent){
        BT bt = getItem(posetion);
        View view;
        BtAdapter.ViewHolder viewHolder;
        if (convertView==null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.bt_item,parent,false);
            viewHolder =new BtAdapter.ViewHolder();
            viewHolder.name = (TextView)view.findViewById(R.id.name);
            viewHolder.address = (TextView)view.findViewById(R.id.address);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (BtAdapter.ViewHolder) view.getTag();
        }
        viewHolder.name.setText(bt.getBtName());
        viewHolder.address.setText(bt.getAddress());
        return view;
    }

    class ViewHolder{
        TextView name,address;
    }
}
