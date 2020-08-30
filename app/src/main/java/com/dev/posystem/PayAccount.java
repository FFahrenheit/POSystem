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

import java.util.ArrayList;

public class PayAccount extends AppCompatActivity
{
    private FloatingActionButton ticketReturn;
    private FloatingActionButton ticketPay;
    private EditText ticketPaid;
    private TextView ticketChange;
    private TextView ticketTotal;
    private TextView ticketProductCount;
    private ArrayList<Product> products;
    private TicketAdapter adapter;
    private ListView ticketList;
    private Utilities util;

    private boolean paid = false;
    private Sale paidSale = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_account);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ticketReturn = findViewById(R.id.ticketReturn);
        ticketPay = findViewById(R.id.ticketConfirm);
        ticketPaid = findViewById(R.id.ticketPaid);
        ticketChange = findViewById(R.id.ticketChange);
        ticketTotal = findViewById(R.id.ticketTotal);
        ticketProductCount = findViewById(R.id.ticketProductCount);
        products = new ArrayList<>();
        adapter = new TicketAdapter();
        adapter.context = getApplicationContext();
        adapter.products = products;
        ticketList = findViewById(R.id.ticketList);
        ticketList.setAdapter(adapter);
        util = new Utilities(getApplicationContext(),ticketChange);

        Intent receive = getIntent();
        paid = receive.getBooleanExtra("paid",false);
        if(paid)
        {
            try
            {
                paidSale = new Sale(new JSONObject(receive.getStringExtra("sale")));
                ticketPaid.setText(paidSale.getPaid().toString());
                Double cash = Double.parseDouble(ticketPaid.getText().toString());
                Double change  = cash - adapter.getTotal();
                ticketChange.setText("$"+String.format("%.2f",change));
                if(change>=0)
                {
                    ticketChange.setTextColor(Color.GREEN);
                }
                else
                {
                    ticketChange.setTextColor(Color.RED);
                }
                setPaid();
            } catch (JSONException e) {
                util.snack("No se pudo recibir la venta");
            }
        }
        initTicket();

        ticketPaid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(util.isCode(charSequence.toString()))
                {
                    Double cash = Double.parseDouble(charSequence.toString());
                    Double change  = cash - adapter.getTotal();
                    ticketChange.setText("$"+String.format("%.2f",change));
                    if(change>=0)
                    {
                        ticketChange.setTextColor(Color.GREEN);
                    }
                    else
                    {
                        ticketChange.setTextColor(Color.RED);
                    }
                }
                else
                {
                    ticketChange.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ticketPay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!util.isCode(ticketPaid.getText().toString()))
                {
                    util.snack("Ingrese una cantidad valida");
                }
                else if(isPayable())
                {
                    pay();
                }
                else
                {
                    util.snack("Hace falta dinero para pagar");
                }
            }
        });

        ticketReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initTicket()
    {
        String url;
        if(paid)
        {
            url = util.getServer() + "getSale.php?pk="+paidSale.getPk();
        }
        else
        {
            url = util.getServer() + "getCart.php?user="+util.getCashier();
        }
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try
                        {
                            products.clear();
                            for (int i=0; i<response.length();i++)
                            {
                                JSONObject product = response.getJSONObject(i);
                                Product prod = new Product();
                                prod.setName(product.getString("name"));
                                prod.setEsp(product.getString("esp"));
                                prod.setPrimaryKey(product.getInt("pk"));
                                prod.setQuantity(product.getDouble("qty"));
                                prod.setCodeBar(product.getString("code"));
                                prod.setPrice(product.getDouble("price"));
                                prod.setTotal(prod.getPrice() * prod.getQuantity());
                                products.add(prod);
                            }
                            adapter.notifyDataSetChanged();
                            ticketTotal.setText("$"+adapter.getTotal());
                            ticketProductCount.setText("Total de productos: "+adapter.getCount());
                        }
                        catch(JSONException e)
                        {
                            Snackbar.make(ticketChange, "Error con el servidor. Reintente o intente mas tarde", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Reintentar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            initTicket();
                                        }
                                    }).show();                        }                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Snackbar.make(ticketChange, "No se pudo cargar el ticket", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Reintentar", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        initTicket();
                                    }
                                }).show();
                    }
                }
        );
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    private boolean isPayable()
    {
        Double total = adapter.getTotal();
        Double cash = Double.parseDouble(ticketPaid.getText().toString());
        return cash >= total;
    }

    private void pay()
    {
        if(paid)
        {
            util.snack("Esta venta esta cerrada");
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(PayAccount.this);

        alert.setMessage("Confirma la venta de "+adapter.getCount()+" productos por $"+adapter.getTotal());
        alert.setTitle("Confirmar venta");

        alert.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String url = util.getServer() + "payAccount.php?user="+util.getCashier()+"&cash="+ticketPaid.getText().toString()+"&total="+adapter.getTotal();
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int status = response.getInt("status");
                                    String message = util.simpleStatusAlert(status);
                                    if(status==200)
                                    {
                                        Snackbar.make(ticketChange, "Pago realizado con exito", Snackbar.LENGTH_INDEFINITE)
                                                .setAction("Salir", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        finish();
                                                    }
                                                }).show();

                                        setPaid();
                                        paid = true;
                                    }
                                    else
                                    {
                                        Snackbar.make(ticketChange, message, Snackbar.LENGTH_INDEFINITE)
                                                .setAction("Reintentar", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        pay();
                                                    }
                                                }).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Snackbar.make(ticketChange, "No se pudo hacer el pago", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Reintentar", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                pay();
                                            }
                                        }).show();
                            }

                        }
                );

                RequestQueue queue = Volley.newRequestQueue(PayAccount.this);
                queue.add(request);
            }
        });

        alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                util.snack("Operacion cancelada");
            }
        });
        alert.show();
    }

    private void setPaid()
    {
        ticketPay.setFocusable(false);
        ticketPay.setClickable(false);
        ticketPaid.setFocusable(false);
        ticketPaid.setFocusable(false);
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