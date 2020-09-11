package com.dev.posystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class VisitDayReportAdapter extends BaseAdapter {
    private ArrayList<VisitDayReport> visits;
    private Context context;

    public VisitDayReportAdapter(Context c, ArrayList<VisitDayReport> v)
    {
        visits = v;
        context = c;
    }

    public Double getTotal()
    {
        Double total = 0.0;
        for (int i=0; i<visits.size();i++)
        {
            total += visits.get(i).getTotal();
        }
        return total;
    }


    @Override
    public int getCount() {
        return visits.size();
    }

    @Override
    public Object getItem(int i) {
        return visits.get(i);
    }

    @Override
    public long getItemId(int i) {
        return visits.get(i).getPk();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        LayoutInflater link = LayoutInflater.from(context);

        View v = link.inflate(R.layout.visit_card, null);

        TextView tvn = v.findViewById(R.id.visitProductCount);
        tvn.setText(visits.get(i).getProducts().toString()+"arts.");

        tvn = v.findViewById(R.id.visitProviderName);
        tvn.setText(visits.get(i).getProvider());

        tvn = v.findViewById(R.id.visitHour);
        tvn.setText(visits.get(i).getDate());

        tvn = v.findViewById(R.id.visitTotal);
        tvn.setText("$"+visits.get(i).getTotal());

        return v;
    }
}
