package com.dev.posystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
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
import android.widget.Toast;

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
import java.util.regex.Pattern;

public class EditProduct extends AppCompatActivity
{
    private TextInputEditText search;
    private ListView searchResults;
    private ArrayList<ProductItem> products;
    private ProductItemAdapter adapter;
    private String server;
    private Utilities util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        search = findViewById(R.id.searchEditProduct);
        searchResults = findViewById(R.id.searchEditList);
        searchResults.setEmptyView(findViewById(R.id.emptySearchEditProduct));

        util = new Utilities(getApplicationContext(),search);

        products = new ArrayList<>();
        adapter = new ProductItemAdapter();
        adapter.context = EditProduct.this;
        adapter.products = products;
        searchResults.setAdapter(adapter);

        server = util.getServer();

        searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> listView, View itemView, final int itemPosition, long itemId)
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(EditProduct.this);

                alert.setMessage("¿Que desea hacer con este producto?");
                alert.setTitle("Modificar o eliminar "+products.get(itemPosition).getName());


                alert.setPositiveButton("Modificar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        Intent intent = new Intent(EditProduct.this,NewProduct.class);
                        intent.putExtra("edit",true);
                        intent.putExtra("code",products.get(itemPosition).getCodeBar());
                        intent.putExtra("name",products.get(itemPosition).getName());
                        intent.putExtra("stock",products.get(itemPosition).getStock().toString());
                        intent.putExtra("price",products.get(itemPosition).getPrice().toString());
                        intent.putExtra("esp",products.get(itemPosition).getEsp());
                        startActivity(intent);
                    }
                });

                alert.setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        util.snack("Operacion cancelada");
                    }
                });

                alert.setNegativeButton("Eliminar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        AlertDialog.Builder nAlert = new AlertDialog.Builder(EditProduct.this);

                        nAlert.setMessage("¿Seguro que desea eliminar el producto?");
                        nAlert.setTitle("Eliminar "+products.get(itemPosition).getName());

                        nAlert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                util.snack("Operacion cancelada");
                            }
                        });

                        nAlert.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String nUrl = server + "deleteProduct.php?code="+products.get(itemPosition).getCodeBar();

                                JsonObjectRequest request = new JsonObjectRequest(
                                        Request.Method.GET,
                                        nUrl,
                                        null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    int status = response.getInt("status");
                                                    util.simpleStatusAlert(status);
                                                    if(status==200)
                                                    {
                                                        search.setText("");
                                                        products.clear();
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                } catch (JSONException e) {
                                                    util.snack("Error en respuesta");
                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                util.snack("No se pudo eliminar el producto");
                                            }
                                        }
                                );

                                RequestQueue queue = Volley.newRequestQueue(EditProduct.this);
                                queue.add(request);
                            }
                        });

                        nAlert.show();
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
                if(charSequence.length() >= 2)
                {
                    products.clear();
                    String searchQuery = charSequence.toString();
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
                                        Toast.makeText(getApplicationContext(),"No se encontraron resultados",Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(),"No se pudieron recibir resultados",Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                    RequestQueue queue = Volley.newRequestQueue(EditProduct.this);

                    queue.add(request);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        FloatingActionButton fab = findViewById(R.id.returnFromEdit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}