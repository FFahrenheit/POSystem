package com.dev.posystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import java.util.regex.Pattern;

public class AddProduct extends AppCompatActivity
{
    private Integer productsAdded;
    private TextInputEditText search;
    private ListView searchResults;
    private ArrayList<ProductItem> products;
    private ProductItemAdapter adapter;
    private String server;
    private String cashier;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productsAdded = 0;

        search = findViewById(R.id.searchScanCode);
        searchResults = findViewById(R.id.searchProductList);

        products = new ArrayList<>();
        adapter = new ProductItemAdapter();
        adapter.context = AddProduct.this;
        adapter.products = products;
        searchResults.setAdapter(adapter);

        emptyView = findViewById(R.id.emptySearch);
        searchResults.setEmptyView(emptyView);

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
                        if(edittext.getText().toString().charAt(0)=='.')
                        {
                            edittext.setText("0"+edittext.getText());
                        }
                        if(isCode(edittext.getText().toString())) {
                            Double value = Double.parseDouble(edittext.getText().toString());
                            String addedStock = value.toString();
                            if(addedStock.contains("-"))
                            {
                                Snackbar.make(search, "Ingrese stock positivo", Snackbar.LENGTH_LONG).show();
                                return;
                            }
                            if(addedStock.contains(".") && products.get(itemPosition).getEsp().contains("ieza"))
                            {
                                Double val = Double.parseDouble(addedStock);
                                if(val != Math.floor(val))
                                {
                                    Snackbar.make(search, "Ingrese piezas enteras", Snackbar.LENGTH_LONG).show();
                                    return;
                                }
                            }

                            if (value > products.get(itemPosition).getStock()) {
                                Snackbar.make(search,"No hay suficiente stock",Snackbar.LENGTH_LONG).setAction("Agregar stock",
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                final Utilities util = new Utilities(getApplicationContext(),search);
                                                AlertDialog.Builder alert = new AlertDialog.Builder(AddProduct.this);
                                                final EditText edittext = new EditText(AddProduct.this);
                                                edittext.setInputType(InputType.TYPE_CLASS_NUMBER |
                                                        InputType.TYPE_NUMBER_FLAG_DECIMAL |
                                                        InputType.TYPE_NUMBER_FLAG_SIGNED);
                                                alert.setMessage("Ingrese el stock a agregar");
                                                alert.setTitle("Agregar "+products.get(itemPosition).getName());

                                                alert.setView(edittext);

                                                alert.setPositiveButton("Agregar stock", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i)
                                                    {
                                                        String addedStock = edittext.getText().toString();
                                                        if(addedStock.contains("-"))
                                                        {
                                                            Snackbar.make(search, "Ingrese stock positivo", Snackbar.LENGTH_LONG).show();
                                                            return;
                                                        }
                                                        if(addedStock.contains(".") && products.get(itemPosition).getEsp().contains("ieza"))
                                                        {
                                                            Double val = Double.parseDouble(addedStock);
                                                            if(val != Math.floor(val))
                                                            {
                                                                Snackbar.make(search, "Ingrese piezas enteras", Snackbar.LENGTH_LONG).show();
                                                                return;
                                                            }
                                                        }

                                                        String url = server + "addInventory.php?pk="+products.get(itemPosition).getCodeBar()+"&add="+addedStock;
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
                                                                                searchProducts(search.getText().toString());
                                                                                util.snack("Se ha agregado el producto");
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
                                                                    }
                                                                }
                                                        );

                                                        RequestQueue queue = Volley.newRequestQueue(AddProduct.this);
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
                                        }).show();
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
                                                            Snackbar.make(search,"Producto agregado",Snackbar.LENGTH_SHORT)
                                                                    .setAction("Regresar", new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View view) {
                                                                            finish();
                                                                        }
                                                                    }).show();
                                                            break;
                                                        default:
                                                            Snackbar.make(search,"No se pudo agregar el producto",Snackbar.LENGTH_LONG).show();
                                                            break;
                                                    }
                                                } catch (JSONException e) {
                                                    Snackbar.make(search,"Error en el servidor",Snackbar.LENGTH_LONG).show();
                                                    e.printStackTrace();
                                                }

                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Snackbar.make(search,"No se pudo agregar",Snackbar.LENGTH_LONG).show();
                                            }
                                        }
                                );

                                RequestQueue queue = Volley.newRequestQueue(AddProduct.this);
                                queue.add(request);
                                search.setText("");
                            }
                        }
                        else
                        {
                            Snackbar.make(search,"Ingrese un numero valido",Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Snackbar.make(search,"Operacion cancelada",Snackbar.LENGTH_SHORT).show();
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
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() >= 2)
                {
                    searchProducts(charSequence.toString());
                }
                else
                {
                    emptyView.setText("Realice su busqueda");
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
        String cashier = preferences.getString("cashier","Ivan");
        return cashier;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent data = new Intent();
                data.setData(Uri.parse(productsAdded.toString()));
                setResult(RESULT_OK, data);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent data = new Intent();
        data.setData(Uri.parse(productsAdded.toString()));
        setResult(RESULT_OK, data);
        finish();
    }

    private void searchProducts(final String searchQuery)
    {
        products.clear();
        String url;
        if(isCode(searchQuery) && !searchQuery.contains("."))
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
                            products.clear();
                            if(response.length()<=0)
                            {
                                emptyView.setText("No se encontraron resultados");
                            }
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
                            Snackbar.make(search,"No se encontraron resultados",Snackbar.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(search,"No se pudieron recibir los resultados",Snackbar.LENGTH_LONG)
                                .setAction("Reintentar", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        searchProducts(searchQuery);
                                    }
                                }).show();
                    }
                }
        );
        RequestQueue queue = Volley.newRequestQueue(AddProduct.this);

        queue.add(request);
    }
}