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
import android.view.WindowManager;
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

public class ManageProvider extends AppCompatActivity {
    private FloatingActionButton returnButton;
    private FloatingActionButton addButton;
    private EditText search;
    private ListView searchList;
    private TextView empty;
    private ArrayList<Provider> providers;
    private Utilities util;
    private ProviderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_provider);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        search = findViewById(R.id.manageProviderSearch);
        searchList = findViewById(R.id.manageProviderList);
        empty = findViewById(R.id.manageProviderEmpty);
        providers = new ArrayList<>();
        adapter = new ProviderAdapter(getApplicationContext(), providers);
        searchList.setAdapter(adapter);
        searchList.setEmptyView(empty);
        util = new Utilities(getApplicationContext(), empty);

        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder alert = new AlertDialog.Builder(ManageProvider.this);

                //alert.setMessage("");
                alert.setTitle("Administrar " + providers.get(i).getName());

                String[] options = {"Editar proveedor", "Administrar productos", "Eliminar proveedor"};
                alert.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int iS) {
                        Intent intent;
                        switch (iS)
                        {
                            case 0:
                                intent = new Intent(ManageProvider.this, NewProvider.class);
                                intent.putExtra("edit", true);
                                intent.putExtra("name", providers.get(i).getName());
                                intent.putExtra("number", providers.get(i).getNumber());
                                intent.putExtra("pk", providers.get(i).getPk());
                                startActivity(intent);
                                break;
                            case 1:
                                intent = new Intent(ManageProvider.this,ManageProvisions.class);
                                intent.putExtra("name", providers.get(i).getName());
                                intent.putExtra("pk", providers.get(i).getPk());
                                startActivity(intent);
                                break;
                            case 2:
                                deleteProvider(i);
                                break;
                            default:
                                util.snack("Opcion seleccionada: " + iS);
                        }
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
                searchProvider(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        addButton = findViewById(R.id.manageProviderAdd);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageProvider.this, NewProvider.class);
                startActivity(intent);
            }
        });

        returnButton = findViewById(R.id.manageProviderReturn);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        searchProvider("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    private void deleteProvider(final Integer index) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ManageProvider.this);
        alert.setMessage("¿Esta seguro que desea eliminar a este proveedor y todos los productos que provee?");
        alert.setTitle("Eliminar " + providers.get(index).getName());

        alert.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String url = util.getServer() + "deleteProvider.php?pk=" + providers.get(index).getPk();

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
                                    search.setText("");
                                    searchProvider("");
                                } catch (JSONException e) {
                                    util.snack("No se pudo completar la operacion");
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                util.snack("No se pudo eliminar al proveedor");
                            }
                        }
                );

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
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

    @Override
    protected void onResume() {
        super.onResume();
        searchProvider(search.getText().toString());
    }
}