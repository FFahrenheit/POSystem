package com.dev.posystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ManageProvider extends AppCompatActivity
{
    private FloatingActionButton returnButton;
    private FloatingActionButton addButton;
    private EditText search;
    private ListView searchList;
    private TextView empty;
    private ArrayList<Provider> providers;
    private Utilities util;
    private ProviderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_provider);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        search = findViewById(R.id.manageProviderSearch);
        searchList = findViewById(R.id.manageProviderList);
        empty = findViewById(R.id.manageProviderEmpty);
        providers = new ArrayList<>();
        adapter = new ProviderAdapter(getApplicationContext(),providers);
        searchList.setAdapter(adapter);
        searchList.setEmptyView(empty);
        util = new Utilities(getApplicationContext(),empty);

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

    private void searchProvider(String query)
    {
        String url = util.getServer()+"searchProvider.php?name="+query;
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        providers.clear();
                        for(int i=0; i<response.length();i++)
                        {
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
                        util.snack("No se pudieron recibir resultados"+error.getMessage());
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }
}