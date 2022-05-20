package com.example.myapplication;

import java.util.Map;

public class ItemBLArray {
    String thisName;
    Map<String, Double> thisItem;

    ItemBLArray(String name, Map<String, Double> item){
        thisName = name;
        thisItem = item;
    }
    public String Name(){
        return thisName;
    }
    public String distance(){
        if(thisItem.containsKey("M")){
            return thisItem.get("M").toString();
        }
        return "-1";
    }
    public String x(){
        if(thisItem.containsKey("M")){
            return thisItem.get("X").toString();
        }
        return "-1";
    }
    public String y(){
        if(thisItem.containsKey("M")){
            return thisItem.get("Y").toString();
        }
        return "-1";
    }

}
