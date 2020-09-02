package com.dev.posystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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