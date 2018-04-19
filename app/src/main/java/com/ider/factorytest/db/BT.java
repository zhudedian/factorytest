package com.ider.factorytest.db;

/**
 * Created by Eric on 2017/9/8.
 */

public class BT {
    private String btName;

    private String address;


    public BT(String name,String address){
        this.btName = name;
        this.address = address;

    }

    public void setBtName(String name){
        this.btName = name;
    }

    public String getBtName(){
        return this.btName;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public String getAddress(){
        return this.address;
    }



    @Override
    public boolean equals(Object object){
        if (object instanceof BT){
            BT bt= (BT) object;
            if (bt.address.equals(this.address)&&bt.btName.equals(this.btName)){
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
