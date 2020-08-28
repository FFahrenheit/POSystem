package com.dev.posystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class NewProduct extends AppCompatActivity {

    private boolean isEdit;
    private EditText vName;
    private EditText vCode;
    private EditText vStock;
    private Spinner vEsp;
    private EditText vPrice;
    private String originalCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent rx = getIntent();
        isEdit = rx.getBooleanExtra("edit",false);
        if(isEdit)
        {
            originalCode = rx.getStringExtra("code");
            ((TextView)findViewById(R.id.newProductLabel)).setText("Editar producto");
        }

        vName = findViewById(R.id.newProductName);
        vCode = findViewById(R.id.newProductCode);
        vStock = findViewById(R.id.newProductStock);
        vPrice = findViewById(R.id.newProductPrice);
        vEsp = findViewById(R.id.newProductEsp);

        FloatingActionButton save = findViewById(R.id.newProductSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = vName.getText().toString();
                if(name.length() < 2)
                {
                    Snackbar.make(view, "El nombre es muy corto", Snackbar.LENGTH_LONG).show();
                    return;
                }
                String code = vCode.getText().toString();
                if(!isNumber(code) || code.contains("."))
                {
                    Snackbar.make(view, "Codigo no valido. Recuerde dejar vacio si no tiene codigo", Snackbar.LENGTH_LONG).show();
                    return;
                }
                String stock = vStock.getText().toString();
                if(!isNumber(stock))
                {
                    Snackbar.make(view, "Stock no valido", Snackbar.LENGTH_LONG).show();
                    return;
                }
                String price = vPrice.getText().toString();
                if(!isNumber(stock)) {
                    Snackbar.make(view, "Precio invalido", Snackbar.LENGTH_LONG).show();
                    return;
                }
                String esp = vEsp.getSelectedItem().toString();
                if(stock.contains(".") && esp.equals("Pieza(s)"))
                {
                    Snackbar.make(view, "Ingrese piezas enteras", Snackbar.LENGTH_LONG).show();
                    return;
                }
                esp = esp.equals("Pieza(s)")? "pieza" : "granel";
                ProductItem item = new ProductItem(code,name,Double.parseDouble(stock),Double.parseDouble(price),esp);
                String url = isEdit ? "editProduct.php" : "newProduct.php";
                url = getServer() + url + item.getGET();
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int status = response.getInt("status");
                                    switch(status)
                                    {
                                        case 200:
                                            Snackbar.make(vName, "Producto guardado", Snackbar.LENGTH_LONG).show();
                                            if(isEdit)
                                            {
                                                finish();
                                            }
                                            else
                                            {
                                                resetForm();
                                            }
                                            break;
                                        case 100:
                                            Snackbar.make(vName, "Error de conexion", Snackbar.LENGTH_LONG).show();
                                            break;
                                        case 101:
                                            Snackbar.make(vName, "No se pudo guardar", Snackbar.LENGTH_LONG).show();
                                            break;
                                        default:
                                            Snackbar.make(vName, "Error desconocido", Snackbar.LENGTH_LONG).show();
                                            break;

                                    }
                                } catch (JSONException e) {
                                    Snackbar.make(vName, "Error en el servidor", Snackbar.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Snackbar.make(vName, "No se pudo guardar", Snackbar.LENGTH_LONG).show();
                            }
                        }
                );
                RequestQueue queue = Volley.newRequestQueue(NewProduct.this);
                queue.add(request);
            }
        });

        FloatingActionButton exit = findViewById(R.id.newProductReturn);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEdit)
                {
                    Snackbar.make(view, "Edicion cancelada", Snackbar.LENGTH_LONG).show();
                }
                finish();
            }
        });
    }

    public boolean isNumber(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    private String getServer()
    {
        SharedPreferences preferences = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        String server = "http://"+preferences.getString("server","localhost")+"/POSystem/";
        return server;
    }

    private String getCashier()
    {
        SharedPreferences preferences = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        String cashier = preferences.getString("cashier","Ivan");
        return cashier;
    }

    private void resetForm()
    {
        vCode.setText("");
        vPrice.setText("");
        vStock.setText("");
        vName.setText("");
    }
}