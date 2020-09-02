package com.dev.posystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

public class NewProvision extends AppCompatActivity
{
    private FloatingActionButton fab;
    private ArrayList<ProductItem> products;
    private ProductItemAdapter adapter;
    private ListView list;
    private EditText search;
    private TextView empty;
    private Utilities util;
    private Integer pk;
    private String providerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_provision);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        products = new ArrayList<>();
        adapter = new ProductItemAdapter(getApplicationContext(),products);
        list = findViewById(R.id.newProvisionList);
        search = findViewById(R.id.newProvisionSearch);
        empty = findViewById(R.id.newProvisionEmpty);
        util = new Utilities(getApplicationContext(),empty);
        list.setAdapter(adapter);
        list.setEmptyView(empty);

        Intent intent = getIntent();
        pk = intent.getIntExtra("pk",0);
        providerName = intent.getStringExtra("name");

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder alert = new AlertDialog.Builder(NewProvision.this);
                final EditText edittext = new EditText(NewProvision.this);
                edittext.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL |
                        InputType.TYPE_NUMBER_FLAG_SIGNED);
                if(products.get(i).isAlreadyProvider())
                {
                    alert.setMessage("Inserte el nuevo precio del producto del proveedor");
                    alert.setTitle("Editar "+products.get(i).getName()+" de "+providerName);
                }
                else
                {
                    alert.setMessage("Ingrese el precio del producto del proveedor");
                    alert.setTitle("Agregar "+products.get(i).getName()+" de "+providerName);
                }
                alert.setView(edittext);

                alert.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int iS) {
                        if(edittext.getText().toString().contains("-"))
                        {
                            util.snack("Ingrese un precio positivo");
                            return;
                        }
                        if(!util.isCode(edittext.getText().toString()))
                        {
                            util.snack("Ingrese un valor positivo");
                            return;
                        }
                        String url = util.getServer()+"addProvision.php?code="+products.get(i).getCodeBar()
                                +"&provider="+pk+"&price="+edittext.getText().toString();
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
                                                search.setText("");
                                                if(products.get(i).isAlreadyProvider())
                                                {
                                                    util.snack("Provision actualizada");
                                                }
                                                else
                                                {
                                                    util.snack("Provision agregada");
                                                }
                                            }
                                        } catch (JSONException e) {
                                            util.snack("Error al obtener estados");
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        util.snack("Error al procesar la solicitud");
                                    }
                                }
                        );

                        RequestQueue queue = Volley.newRequestQueue(NewProvision.this);
                        queue.add(request);
                    }
                });

                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        util.snack("Operacion cancelada");
                    }
                });

                alert.create().show();
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length()>0)
                {
                    searchProducts(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        fab = findViewById(R.id.newProvisionBack);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void searchProducts(String query)
    {
        String url;
        if(util.isCode(query))
        {
            url = util.getServer()+"searchProductsCode.php?provider="+this.pk+"&code="+query;
        }
        else
        {
            url = util.getServer()+"searchProductsName.php?provider="+this.pk+"&name="+query;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        products.clear();
                        for(int i=0; i<response.length();i++)
                        {
                            try {
                                ProductItem item = new ProductItem(response.getJSONObject(i),true);
                                products.add(item);
                            } catch (JSONException e) {
                                util.snack("No se recibieron correctamente los productos");
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        util.snack("No se pudieron recibir los datos");
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
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