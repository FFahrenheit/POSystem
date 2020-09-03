package com.dev.posystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class AddVisit extends AppCompatActivity
{
    private TextView title;
    private ListView searchList;
    private ListView productList;
    private TextView emptySearch;
    private TextView emptyProduct;
    private TextView status;
    private String providerName;
    private Integer providerKey;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_visit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Visita de proveedor");

        title = findViewById(R.id.addVisitText);
        status = findViewById(R.id.addVisitStatus);
        searchBar = findViewById(R.id.addVisitSearchBar);

        searchList = findViewById(R.id.addVisitSearchList);
        productList = findViewById(R.id.addVisitProductList);
        emptySearch = findViewById(R.id.addVisitSearchEmpty);
        emptyProduct = findViewById(R.id.addVisitProductEmpty);

        searchList.setEmptyView(emptySearch);
        productList.setEmptyView(emptyProduct);

        Intent intent = getIntent();
        providerName = intent.getStringExtra("name");
        providerKey = intent.getIntExtra("pk",0);

        title.setText("Visita de "+providerName);

        FloatingActionButton fab = findViewById(R.id.addVisitCancel);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Aqui va la cancelacion jsjs", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}