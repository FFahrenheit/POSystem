package com.dev.posystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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

    private Utilities util;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vCode = findViewById(R.id.newProductCode);
        vName = findViewById(R.id.newProductName);
        vStock = findViewById(R.id.newProductStock);
        vPrice = findViewById(R.id.newProductPrice);
        vEsp = findViewById(R.id.newProductEsp);

        util = new Utilities(getApplicationContext(), vCode);

        Intent rx = getIntent();
        isEdit = rx.getBooleanExtra("edit",false);
        if(isEdit)
        {
            originalCode = rx.getStringExtra("code");
            vCode.setText(originalCode);
            vName.setText(rx.getStringExtra("name"));
            vStock.setText(rx.getStringExtra("stock"));
            vPrice.setText(rx.getStringExtra("price"));
            vEsp.setSelection((rx.getStringExtra("esp").contains("iez")? 0 : 1));

            ((TextView)findViewById(R.id.newProductLabel)).setText("Editar producto");
            if(originalCode.length()<8)
            {
                vCode.setFocusable(false);
                vCode.setClickable(false);
            }
        }

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
                if(!code.equals("") && (!isNumber(code) || code.contains(".")))
                {
                    Snackbar.make(view, "Codigo no valido. Recuerde dejar vacio si no tiene codigo", Snackbar.LENGTH_LONG).show();
                    return;
                }
                String stock = vStock.getText().toString();
                if(stock.charAt(0)=='.')
                {
                    stock = "0"+stock;
                }
                if(!isNumber(stock))
                {
                    Snackbar.make(view, "Stock no valido", Snackbar.LENGTH_LONG).show();
                    return;
                }
                String price = vPrice.getText().toString();
                if(price.charAt(0)=='.')
                {
                    price = '0'+price;
                }
                if(!isNumber(stock)) {
                    Snackbar.make(view, "Precio invalido", Snackbar.LENGTH_LONG).show();
                    return;
                }
                String esp = vEsp.getSelectedItem().toString();
                if(stock.contains(".") && esp.equals("Pieza(s)"))
                {
                    Double val = Double.parseDouble(stock);
                    if(val != Math.floor(val))
                    {
                        Snackbar.make(view, "Ingrese piezas enteras sin punto", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }
                esp = esp.equals("Pieza(s)")? "pieza" : "granel";
                ProductItem item = new ProductItem(code,name,Double.parseDouble(stock),Double.parseDouble(price),esp);
                String url = isEdit ? "editProduct.php" : "newProduct.php";
                url = getServer() + url + item.getGET();
                if(isEdit)
                {
                    url += "&old="+originalCode;
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
                                    switch(status)
                                    {
                                        case 200:
                                            if(isEdit)
                                            {
                                                Snackbar.make(vName, "Producto modificado", Snackbar.LENGTH_INDEFINITE)
                                                        .setAction("Regresar", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                finish();
                                                            }
                                                        }).show();
                                            }
                                            else
                                            {
                                                if(vCode.getText().toString().trim().equals("")) //Sin codigo
                                                {
                                                    AlertDialog.Builder alert = new AlertDialog.Builder(NewProduct.this);
                                                    final int code = response.getInt("code");
                                                    alert.setMessage("¿Desea imprimir el codigo para " + vName.getText().toString() +"?");
                                                    alert.setTitle("Imprimir codigo " + code);

                                                    alert.setPositiveButton("Imprimir", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i)
                                                        {
                                                            String url = getServer() + "printBarcode.php?pk=" + code;
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
                                                                                    util.toast("Enviado a la impresora");
                                                                                }
                                                                            } catch (JSONException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }
                                                                    },
                                                                    new Response.ErrorListener() {
                                                                        @Override
                                                                        public void onErrorResponse(VolleyError error) {
                                                                            util.snack("No se pudo completar la peticion");
                                                                            Log.d("Error: ", error.getMessage());
                                                                        }
                                                                    }
                                                            );

                                                            RequestQueue queue = Volley.newRequestQueue(NewProduct.this);
                                                            queue.add(request);
                                                        }
                                                    });

                                                    alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            util.snack("Operacion cancelada");
                                                        }
                                                    });

                                                    alert.show();
                                                }

                                                Snackbar.make(vName, "Producto guardado", Snackbar.LENGTH_LONG)
                                                        .setAction("Aceptar", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                resetForm();
                                                            }
                                                        }).show();
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
                                    Snackbar.make(vName, "Error en el servidor o la respuesta", Snackbar.LENGTH_LONG).show();
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