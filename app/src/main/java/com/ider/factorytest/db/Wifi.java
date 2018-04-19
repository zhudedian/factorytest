package com.ider.factorytest.db;

/**
 * Created by Eric on 2017/9/8.
 */

public class Wifi {

    private String wifiName;

    private String level;

    private boolean lock;

    public Wifi(String name,String level,boolean lock){
        this.wifiName = name;
        this.level = level;
        this.lock = lock;
    }

    public void setWifiName(String name){
        this.wifiName = name;
    }

    public String getWifiName(){
        return this.wifiName;
    }

    public void setLevel(String level){
        this.level = level;
    }

    public String getLevel(){
        return this.level;
    }

    public void setLock(boolean lock){
        this.lock = lock;
    }
    public boolean isLock(){
        return this.lock;
    }

    @Override
    public boolean equals(Object object){
        if (object instanceof Wifi){
            Wifi wifi= (Wifi) object;
            if (wifi.wifiName.equals(this.wifiName)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    @Override
    public int hashCode(){
        return 2;
    }
}
