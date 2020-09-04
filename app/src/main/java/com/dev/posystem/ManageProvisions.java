package com.dev.posystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

public class ManageProvisions extends AppCompatActivity
{
    private FloatingActionButton returnButton;
    private FloatingActionButton add;
    private EditText search;
    private TextView empty;
    private ListView provisionList;
    private ArrayList<Provision> provisions;
    private ProvisionAdapter adapter;
    private Utilities util;
    private String providerName;
    private Integer providerKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_provisions);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        add = findViewById(R.id.manageProvisionAdd);
        search = findViewById(R.id.manageProvisionSearch);
        provisionList = findViewById(R.id.manageProvisionList);
        empty = findViewById(R.id.manageProvisionEmpty);
        provisions = new ArrayList<>();
        adapter = new ProvisionAdapter(getApplicationContext(),provisions);
        provisionList.setAdapter(adapter);
        provisionList.setEmptyView(empty);
        util = new Utilities(getApplicationContext(),empty);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                getProvisions(charSequence.toString(),util.isCode(charSequence.toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Intent intent = getIntent();
        this.providerKey = intent.getIntExtra("pk",0);
        this.providerName = intent.getStringExtra("name");
        setTitle("Productos de "+providerName);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageProvisions.this, NewProvision.class);
                intent.putExtra("pk",providerKey);
                intent.putExtra("name",providerName);
                startActivity(intent);
            }
        });

        returnButton = findViewById(R.id.manageProvisionReturn);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getProvisions("",false);

        provisionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int itemPosition, long l) {
                AlertDialog.Builder alert = new AlertDialog.Builder(ManageProvisions.this);

                final EditText edittext = new EditText(ManageProvisions.this);
                edittext.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL |
                        InputType.TYPE_NUMBER_FLAG_SIGNED);
                edittext.setHint("Nuevo precio de proveedor");

                alert.setMessage("Editar cantidades o borrar provision");
                alert.setTitle("Editar "+provisions.get(itemPosition).getName());
                alert.setView(edittext);

                alert.setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        util.snack("Operacion cnacelada");
                    }
                });

                alert.setPositiveButton("Modificar precio", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newPrice = edittext.getText().toString();
                        if(!util.isCode(newPrice))
                        {
                            util.snack("Ingrese un precio valido");
                            return;
                        }
                        String url = util.getServer() + "editProvision.php?code="+provisions.get(itemPosition).getProductCode()
                                + "&price="+newPrice+"&provider="+providerKey;
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
                                                getProvisions(search.getText().toString(),util.isCode(search.getText().toString()));
                                            }
                                        } catch (JSONException e) {
                                            util.snack("Error al procesar: "+e.getMessage());
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        util.snack("No se pudo procesar la peticion: "+error.getMessage());
                                    }
                                }
                        );

                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        queue.add(request);
                    }
                });

                alert.setNegativeButton("Eliminar producto", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog.Builder confirm = new AlertDialog.Builder(ManageProvisions.this);
                        confirm.setTitle("Eliminar "+provisions.get(itemPosition).getName());
                        confirm.setMessage("¿Seguro que desea borrar este producto de la lista de "+providerName+"?");
                        confirm.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                util.snack("Operacion cancelada");
                            }
                        });
                        confirm.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String url = util.getServer()+"deleteProvision.php?code="+provisions.get(itemPosition).getProductCode()
                                        +"&provider="+providerKey;
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
                                                        getProvisions(search.getText().toString(),util.isCode(search.getText().toString()));
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                util.snack("La operacion no pudo ser completada: "+error.getMessage());
                                            }
                                        }
                                );
                                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                                queue.add(request);
                            }
                        });
                        confirm.create().show();
                    }
                });
                alert.create().show();
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
}