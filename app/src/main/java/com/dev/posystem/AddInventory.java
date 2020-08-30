package com.dev.posystem;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

public class AddInventory extends AppCompatActivity
{
    private  EditText search;
    private ListView list;
    private ArrayList<ProductItem> products;
    private ProductItemAdapter adapter;
    private Utilities util;
    private String server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inventory);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        search = findViewById(R.id.addInventorySearch);
        list = findViewById(R.id.addInventoryList);
        products = new ArrayList<>();
        adapter = new ProductItemAdapter();
        adapter.context = getApplicationContext();
        adapter.products = products;
        list.setAdapter(adapter);
        util = new Utilities(getApplicationContext(),search);
        server = util.getServer();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int itemPosition, long l) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AddInventory.this);
                final EditText edittext = new EditText(AddInventory.this);
                edittext.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL |
                        InputType.TYPE_NUMBER_FLAG_SIGNED);
                alert.setMessage("Ingrese el stock a agregar");
                alert.setTitle("Agregar "+products.get(itemPosition).getName());

                alert.setView(edittext);

                alert.setPositiveButton("Agregar stock", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        String addedStock = edittext.getText().toString();
                        if(addedStock.charAt(0)=='.')
                        {
                            addedStock = "0"+addedStock;
                        }
                        if(addedStock.contains("-"))
                        {
                            Snackbar.make(search, "Ingrese stock positivo", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        if(addedStock.contains(".") && products.get(itemPosition).getEsp().contains("ieza"))
                        {
                            Double val = Double.parseDouble(addedStock);
                            if(val != Math.floor(val))
                            {
                                Snackbar.make(search, "Ingrese piezas enteras", Snackbar.LENGTH_LONG).show();
                                return;
                            }
                        }
                        if(!util.isCode(addedStock))
                        {
                            util.snack("Ingrese un numero valido");
                            return;
                        }

                        String url = server + "addInventory.php?pk="+products.get(itemPosition).getCodeBar()+"&add="+addedStock;
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
                                    }
                                }
                        );

                        RequestQueue queue = Volley.newRequestQueue(AddInventory.this);
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
                if(charSequence.length()>=2)
                {
                    searchProducts(charSequence.toString());
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
    }

    private void searchProducts(final String searchQuery)
    {
        products.clear();
        String url;
        if(util.isCode(searchQuery) && !searchQuery.contains("."))
        {
            url = server + "searchByCode.php?code="+searchQuery;
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
        RequestQueue queue = Volley.newRequestQueue(AddInventory.this);

        queue.add(request);
    }
}