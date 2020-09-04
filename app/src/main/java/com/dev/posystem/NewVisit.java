package com.dev.posystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

public class NewVisit extends AppCompatActivity
{
    private ListView providerList;
    private ArrayList<Provider> providers;
    private ProviderAdapter adapter;
    private TextView empty;
    private EditText search;
    private Utilities util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_visit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Agregar nueva visita");

        providerList = findViewById(R.id.newVisitList);
        providers = new ArrayList<>();
        adapter = new ProviderAdapter(getApplicationContext(),providers);
        empty = findViewById(R.id.newVisitEmpty);
        search = findViewById(R.id.newVisitSearch);
        util = new Utilities(getApplicationContext(),empty);

        providerList.setAdapter(adapter);
        providerList.setEmptyView(empty);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchProvider(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        providerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder alert = new AlertDialog.Builder(NewVisit.this);
                alert.setTitle("Registrar visita de "+providers.get(i).getName());
                alert.setMessage("¿Desea empezar a registrar una visita de "+providers.get(i).getName()+"?");

                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        util.snack("Visita cancelada");
                    }
                });

                alert.setPositiveButton("Registrar visita", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int item) {
                        String url = util.getServer() + "newVisit.php?provider="+providers.get(i).getPk()+"&cashier="+util.getCashier();
                        JsonObjectRequest request = new JsonObjectRequest(
                                Request.Method.GET,
                                url,
                                null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            int status = response.getInt("status");
                                            if(status==200)
                                            {
                                                Intent intent = new Intent(NewVisit.this, AddVisit.class);
                                                intent.putExtra("name",providers.get(i).getName());
                                                intent.putExtra("pk",providers.get(i).getPk());
                                                intent.putExtra("key",response.getInt("key"));
                                                startActivity(intent);
                                            }
                                        } catch (JSONException e) {
                                            util.snack("No se pudo iniciar la visita");
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        util.snack("Error al abrir visita: "+error.getMessage());
                                    }
                                }
                        );
                        RequestQueue queue = Volley.newRequestQueue(NewVisit.this);
                        queue.add(request);
                    }
                });

                alert.create().show();
            }
        });

        FloatingActionButton fab = findViewById(R.id.newVisitReturn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        findCurrentVisit();

        searchProvider("");
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

    private void searchProvider(String query) {
        String url = util.getServer() + "searchProvider.php?name=" + query;
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        providers.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                Provider provider = new Provider(response.getJSONObject(i));
                                providers.add(provider);
                            } catch (JSONException e) {
                                util.snack("No se recibieron los datos correctamente");
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        util.snack("No se pudieron recibir resultados" + error.getMessage());
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    private void findCurrentVisit()
    {
        String url = util.getServer() + "getCurrentVisit.php?cashier="+util.getCashier();
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int status = response.getInt("status");
                            if(status==200)
                            {
                                    Intent intent = new Intent(NewVisit.this, AddVisit.class);
                                    intent.putExtra("pk", response.getInt(("pk")));
                                    intent.putExtra("name",response.getString("name"));
                                    intent.putExtra("key",response.getInt("key"));
                                    startActivity(intent);
                            }
                            else if(status==201)
                            {
                                return;
                            }
                            else
                            {
                                util.simpleStatusAlert(status);
                            }
                        } catch (JSONException e) {
                            util.snack("Error al recibir informacion: "+e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        util.snack("Error el buscar visitas: "+error.getMessage());
                        Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
        );

        RequestQueue  queue = Volley.newRequestQueue(NewVisit.this);
        queue.add(request);
    }

    @Override
    public void onResume(){
        super.onResume();
        findCurrentVisit();
    }
}