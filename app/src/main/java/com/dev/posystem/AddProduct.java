package com.dev.posystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.regex.Pattern;

public class AddProduct extends AppCompatActivity
{
    private Integer productsAdded = 0;
    private TextInputEditText search;
    private ListView searchResults;
    private ArrayList<ProductItem> products;
    private ProductItemAdapter adapter;
    private String server;
    private String cashier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        search = (TextInputEditText) findViewById(R.id.searchScanCode);
        searchResults = (ListView) findViewById(R.id.searchProductList);

        products = new ArrayList<>();
        adapter = new ProductItemAdapter();
        adapter.context = AddProduct.this;
        adapter.products = products;
        searchResults.setAdapter(adapter);

        server = getServer();
        cashier = getCashier();

        searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> listView, View itemView, final int itemPosition, long itemId)
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(AddProduct.this);
                final EditText edittext = new EditText(AddProduct.this);
                edittext.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL |
                        InputType.TYPE_NUMBER_FLAG_SIGNED);
                alert.setMessage("Ingrese la cantidad a agregar");
                alert.setTitle("Agregar "+products.get(itemPosition).getName());

                alert.setView(edittext);

                alert.setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        if(isCode(edittext.getText().toString())) {
                            Double value = Double.parseDouble(edittext.getText().toString());
                            if (value > products.get(itemPosition).getStock()) {
                                Toast.makeText(getApplicationContext(), "No hay suficiente stock", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String url = server + "addProductManual.php?code="+products.get(itemPosition).getCodeBar()
                                        +"&qty="+value+"&user="+cashier;
                                JsonObjectRequest request  = new JsonObjectRequest(
                                        Request.Method.GET,
                                        url,
                                        null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    int statusCode = response.getInt("status");
                                                    String message;
                                                    switch(statusCode)
                                                    {
                                                        case 200:
                                                            productsAdded++;
                                                            message = "Producto agregado";
                                                            break;
                                                        default:
                                                            message = "Error desconocido";
                                                            break;
                                                    }
                                                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                                                } catch (JSONException e) {
                                                    Toast.makeText(getApplicationContext(),"Error en el servidor",Toast.LENGTH_SHORT).show();
                                                    e.printStackTrace();
                                                }

                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Toast.makeText(getApplicationContext(),"No se pudo agregar",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                );

                                RequestQueue queue = Volley.newRequestQueue(AddProduct.this);
                                queue.add(request);
                                search.setText("");
                            }
                        }
                    }
                });

                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                    }
                });
                alert.show();
            }
        });

        FloatingActionButton fab = findViewById(R.id.searchReady);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent data = new Intent();
                data.setData(Uri.parse(productsAdded.toString()));
                setResult(RESULT_OK, data);
                finish();
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() >= 2)
                {
                    products.clear();
                    String searchQuery = charSequence.toString();
                    String url;
                    if(isCode(searchQuery))
                    {
                        url = server + "searchByCode.php?code="+searchQuery;
                    }
                    else
                    {
                        url = server + "searchByName.php?name="+searchQuery;
                    }
                    final JsonArrayRequest request = new JsonArrayRequest(
                            Request.Method.GET,
                            url,
                            null,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try
                                    {
                                        for (int i = 0; i <  response.length(); i++)
                                        {
                                            JSONObject product = response.getJSONObject(i);
                                            ProductItem item = new ProductItem();
                                            item.setCodeBar(product.getString("codigo"));
                                            item.setName(product.getString("descripcion"));
                                            item.setEsp(product.getString("especificacion"));
                                            item.setPrice(product.getDouble("precio"));
                                            item.setStock(product.getDouble("stock"));

                                            products.add(item);
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                    catch(JSONException e)
                                    {
                                        Toast.makeText(getApplicationContext(),"No se encontraron resultados",Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(),"No se pudieron recibir resultados",Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                    RequestQueue queue = Volley.newRequestQueue(AddProduct.this);

                    queue.add(request);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public boolean isCode(String strNum) {
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
        String cashier = "http://"+preferences.getString("server","localhost")+"/POSystem/";
        return cashier;
    }

    @Override
    public void onStop ()
    {
        super.onStop();
        Intent data = new Intent();
        data.setData(Uri.parse(productsAdded.toString()));
        setResult(RESULT_OK, data);
        finish();
    }

}