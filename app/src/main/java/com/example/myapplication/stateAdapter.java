package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class stateAdapter extends BaseAdapter {
    private Map<String,Map<String,Double>> dictMap;
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<String> keys;

    stateAdapter( Context context,Map<String,Map<String,Double>> map) {

        dictMap = map;

        ctx = context;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private void fillArray(){
        keys= new ArrayList<String>();
        Object[] arr = dictMap.keySet().toArray();

        for (int j = 0; j < arr.length; j++){
            if(arr[j]!=null)
            keys.add(arr[j].toString());
        }

    }

    @Override
    public int getCount() {
        return dictMap.size() ;
    }

    @Override
    public Object getItem(int i) {
        fillArray();

        if(keys.size()>0){
            try {
                Object result = new ItemBLArray(keys.get(i),dictMap.get(keys.get(i)));
                return result;
            }catch(IndexOutOfBoundsException e){
                Log.e("index error","" + i + " dictMap:"+dictMap);
            }

        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_item, parent, false);
        }
        try{
            ItemBLArray p = (ItemBLArray) getItem(position);
            Log.e("getItem", p.Name() );
            ((TextView) view.findViewById(R.id.Name))
                    .setText(p.Name());
            ((TextView) view.findViewById(R.id.distance))
                    .setText(p.distance());
            ((TextView) view.findViewById(R.id.x))
                    .setText(p.y());
            ((TextView) view.findViewById(R.id.y))
                    .setText(p.x());
        }
        catch(NullPointerException e){

        }
        return view;
    }
}