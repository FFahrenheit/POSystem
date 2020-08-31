package com.dev.posystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DailySaleAdapter extends BaseAdapter
{
    public Context context;
    public ArrayList<DailySale> sales;

    public DailySaleAdapter(Context ctx, ArrayList<DailySale> sls)
    {
        this.context = ctx;
        this.sales = sls;
    }

    @Override
    public int getCount() {
        return sales.size();
    }

    @Override
    public Object getItem(int i) {
        return sales.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        LayoutInflater link = LayoutInflater.from(context);

        View v = link.inflate(R.layout.sale_day_card, null);

        TextView tvn = v.findViewById(R.id.dailySaleCount);
        tvn.setText(sales.get(i).getSaleCount()+" ventas");

        tvn =  v.findViewById(R.id.dailySaleDay);
        tvn.setText(sales.get(i).getDay());

        tvn = v.findViewById(R.id.dailySaleTotal);
        tvn.setText("$"+sales.get(i).getTotal());

        return v;
    }
}
