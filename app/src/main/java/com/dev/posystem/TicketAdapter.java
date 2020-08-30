package com.dev.posystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TicketAdapter extends BaseAdapter
{
    public ArrayList<Product> products;
    public Context context;

    public Double getTotal()
    {
        Double total = 0.0;
        for (int i = 0; i < products.size(); i++)
        {
            total += products.get(i).getTotal();
        }
        return total;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int i) {
        return products.get(i);
    }

    @Override
    public long getItemId(int i) {
        return products.get(i).getPrimaryKey();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater link = LayoutInflater.from(context);

        View v = link.inflate(R.layout.ticket_card, null);

        TextView tvn = v.findViewById(R.id.ticketQty);
        tvn.setText(products.get(i).getQuantity().toString());

        tvn = v.findViewById(R.id.ticketProduct);
        tvn.setText(products.get(i).getName());

        tvn = v.findViewById(R.id.ticketPriceUnit);
        tvn.setText("$"+products.get(i).getPrice());

        tvn =  v.findViewById(R.id.ticketPrice);
        tvn.setText(products.get(i).getTotal().toString());

        return v;
    }
}
