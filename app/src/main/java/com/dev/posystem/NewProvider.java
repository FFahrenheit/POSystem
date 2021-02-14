package com.dev.posystem;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class NewProvider extends AppCompatActivity {

    private FloatingActionButton returnButton;
    private FloatingActionButton saveButton;
    private TextInputEditText vName;
    private TextInputEditText vNumber;
    private Utilities util;
    private boolean isEdit;
    private Integer pk;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_provider);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        saveButton = findViewById(R.id.newProviderSave);
        vName = findViewById(R.id.newProviderName);
        vNumber = findViewById(R.id.newProviderNumber);
        util = new Utilities(getApplicationContext(),saveButton);
        title = findViewById(R.id.newProviderAlert);


        Intent intent = getIntent();

        isEdit = intent.getBooleanExtra("edit",false);

        if(isEdit)
        {
            setTitle("Editar proveedor");
            this.pk = intent.getIntExtra("pk",0);
            vName.setText(intent.getStringExtra("name"));
            vNumber.setText(intent.getStringExtra("number"));
            title.setText("Editar a "+intent.getStringExtra("name"));
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = vName.getText().toString();
                String number = vNumber.getText().toString();
                if(name.length()<4)
                {
                    util.snack("El nombre del proveedor es muy corto");
                    return;
                }
                if(number.length()<=6)
                {
                    util.snack("El numero es muy corto");
                    return;
                }

                String url;

                if(isEdit)
                {
                    url = util.getServer() + "editProvider.php?name="+name+"&number="+number+"&pk="+pk;
                }
                else
                {
                    url = util.getServer() + "newProvider.php?name="+name+"&number="+number;
                }


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
                                        if(isEdit)
                                        {
                                            title.setText("Editar a "+vName.getText().toString());
                                            Snackbar.make(returnButton,"Producto editado",Snackbar.LENGTH_INDEFINITE)
                                                    .setAction("Salir", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            finish();
                                                        }
                                                    });
                                        }
                                        else
                                        {
                                            vName.setText("");
                                            vNumber.setText("");
                                        }
                                    }
                                } catch (JSONException e) {
                                    util.snack("Error, no se pudo completar la operacion");
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                util.snack("Hubo un error al procesar la peticion");
                            }
                        }
                );

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                queue.add(request);

            }
        });

        returnButton = findViewById(R.id.newProviderAnother);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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
}