package com.neuralBit.letsTalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.campaign.R;

import java.util.zip.Inflater;

public class LangSpinnerAdapter extends BaseAdapter {

    private Context context;
    private  int flags[];
    private String[] languages;
    private  LayoutInflater inflater;
    @Override
    public int getCount() {
        return flags.length;
    }

    public  LangSpinnerAdapter (Context context, int [] flags , String [] languages){
        this.context =context;
        this.flags = flags;
        this.languages =languages;
        inflater =LayoutInflater.from(context);
    }
    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.lang_spin_layout, null);
        ImageView icon = (ImageView) convertView.findViewById(R.id.imageView);
        TextView names = (TextView) convertView.findViewById(R.id.textView);
        icon.setImageResource(flags[position]);
        names.setText(languages[position]);
        return convertView;
    }


}
