package com.dev.posystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class PayVisit extends AppCompatActivity
{
    private FloatingActionButton goBack;
    private FloatingActionButton goPay;
    private TextView subtotal;
    private TextView total;
    private EditText paid;
    private EditText taxPercentage;
    private TextView change;
    private ListView list;
    private TextView title;
    private ArrayList<VisitTicket> products;
    private VisitTicketAdapter adapter;
    private boolean isPaid;
    private boolean hasIva;
    private boolean hasIEPS;
    private Integer visitID;
    private String providerName;
    private Utilities util;
    private Double totalMoney;
    private Double subtotalMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_visit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Ticket de visita");

        Intent intent = getIntent();
        isPaid = intent.getBooleanExtra("paid",false);
        visitID = intent.getIntExtra("key",0);
        providerName = intent.getStringExtra("name");
        hasIva = false;
        hasIEPS = false;

        goBack = findViewById(R.id.payVisitReturn);
        goPay = findViewById(R.id.payVisitPay);
        subtotal = findViewById(R.id.payVisitSubotal);
        total = findViewById(R.id.payVisitTotal);
        paid = findViewById(R.id.payVisitPaid);
        taxPercentage = findViewById(R.id.payVisitIEPSPercentage);
        change = findViewById(R.id.payVisitChange);
        list = findViewById(R.id.payVisitList);
        title = findViewById(R.id.payVisitProvider);

        title.setText("Visita de "+providerName);
        util = new Utilities(getApplicationContext(),list);

        products = new ArrayList<>();
        adapter = new VisitTicketAdapter(getApplicationContext(),products);
        list.setAdapter(adapter);

        updateVisit();

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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

    private void updateVisit()
    {
        String url = util.getServer() + "getVisit.php?key="+visitID;
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        products.clear();
                        for(int i=0; i<response.length();i++)
                        {
                            try {
                                VisitTicket item = new VisitTicket(response.getJSONObject(i));
                                products.add(item);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                        subtotalMoney = adapter.getTotal();
                        subtotal.setText("Subtotal: $"+subtotalMoney);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(list,"No se pudieron cargar los datos: "+error.getMessage(),Snackbar.LENGTH_INDEFINITE)
                                .setAction("Reintentar", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        updateVisit();
                                    }
                                }).show();
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    public void onCheckboxClicked(View view)
    {
        boolean checked = ((CheckBox) view).isChecked();
        switch(view.getId())
        {
            case R.id.payVisitIVA:
                hasIva = checked;
                break;
            case R.id.payVisitIEPS:
                hasIEPS = checked;
                break;
        }
    }

    
}