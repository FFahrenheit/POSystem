package com.dev.posystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class VisitTicketAdapter extends BaseAdapter
{
    public ArrayList<VisitTicket> items;
    public Context context;

    public VisitTicketAdapter(Context applicationContext, ArrayList<VisitTicket> i)
    {
        context = applicationContext;
        items = i;
    }

    public Double getTotal()
    {
        Double total = 0.0;
        for (int i = 0; i < items.size(); i++)
        {
            total += items.get(i).getTotal();
        }
        return total;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return items.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        LayoutInflater link = LayoutInflater.from(context);

        View v = link.inflate(R.layout.visit_ticket_card, null);

        TextView tvn = v.findViewById(R.id.visitTicketQty);
        tvn.setText(items.get(i).getQty().toString());

        tvn = v.findViewById(R.id.visitTicketProduct);
        tvn.setText(items.get(i).getProductName());

        tvn = v.findViewById(R.id.visitTicketPriceUnit);
        tvn.setText("$"+items.get(i).getPrice());

        tvn =  v.findViewById(R.id.visitTicketPrice);
        tvn.setText("$"+items.get(i).getTotal());

        return v;
    }
}
