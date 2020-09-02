package com.dev.posystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ProductItemAdapter extends BaseAdapter
{
    public ArrayList<ProductItem> products;
    public Context context;

    public ProductItemAdapter(Context applicationContext, ArrayList<ProductItem> products) {
        this.products = products;
        this.context = applicationContext;
    }

    public ProductItemAdapter() {

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
        return Long.parseLong(products.get(i).getCodeBar());
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {

        LayoutInflater link = LayoutInflater.from(context);

        View v = link.inflate(R.layout.product_card, null);

        TextView tvn = (TextView) v.findViewById(R.id.productName);
        tvn.setText(products.get(i).getName());

        tvn = (TextView) v.findViewById(R.id.productCode);
        tvn.setText(products.get(i).getCodeBar());

        tvn = (TextView) v.findViewById(R.id.productPrice);
        tvn.setText("$"+products.get(i).getPrice());

        tvn = (TextView) v.findViewById(R.id.productQuantity);
        tvn.setText("Cantidad: "+products.get(i).getStock()+" "+products.get(i).getEsp());

        return v;
    }
}
