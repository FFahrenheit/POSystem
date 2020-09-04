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
    private ArrayList<Provision> provisions;
    private ProvisionAdapter adapter;
    private Utilities util;
    private Integer visitID;

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
        util = new Utilities(getApplicationContext(),emptyProduct);

        provisions = new ArrayList<>();
        adapter = new ProvisionAdapter(getApplicationContext(),provisions);

        searchList.setAdapter(adapter);
        searchList.setEmptyView(emptySearch);
        productList.setEmptyView(emptyProduct);
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemPosition, long l){
                AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());

                final EditText edittext = new EditText(AddVisit.this);

                edittext.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL |
                        InputType.TYPE_NUMBER_FLAG_SIGNED);

                alert.setMessage("Ingrese la cantidad a agregar");
                alert.setTitle("Agregar "+provisions.get(itemPosition).getName());

                alert.setView(edittext);

                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        util.snack("Operacion cancelada");
                    }
                });

                alert.setPositiveButton("Agregar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String qty = edittext.getText().toString();
                                if(qty.contains("-") || !util.isCode(qty))
                                {
                                    util.snack("Ingrese una cantidad valida");
                                    return;
                                }
                                String url = util.getServer() + "addProductVisit.php?item="+provisions.get(i).getProductCode()
                                        +"&qty="+qty+"&price="+provisions.get(i).getPrice();
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
                                                    updateVisit();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                util.snack("Error: "+error.getMessage());
                                            }
                                        }
                                );
                                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                                queue.add(request);
                            }
                        });
                alert.create().show();
            }
        });

        Intent intent = getIntent();
        providerName = intent.getStringExtra("name");
        providerKey = intent.getIntExtra("pk",0);
        visitID = intent.getIntExtra("key",0);

        title.setText("Visita de "+providerName);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                getProvisions(charSequence.toString(),util.isCode(charSequence.toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        FloatingActionButton fab = findViewById(R.id.addVisitCancel);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());

                alert.setTitle("Cancelar visita");
                alert.setMessage("¿Seguro que desea cancelar la visita? Si desea reanudar después solo presiona la flecha de regreso");

                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        util.snack("Operacion cancelada");
                    }
                });

                alert.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String url = util.getServer() + "cancelVisit.php?cashier="+util.getCashier();
                        final JsonObjectRequest request = new JsonObjectRequest(
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
                                                finish();
                                            }
                                        } catch (JSONException e) {
                                            util.snack("No se pudo cancelar, intente de nuevo");
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        util.snack("No se pudo realizar la operacion");
                                    }
                                }
                        );

                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        queue.add(request);
                    }
                });

                alert.create().show();
            }
        });

        getProvisions("",false);
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

    private void getProvisions(String query,boolean isCode)
    {
        String url;
        if(isCode)
        {
            url = util.getServer()+"searchProvisionsCode.php?provider="+providerKey+"&code="+query;
        }
        else
        {
            url = util.getServer()+"searchProvisionsName.php?provider="+providerKey+"&name="+query;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        provisions.clear();
                        if(response.length()==0)
                        {
                            Snackbar.make(emptyProduct,"¿No encuentras el producto? Agregalo",Snackbar.LENGTH_LONG)
                                    .setAction("Agregar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(AddVisit.this, NewProvision.class);
                                            intent.putExtra("pk",providerKey);
                                            intent.putExtra("name",providerName);
                                            startActivity(intent);
                                        }
                                    })
                                    .show();
                        }
                        for(int i=0; i<response.length();i++)
                        {
                            try {
                                Provision provision = new Provision(response.getJSONObject(i));
                                provisions.add(provision);
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
                        util.snack("No se pudieron recibir los datos del servidor: ");
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    private void updateVisit()
    {

    }
}