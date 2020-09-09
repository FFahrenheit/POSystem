package com.dev.posystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
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
    public boolean payable;

    private static DecimalFormat df2 = new DecimalFormat("#.##");

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
        payable = false;

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

        if(isPaid)
        {
            disableFunctions();
            //TODO: Get data from database
        }

        paid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateChange(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        taxPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateTotal();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        goPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPaid)
                {
                    Snackbar.make(list,"La visita ya fue cerrada", Snackbar.LENGTH_LONG).show();
                }
                if(!payable)
                {
                    Snackbar.make(list,"Ingrese una cantidad valida de pago", Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    AlertDialog.Builder alert = new AlertDialog.Builder(PayVisit.this);
                    alert.setTitle("Pagar visita de "+providerName);
                    alert.setMessage("Confirma la siguiente visita:\nTotal: "+totalMoney+
                            "\nImpuestos: "+df2.format(totalMoney-subtotalMoney));

                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            util.snack("Operacion cancelada");
                        }
                    });

                    alert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            String iva,ieps;
                            iva = (hasIva)? "16" : "0";
                            ieps = (hasIEPS)? taxPercentage.getText().toString(): "0";
                            String url = util.getServer() + "closeVisit.php?pk="+visitID+"&subtotal="+subtotalMoney+
                                    "&iva="+iva+"&ieps="+ieps+"&total="+totalMoney;

                            JsonObjectRequest request = new JsonObjectRequest(
                                    Request.Method.GET,
                                    url,
                                    null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                int status = response.getInt("status");
                                                util.simpleStatusAlert(status);
                                                if(status == 200)
                                                {
                                                    isPaid = true;
                                                    disableFunctions();
                                                }
                                            } catch (JSONException e) {
                                                util.snack("Error: "+e.getMessage());
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            util.snack("Error al recibir: "+error.getMessage());
                                        }
                                    }
                            );

                            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                            queue.add(request);
                        }
                    });

                    alert.create().show();
                }
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!isPaid)
                {
                    Snackbar.make(list, "Si desea modificar la informacion regrese", Snackbar.LENGTH_LONG)
                            .setAction("Regresar", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    finish();
                                }
                            }).show();
                }
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
                        subtotal.setText("Subtotal: $"+df2.format(subtotalMoney));
                        updateTotal();
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

    public void disableFunctions()
    {
        taxPercentage.setFocusable(false);
        taxPercentage.setClickable(false);
        paid.setFocusable(false);
        paid.setClickable(false);
        goPay.setClickable(false);
        goPay.setFocusable(false);
        ((CheckBox) findViewById(R.id.payVisitIVA)).setClickable(false);
        ((CheckBox) findViewById(R.id.payVisitIVA)).setFocusable(false);
        ((CheckBox) findViewById(R.id.payVisitIEPS)).setClickable(false);
        ((CheckBox) findViewById(R.id.payVisitIEPS)).setFocusable(false);
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
                if(checked)
                {
                    taxPercentage.setHint("Ingrese IEPS");
                }
                else
                {
                    taxPercentage.setHint("");
                }
                break;
        }
        updateTotal();
    }

    private void updateTotal()
    {
        totalMoney = subtotalMoney;
        if(hasIva)
        {
            totalMoney += subtotalMoney * 0.16;
        }
        if(hasIEPS)
        {
            if(!util.isCode(taxPercentage.getText().toString()))
            {
                util.snack("Ingrese un porcentaje valido");
            }
            else
            {
                Double percentage = Double.parseDouble(taxPercentage.getText().toString());
                totalMoney += subtotalMoney * percentage / 100;
            }
        }
        total.setText("Total: $"+df2.format(totalMoney));
        if(!paid.getText().toString().equals(""))
        {
            updateChange(paid.getText().toString());
        }
    }

    private void updateChange(String paidAmount)
    {
        if(util.isCode(paidAmount))
        {
            Double changeAmount = Double.parseDouble(paidAmount) - totalMoney;
            if(changeAmount>=0)
            {
                payable = true;
                change.setTextColor(Color.GREEN);
            }
            else
            {
                payable = false;
                change.setTextColor(Color.RED);
            }
            change.setText("$"+df2.format(changeAmount));
        }
        else
        {
            payable = false;
            change.setTextColor(Color.GRAY);
            change.setText("...");
            util.snack("Ingrese una cantidad valida");
        }
    }

}