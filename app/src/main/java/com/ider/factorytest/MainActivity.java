package com.ider.factorytest;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import com.ider.factorytest.db.BT;
import com.ider.factorytest.db.Wifi;
import com.ider.factorytest.util.WifiSort;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    TextView remoteValue;
    TextView ethMacValue;
    TextView ethIpValue;
    TextView wifiMacValue;
    TextView usb1Value;
    TextView usb2Value;
    TextView sdtfValue;

    TextView imageValue;
    TextView deviceModelValue;
    TextView modelValue;
    TextView cpuValue;
    TextView storageValue;
    TextView memoryValue;
    TextView resolutionValue;

    private String manufature= Build.MANUFACTURER.toLowerCase();
    private int sdkVersion= Build.VERSION.SDK_INT;

    LinearLayout usb3,usb4;
    boolean isUsb1Visi = false,isUsb2Visi = false,isUsb3Visi = false,isUsb4Visi= false;

    ListView wifiList,btList;
    private BtAdapter btAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    WifiManager wifiManager;
    WifiAdapter wifiAdapter;
    List<ScanResult> scanResults;
    private List<Wifi> wifis = new ArrayList<>();
    private List<BT> bts = new ArrayList<>();
    VideoView videoView;
    Button refreshWifi,setting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
//        }else {
//
//        }
        wifiManager = (WifiManager) getSystemService(Service.WIFI_SERVICE);

        registReceivers();

        openWifi();
        openBt();
        findViewByIds();
        scanBt();
        setListeners();

        setupValues();



        checkExternalStorage();


//        String s  = getEthMacs();
//        Log.i("mymac", "onCreate: macs===="+s);
//        Log.i("mymac", Build.DEVICE);
//        Log.i("mymac", Build.VERSION.SDK_INT+"");

    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
//        switch (requestCode){
//            case 1:
//                if (grantResults.length>0&& grantResults[0] ==PackageManager.PERMISSION_GRANTED){
//
//                }else {
//                    Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
//                }
//                break;
//
//            default:
//        }
//    }


    private void scanWifi(){
        scanResults = wifiManager.getScanResults();
//                Log.i("scanResults",scanResults.size()+"");
        wifis.clear();
        if (scanResults!=null&&scanResults.size()>0)
        for (ScanResult scanResult:scanResults){
            boolean isLock;
//                    Log.i("scanResults",scanResult.capabilities);
            if (scanResult.capabilities.equals("[ESS]")){
//                        Log.i("scanResults",scanResult.capabilities);
                isLock = false;
            }else {
                isLock = true;
            }
            wifis.add(new Wifi(scanResult.SSID,String.valueOf(WifiManager.calculateSignalLevel(scanResult.level, 100)),isLock));
        }
        WifiSort.sort(wifis);
        wifiAdapter.notifyDataSetChanged();
    }
    private void scanBt(){
        if (mBluetoothAdapter!=null) {
            // 获取所有已经绑定的蓝牙设备
            bts.clear();
            Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
            if (devices.size() > 0) {
                for (BluetoothDevice bluetoothDevice : devices) {
//                    Log.i("device.getName()", bluetoothDevice.getName());
                    bts.add(new BT(bluetoothDevice.getName(), bluetoothDevice.getAddress()));
                }
            }
            btAdapter.notifyDataSetChanged();

            mBluetoothAdapter.startDiscovery();
        }
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            Log.i("device.getName()",action);
            // 获得已经搜索到的蓝牙设备
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                Log.i("device.getName()", device.getName()+device.getAddress());
                if (device.getName()!=null) {
//                    Log.i("device.getName()", device.getName());
//                    bts.add(device.getName());
                }
                // 搜索到的不是已经绑定的蓝牙设备
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 显示在TextView上
                    if (device.getName()!=null) {
//                        Log.i("device.getName()", device.getName());
                        BT bt = new BT(device.getName(),device.getAddress());
                        if (!bts.contains(bt)) {
                            bts.add(bt);
                        }
                    }
                }
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                scanBt();
            }
            btAdapter.notifyDataSetChanged();
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        startVideo();
    }

    public void registReceivers() {
        IntentFilter filter;
        filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectReceiver, filter);

        // 外接u盘广播
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addDataScheme("file");
        registerReceiver(mediaReciever, filter);

        IntentFilter mFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mReceiver, mFilter);
        // 注册搜索完时的receiver
        mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行动扫描模式改变了
        mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        mFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, mFilter);

    }


    public void unRegistReceivers() {
        try {
            unregisterReceiver(connectReceiver);
            unregisterReceiver(mediaReciever);;
        }catch (Exception e) {

        }
    }

    BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.i("scanResults",intent.getAction()+"");
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                scanWifi();
                switch (wifiManager.getWifiState()) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        getWifiMac();
                        scanResults = wifiManager.getScanResults();
//                        Log.i("scanResults",scanResults.size()+"");
                        wifis.clear();
                        for (ScanResult scanResult:scanResults){
                            boolean isLock;
//                            Log.i("scanResults",scanResult.SSID+scanResult.capabilities+scanResult.capabilities.length());
                            if (scanResult.capabilities.equals("[ESS]")){
                                isLock = false;
                            }else {
                                isLock = true;
                            }
                            wifis.add(new Wifi(scanResult.SSID,String.valueOf(WifiManager.calculateSignalLevel(scanResult.level, 100)),isLock));
                        }
                        WifiSort.sort(wifis);
                        wifiAdapter.notifyDataSetChanged();
                        break;
                }
            } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (isEthernetConnected()) {
                    getEthIp();
                }
            }else if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                scanWifi();
            }
        }
    };


    BroadcastReceiver mediaReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            Log.i("tag", action);
//            String data = intent.getDataString();
//            String path = data.substring(7);
//            Log.i("tag", "onReceive: " + path);
//
//            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
//
//                if (path.contains("USB_DISK0") || path.contains("udisk0")) {
//                    setText(usb1Value, "可用", 0xff00ff00);
//                } else if (path.contains("USB_DISK1") || path.contains("udisk1")) {
//                    setText(usb2Value, "可用", 0xff00ff00);
//                } else if (path.contains("sdcard1")) {
//                    setText(sdtfValue, "可用", 0xff00ff00);
//                }
//            } else if (action.equals(Intent.ACTION_MEDIA_REMOVED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
//                if (path.contains("USB_DISK0") || path.contains("udisk0")) {
//                    setText(usb1Value, "不可用", 0xffff0000);
//                } else if (path.contains("USB_DISK1") || path.contains("udisk1")) {
//                    setText(usb2Value, "不可用", 0xffff0000);
//                } else if (path.contains("sdcard1")) {
//                    setText(sdtfValue, "不可用", 0xffff0000);
//                }
//            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkExternalStorage();
                }
            },1000);

        }
    };

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unRegistReceivers();
        unregisterReceiver(mReceiver);
        if (mBluetoothAdapter!=null){
            mBluetoothAdapter.disable();
        }
        if (wifiManager!=null){
            wifiManager.setWifiEnabled(false);
        }
    }


    private void checkExternalStorage() {
        String usb1_path = getExternalPaths()[0];
        String usb2_path = getExternalPaths()[1];
        String usb3_path = getExternalPaths()[3];
        String usb4_path = getExternalPaths()[4];
        String tf_path = getExternalPaths()[2];
        setText(usb1Value, "不可用", 0xffff0000);
        setText(usb2Value, "不可用", 0xffff0000);
        setText(sdtfValue, "不可用", 0xffff0000);
        usb3.setVisibility(View.GONE);
        usb4.setVisibility(View.GONE);
        isUsb1Visi = false;
        isUsb2Visi = false;
        isUsb3Visi = false;
        isUsb3Visi = false;

        if (usb1_path != null) {
            File usb1 = new File(usb1_path);
            if (usb1.exists() && usb1.getTotalSpace() > 0) {
                setText(usb1Value, "可用", 0xff00ff00);
                isUsb1Visi = true;
            }
        }


        if (usb2_path != null) {
            File usb2 = new File(usb2_path);
            if (usb2.exists() && usb2.getTotalSpace() > 0) {
                if (isUsb1Visi){
                    setText(usb2Value, "可用", 0xff00ff00);
                    isUsb2Visi = true;
                }else {
                    setText(usb1Value, "可用", 0xff00ff00);
                    isUsb1Visi =true;
                }

            }
        }
        if (usb3_path != null) {
            File f3 = new File(usb3_path);
            if (f3.exists() && f3.getTotalSpace() > 0) {
                Log.i("tag",usb1Value.getText()+"" );
                if (usb2Value.getText().equals("：可用")){
                    usb3.setVisibility(View.VISIBLE);
                    isUsb3Visi = true;
                }else if (isUsb1Visi){
                    setText(usb2Value, "可用", 0xff00ff00);
                    isUsb2Visi = true;
                }else {
                    setText(usb1Value, "可用", 0xff00ff00);
                    isUsb1Visi =true;
                }

            }
        }
        if (usb4_path != null) {
            File f4 = new File(usb4_path);
            if (f4.exists() && f4.getTotalSpace() > 0) {
                if (isUsb3Visi){
                    usb4.setVisibility(View.VISIBLE);
                    isUsb4Visi = true;
                }else if (isUsb2Visi){
                    usb3.setVisibility(View.VISIBLE);
                    isUsb3Visi = true;
                }else if (isUsb1Visi){
                    setText(usb2Value, "可用", 0xff00ff00);
                    isUsb2Visi = true;
                }else {
                    setText(usb1Value, "可用", 0xff00ff00);
                    isUsb1Visi =true;
                }

            }
        }

        if (tf_path != null) {
            File tf = new File(tf_path);
            if (tf.exists() && tf.getTotalSpace() > 0) {
                setText(sdtfValue, "可用", 0xff00ff00);
            }
        }

    }

    private String[] getExternalPaths() {
        Log.i("tag", "SDK version = " + Build.VERSION.SDK_INT+getCpuName());
        if (Build.VERSION.SDK_INT < 23) {
            String[] path = new String[5];

            if (Build.DEVICE.contains("rk")) {
                path[0] = "/mnt/usb_storage/USB_DISK0";
                path[1] = "/mnt/usb_storage/USB_DISK1";
                path[2] = "/mnt/external_sd";
                path[3] = "/mnt/usb_storage/USB_DISK2";
                path[4] = "/mnt/usb_storage/USB_DISK3";
            } else if (getCpuName().contains("Amlogic")) {
                if (Build.DEVICE.equals("p201")&&Build.VERSION.SDK_INT==22){
                    path[0] = "/storage/udisk0";
                    path[1] = "/storage/udisk1";
                    path[2] = "/storage/sdcard1";
                    return path;
                }
                String usbP = "/storage/external_storage";
                File file = new File(usbP);
                if(file!=null&&file.listFiles().length>0){
                   switch (file.listFiles().length){
                       case 1:
                           if(!file.listFiles()[0].getName().contains("sdcard")){
                               path[0] = usbP + "/" +file.listFiles()[0].getName();

                           }else{
                               path[0] = null;
                           }
                           path[1] = null;
                           break;
                       case 2:
                           if(!file.listFiles()[0].getName().contains("sdcard")) {
                               path[0] = usbP + "/" + file.listFiles()[0].getName();
                               if(!file.listFiles()[1].getName().contains("sdcard")){
                                   path[1] = usbP + "/" +file.listFiles()[1].getName();
                               }else{
                                   path[1] = null;
                               }
                           }else {
                               path[0] = usbP + "/" + file.listFiles()[1].getName();
                               path[1] = null;
                           }


                           break;
                       case 3:
                           path[0] = usbP + "/" +file.listFiles()[0].getName();
                           path[1] = usbP + "/" +file.listFiles()[1].getName();
                           break;
                   }

                }
                path[2] = "/storage/external_storage/sdcard1";
            } else if(getCpuName().equals("sun8i")) {
                path[0] = "/mnt/usbhost/Storage01";
                path[1] = "/mnt/usbhost/Storage02";
                if (Build.VERSION.SDK_INT==19){
                    path[2] = "/mnt/extsd";
                }else {
                    path[2] = null;
                }
            }
            return path;
        } else {
            return getNUSBPath();
        }
    }


    private String[] getNUSBPath() {
        String[] paths = new String[5];
        StorageManager storageManager = (StorageManager) getSystemService(Service.STORAGE_SERVICE);
        try {
            Class SM = Class.forName("android.os.storage.StorageManager");
            Class VI = Class.forName("android.os.storage.VolumeInfo");
            Class DI = Class.forName("android.os.storage.DiskInfo");
            Method getVolumes = SM.getDeclaredMethod("getVolumes");
            Method getPath = VI.getDeclaredMethod("getPath");
            Method getDiskInfo = VI.getDeclaredMethod("getDisk");
            Method isMountedReadable = VI.getDeclaredMethod("isMountedReadable");
            Method getType = VI.getDeclaredMethod("getType");

            Method isUsb = DI.getDeclaredMethod("isUsb");
            Method isSd = DI.getDeclaredMethod("isSd");
            List volumeInfos = (List) getVolumes.invoke(storageManager);
            for(int i = 0; i < volumeInfos.size(); i++) {
                Object volume = volumeInfos.get(i);
                if(volume != null && (boolean)isMountedReadable.invoke(volume) && (Integer) getType.invoke(volume) == 0) {
                    File path = (File) getPath.invoke(volumeInfos.get(i));
                    Log.i("tag", "path = " + path.getAbsolutePath());
                    Object diskInfo = getDiskInfo.invoke(volumeInfos.get(i));
                    boolean isUSB = (boolean) isUsb.invoke(diskInfo);
                    boolean isSD = (boolean) isSd.invoke(diskInfo);
                    if(isUSB) {
                        if(paths[0] == null) {
                            paths[0] = path.getAbsolutePath();
                        } else {
                            paths[1] = path.getAbsolutePath();
                        }
                    }
                    if(isSD) {
                        paths[2] = path.getAbsolutePath();
                    }
                }

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return paths;
    }



    private boolean isEthernetConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Service.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        return info.isConnected() && info.isAvailable();
    }


    public void openWifi() {
        wifiManager.setWifiEnabled(true);
        wifiManager.startScan();
    }
    public void openBt(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter!=null) {
            mBluetoothAdapter.enable();
        }
    }


    public void findViewByIds() {
        remoteValue = (TextView) findViewById(R.id.remote_value);
        ethMacValue = (TextView) findViewById(R.id.eth_mac_value);
        ethIpValue = (TextView) findViewById(R.id.eth_ip_value);
        wifiMacValue = (TextView) findViewById(R.id.wifi_mac_value);
        usb1Value = (TextView) findViewById(R.id.usb1_value);
        usb2Value = (TextView) findViewById(R.id.usb2_value);
        sdtfValue = (TextView) findViewById(R.id.sdtf_value);

        imageValue = (TextView) findViewById(R.id.image_Version_value);
        deviceModelValue = (TextView) findViewById(R.id.device_model_value);
        modelValue = (TextView) findViewById(R.id.model_value);
        cpuValue = (TextView) findViewById(R.id.cpu_value);
        storageValue = (TextView) findViewById(R.id.storage_value);
        memoryValue = (TextView) findViewById(R.id.memory_value);
        resolutionValue = (TextView) findViewById(R.id.resolution_value);

        usb3 = (LinearLayout) findViewById(R.id.usb3);
        usb4 = (LinearLayout) findViewById(R.id.usb4);

        wifiList = (ListView) findViewById(R.id.wifi_list);
        btList = (ListView)findViewById(R.id.bt_list);
        videoView = (VideoView) findViewById(R.id.videoview);
        refreshWifi = (Button) findViewById(R.id.refresh_wifi);
        setting = (Button) findViewById(R.id.setting);

        btAdapter = new BtAdapter(MainActivity.this,R.layout.bt_item,bts);
        btList.setAdapter(btAdapter);
        wifiAdapter = new WifiAdapter(MainActivity.this, wifis);
        wifiList.setAdapter(wifiAdapter);
    }

    public void setListeners() {
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                startVideo();
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                refreshWifi.requestFocus();
            }
        });

        refreshWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBt();
                scanWifi();
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                //Log.i("tag","manufature:"+manufature);
                //Log.i("tag",sdkVersion+"");
                if(manufature.equals("amlogic")||getCpuName().contains("Amlogic")) {
                    //Log.i("tag",sdkVersion+"");
                    //if (sdkVersion>=25){
                    //   intent.setComponent(new ComponentName("com.droidlogic.tv.settings", "com.droidlogic.tv.settings.display.DisplayActivity"));
                    //}else

                    intent.setComponent(new ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings"));

                } else if(manufature.equals("rockchip")) {
                    intent.setComponent(new ComponentName("com.rk_itvui.settings", "com.rk_itvui.settings.Settings"));
                }
                try{
                    startActivity(intent);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            });
    }

    public void setupValues() {
        getDeviceInfo();
        getEthMac();
        getCurrentDisplayMode();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        setText(remoteValue, KeyEvent.keyCodeToString(keyCode));
        return super.onKeyDown(keyCode, event);
    }


    public void setText(TextView textView, String value) {
        textView.setText(String.format(getResources().getString(R.string.value), value));
    }

    public void setText(TextView textView, String value, int color) {
        textView.setText(String.format("：%s", value));
        textView.setTextColor(color);
    }
    public String getEthMacs() {
        File file = new File("/sys/class/net/eth0/address");
        String macs = null;
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String mac = br.readLine();
            macs = String.format("：%s", mac);
            //setText(ethMacValue, mac, 0xff00ff00);
        } catch (FileNotFoundException e) {
            macs =  "11";
            e.printStackTrace();
        } catch (IOException e) {
            macs =  "22";
            e.printStackTrace();
        }
        return macs;
    }

    public void getEthMac() {
        File file = new File("/sys/class/net/eth0/address");
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String mac = br.readLine();
            setText(ethMacValue, mac, 0xff00ff00);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void getEthIp() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface networkInterface = en.nextElement();
                List<InterfaceAddress> list = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress IAddr : list) {
                    InetAddress address = IAddr.getAddress();
                    if (!address.isLoopbackAddress()) {
                        String ip = address.getHostAddress();
                        setText(ethIpValue, ip, 0xff00ff00);

                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void getWifiMac() {
        setText(wifiMacValue, wifiManager.getConnectionInfo().getMacAddress(), 0xff00ff00);
    }

    public void getDeviceInfo() {
        setText(cpuValue, getCpuName());
        setText(storageValue, Storage.getSDCardStorage(this));
        setText(memoryValue, Memory.getTotalMemorySize(this));
        setText(deviceModelValue, Build.DEVICE);
        setText(modelValue, Build.MODEL);

        setText(imageValue, Build.DISPLAY);
        Log.i("tag", String.valueOf(Memory.getTotalMemorySize(this)));

    }


    public String getCpuName() {
        File file = new File("/proc/cpuinfo");
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Hardware")) {
                    String cpu = line.split(":")[1];
                    return cpu.trim();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "unknown";
    }


    public void getCurrentDisplayMode() {
        if(!getCpuName().equals("sun8i")) {
            String path = getHDMIFile();
            Log.i("getCurrentDisplayMode", "getCurrentDisplayMode: patch = "+path);
            if (path == null) {
                return;
            }
            File file = new File(path);
            try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String mode = br.readLine();
                setText(resolutionValue, mode);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            setText(resolutionValue, "HDMI 720P 50Hz");
        }

    }


    private String getHDMIFile() {
        if (Build.DEVICE.contains("rk")) {
            return "/sys/class/display/HDMI/mode";
        } else if (getCpuName().contains("Amlogic")) {
            return "/sys/class/display/mode";
        } else if(getCpuName().equals("sun8i")) {
            return null;
        }
        return null;
    }

    public void startVideo() {
        String uri = "android.resource://" + getPackageName() + "/" + R.raw.testvideo;
        Uri URI = Uri.parse(uri);
        videoView.setVideoURI(URI);
        videoView.start();
    }

}
