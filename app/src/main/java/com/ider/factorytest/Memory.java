package com.ider.factorytest;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by ider-eric on 2016/11/4.
 */

public class Memory {

    /**
     * 获取系统总内存
     *
     * @param context 可传入应用程序上下文。
     * @return 总内存大单位为B。
     */
    public static String getTotalMemorySize(Context context) {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine().replace(" ", "");
//            Log.i("tag", "memoryLine = " + memoryLine);
            String subMemoryLine = memoryLine.split(":")[1];
            br.close();
            String kb = subMemoryLine.replaceAll("[a-zA-Z]", "");
//            Log.i("tag", "kb = " + kb);
            return kb2mb(kb);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "不可用";
    }

    private static String kb2mb(String kb) {
        Long kbl = Long.valueOf(kb);
        int mb = (int) (kbl / 1024);
        return String.valueOf(mb) + "M";
    }

}
