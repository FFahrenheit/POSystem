package com.dev.posystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ProviderAdapter extends BaseAdapter
{
    public Context context;
    public ArrayList<Provider> providers;

    public ProviderAdapter(Context ctx, ArrayList<Provider> source)
    {
        this.context = ctx;
        this.providers = source;
    }

    @Override
    public int getCount() {
        return providers.size();
    }

    @Override
    public Object getItem(int i) {
        return providers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return providers.get(i).getPk();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        LayoutInflater link = LayoutInflater.from(context);

        View v = link.inflate(R.layout.provider_card, null);

        TextView tvn = (TextView) v.findViewById(R.id.providerName);
        tvn.setText(providers.get(i).getName());

        tvn = (TextView) v.findViewById(R.id.providerNumber);
        tvn.setText(providers.get(i).getNumber());

        tvn = (TextView) v.findViewById(R.id.providerProducts);
        tvn.setText(providers.get(i).getProducts()+" productos");

        return v;
    }
}
