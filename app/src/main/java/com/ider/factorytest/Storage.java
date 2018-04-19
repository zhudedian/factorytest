package com.ider.factorytest;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import java.io.File;

/**
 * Created by ider-eric on 2016/11/4.
 */

public class Storage {

    public static String getSDCardStorage(Context context) {
        String[] sdCardInfo = new String[2];
        String state = Environment.getExternalStorageState();
        File sdcardDir = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(sdcardDir.getPath());
        long bSize = sf.getBlockSize();
        long bCount = sf.getBlockCount();
        long availBlocks = sf.getAvailableBlocks();

        long total = bSize * bCount;
        long available = bSize * availBlocks;

        sdCardInfo[0] = String.valueOf(formatTotalSd(total));
        sdCardInfo[1] = getSDAvailableSize(context);

        return sdCardInfo[0] + "/" + sdCardInfo[1];

    }

    private static String getSDAvailableSize(Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(context, blockSize * totalBlocks);
    }

    private static String formatTotalSd(long total) {
        int totalM = (int) (total / 1024 / 1024);
        if (totalM < 4096) {
            return "4GB";
        } else if (totalM > 4096 && totalM < 8129) {
            return "8GB";
        } else if (totalM > 8129 && totalM < 16384) {
            return "16GB";
        } else if (totalM > 16384 && totalM < 32768) {
            return "32GB";
        } else {
            return "64GB";
        }
    }

}
