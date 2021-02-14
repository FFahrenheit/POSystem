package com.dev.posystem;

import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

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

public class PrintBarcode extends AppCompatActivity {

    private EditText search;
    private ListView list;
    private ArrayList<ProductItem> products;
    private ProductItemAdapter adapter;
    private Utilities util;
    private String server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_barcode);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Imprimir codigos de barras");

        search = findViewById(R.id.addInventorySearch);
        list = findViewById(R.id.addInventoryList);
        products = new ArrayList<>();
        adapter = new ProductItemAdapter();
        adapter.context = getApplicationContext();
        adapter.products = products;
        list.setAdapter(adapter);
        list.setEmptyView(findViewById(R.id.emptySearchAddInventory));
        util = new Utilities(getApplicationContext(),search);
        server = util.getServer();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int itemPosition, long l) {
                AlertDialog.Builder alert = new AlertDialog.Builder(PrintBarcode.this);
                alert.setMessage("¿Desea imprimir el codigo para " + products.get(itemPosition).getName()+"?");
                alert.setTitle("Imprimir codigo "+products.get(itemPosition).getCodeBar());

                alert.setPositiveButton("Imprimir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        String url = server + "printBarcode.php?pk="+products.get(itemPosition).getCodeBar();
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
                                            if(status==200)
                                            {
                                                searchProducts(search.getText().toString());
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        util.snack("No se pudo completar la peticion");
                                        Log.d("Error: ", error.getMessage());
                                    }
                                }
                        );

                        RequestQueue queue = Volley.newRequestQueue(PrintBarcode.this);
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
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length()>=1)
                {
                    searchProducts(charSequence.toString());
                }
                else
                {
                    searchProducts("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        FloatingActionButton fab = findViewById(R.id.addInventoryReturn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //.setAction("Action", null).show();
                finish();
            }
        });

        searchProducts("");
    }

    private void searchProducts(final String searchQuery)
    {
        products.clear();
        String url;
        if(searchQuery.equals(""))
        {
            url = server + "searchByCodeDesc.php?code=" + searchQuery;
        }
        else if(util.isCode(searchQuery) && !searchQuery.contains("."))
        {
            url = server + "searchByCode.php?code=" + searchQuery;
        }
        else
        {
            url = server + "searchByName.php?name="+searchQuery;
        }
        final JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try
                        {
                            products.clear();
                            for (int i = 0; i <  response.length(); i++)
                            {
                                JSONObject product = response.getJSONObject(i);
                                ProductItem item = new ProductItem();
                                item.setCodeBar(product.getString("codigo"));
                                item.setName(product.getString("descripcion"));
                                item.setEsp(product.getString("especificacion"));
                                item.setPrice(product.getDouble("precio"));
                                item.setStock(product.getDouble("stock"));

                                products.add(item);
                            }
                            adapter.notifyDataSetChanged();
                        }
                        catch(JSONException e)
                        {
                            Snackbar.make(search,"No se encontraron resultados",Snackbar.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(search,"No se pudieron recibir los resultados",Snackbar.LENGTH_LONG)
                                .setAction("Reintentar", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        searchProducts(searchQuery);
                                    }
                                }).show();
                    }
                }
        );
        RequestQueue queue = Volley.newRequestQueue(PrintBarcode.this);

        queue.add(request);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }
}