package com.ider.factorytest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ider.factorytest.db.Wifi;

import java.util.List;

/**
 * Created by ider-eric on 2016/11/5.
 */

public class WifiAdapter extends BaseAdapter {

    List<Wifi> list;
    Context context;

    public WifiAdapter(Context context, List<Wifi> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.wifi_item, parent, false);
            holder.textName = (TextView) convertView.findViewById(R.id.ssid);
            holder.textSignal = (TextView) convertView.findViewById(R.id.signal);
            holder.signal = (ImageView) convertView.findViewById(R.id.wifi_signal);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Wifi wifi = list.get(position);
        holder.textName.setText(wifi.getWifiName());
        holder.textSignal.setText(wifi.getLevel());
        int level = Integer.parseInt(wifi.getLevel());
        if (wifi.isLock()){
            if (level>90){
                holder.signal.setImageResource(R.mipmap.ic_wifi_signal_lock_4_white);
            }else if (level>80){
                holder.signal.setImageResource(R.mipmap.ic_wifi_signal_lock_3_white);
            }else if (level>70){
                holder.signal.setImageResource(R.mipmap.ic_wifi_signal_lock_2_white);
            }else if (level>60){
                holder.signal.setImageResource(R.mipmap.ic_wifi_signal_lock_1_white);
            }else{
                holder.signal.setImageResource(R.mipmap.ic_wifi_signal_0_white);
            }
        }else {
            if (level>90){
                holder.signal.setImageResource(R.mipmap.ic_wifi_signal_4_white);
            }else if (level>80){
                holder.signal.setImageResource(R.mipmap.ic_wifi_signal_3_white);
            }else if (level>70){
                holder.signal.setImageResource(R.mipmap.ic_wifi_signal_2_white);
            }else if (level>60){
                holder.signal.setImageResource(R.mipmap.ic_wifi_signal_1_white);
            }else{
                holder.signal.setImageResource(R.mipmap.ic_wifi_signal_0_white);
            }
        }
        return convertView;
    }

    class ViewHolder {
        TextView textName;
        TextView textSignal;
        ImageView signal;
    }

}
