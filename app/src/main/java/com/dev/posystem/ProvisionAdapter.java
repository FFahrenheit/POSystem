package com.dev.posystem;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ProvisionAdapter extends BaseAdapter
{
    public Context context;
    public ArrayList<Provision> provisions;

    public ProvisionAdapter(Context c, ArrayList<Provision> p)
    {
        context = c;
        provisions = p;
    }

    @Override
    public int getCount() {
        return provisions.size();
    }

    @Override
    public Object getItem(int i) {
        return provisions.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        LayoutInflater link = LayoutInflater.from(context);

        View v = link.inflate(R.layout.provision_card, null);

        TextView tvn = v.findViewById(R.id.provisionCode);
        tvn.setText(provisions.get(i).getProductCode());

        tvn = v.findViewById(R.id.provisionName);
        tvn.setText(provisions.get(i).getName());

        tvn = v.findViewById(R.id.provisionPrice);
        tvn.setText("Se compra a $"+provisions.get(i).getPrice());

        tvn = v.findViewById(R.id.provisionProductPrice);
        Double utility = provisions.get(i).getUtility();
        tvn.setText("Se vende a $"+provisions.get(i).getProductPrice()+"("+utility+" % de utilidad)");

        if(utility<10)
        {
            tvn.setTextColor(Color.RED);
        }
        else
        {
            tvn.setTextColor(Color.GREEN);
        }

        return v;
    }
}
