package com.ider.factorytest.util;

import com.ider.factorytest.db.Wifi;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Eric on 2017/9/8.
 */

public class WifiSort implements Comparator<Wifi> {

    private Collator collator = Collator.getInstance(Locale.CHINA);

    public static void sort(List<Wifi> list){
        Collections.sort(list,new WifiSort());
    }

    @Override
    public int compare(Wifi wifi1 , Wifi wifi2){
        int value = collator.compare(wifi1.getLevel(),wifi2.getLevel());
        if (value>0){
            return -1;
        }else if (value<0){
            return 1;
        }else {
            int value2 = collator.compare(wifi1.getWifiName(),wifi2.getWifiName());
            return value2;
        }
    }
}
