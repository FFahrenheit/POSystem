package com.dev.posystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DailyVisitAdapter extends BaseAdapter
{
    public Context context;
    public ArrayList<DailyVisit> visits;

    public DailyVisitAdapter(Context ctx, ArrayList<DailyVisit> sls)
    {
        this.context = ctx;
        this.visits = sls;
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
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        LayoutInflater link = LayoutInflater.from(context);

        View v = link.inflate(R.layout.visit_day_card, null);

        TextView tvn = v.findViewById(R.id.dailyVisitCount);
        tvn.setText(visits.get(i).getVisitCount()+" visitas");

        tvn =  v.findViewById(R.id.dailyVisitDay);
        tvn.setText(visits.get(i).getDay());

        tvn = v.findViewById(R.id.dailyVisitTotal);
        tvn.setText("$"+visits.get(i).getTotal());

        return v;
    }
}
