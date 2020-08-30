package com.dev.posystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SaleAdapter extends BaseAdapter
{
    public Context context;
    public ArrayList<Sale> sales;

    public Double getTotal()
    {
        Double total = 0.0;
        for(int i=0;i<sales.size();i++)
        {
            total += sales.get(i).getTotal();
        }
        return total;
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
        return sales.get(i).getPk();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater link = LayoutInflater.from(context);

        View v = link.inflate(R.layout.sale_card, null);

        TextView tvn = v.findViewById(R.id.saleProducts);
        tvn.setText(sales.get(i).getProductCount()+" prods.");

        tvn = v.findViewById(R.id.saleTime);
        tvn.setText(sales.get(i).getHour());

        tvn = v.findViewById(R.id.saleTotal);
        tvn.setText("$"+sales.get(i).getTotal());

        tvn = v.findViewById(R.id.saleCashier);
        tvn.setText(sales.get(i).getCashier());

        return v;
    }
}
