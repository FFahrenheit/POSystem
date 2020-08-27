package com.dev.posystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    public ArrayList<Product> products;
    private ProductAdapter productAdapter;
    private ListView productList;
    private TextView totalProducts;
    private TextView totalPrice;
    private TextInputEditText barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Punto de venta");
        totalProducts = (TextView) findViewById(R.id.itemsCount);
        totalPrice = (TextView) findViewById(R.id.totalCount);
        products = new ArrayList<>();
        //products.add(new Product());
        productAdapter = new ProductAdapter();
        productList = (ListView) findViewById(R.id.productList);
        productAdapter.context = MainActivity.this;
        productAdapter.products = products;
        productList.setAdapter(productAdapter);

        FloatingActionButton fab = findViewById(R.id.finishSale);
        FloatingActionButton addProduct = findViewById(R.id.addProduct);

        addProduct.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        //RequestCode = 100
                        Intent intent = new Intent(MainActivity.this,AddProduct.class);
                        startActivityForResult(intent,100);
                    }
                });


        fab.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        Toast.makeText(getApplicationContext(),"hOLA", Toast.LENGTH_SHORT);

        barcode = (TextInputEditText) findViewById(R.id.scannerCode);
        barcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                if(charSequence.length()>=8)   //Cambiar
                {
                    String codeBar = charSequence.toString();
                    final int[] statusCode = new int[1];
                    final Product product = new Product();
                    product.setCodeBar(codeBar);
                    String url = getServer() + "addProductScanner.php?user="+getCashier()+"&codeBar="+codeBar;
                    JsonObjectRequest request = new JsonObjectRequest(
                            Request.Method.GET,
                            url,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        statusCode[0] = response.getInt("status");
                                        String message="";
                                        switch(statusCode[0])
                                        {
                                            case 200:
                                                Toast.makeText(MainActivity.this,"WOOOOW",Toast.LENGTH_SHORT).show();
                                                product.setName(response.getString("name"));
                                                product.setPrice(response.getDouble("price"));
                                                product.setQuantity(1.0);
                                                product.setPrimaryKey(response.getInt("pk"));
                                                product.setTotal(product.getPrice());
                                                productAdapter.products.add(product);
                                                productAdapter.notifyDataSetChanged();
                                                updateBarcode();
                                                updateCart();
                                                break;
                                            case 400:
                                                message = "No se pudo acceder al servidor";
                                                break;
                                            case 300:
                                                message = "Error de servidor";
                                                break;
                                            case 101:
                                                message = "No hay suficiente stock";
                                                break;
                                            case 102:
                                                message = "Producto no registrado";
                                                break;
                                            default:
                                                message = "Error desconocido";
                                                break;
                                        }
                                        if(statusCode[0]!=200)
                                        {
                                            Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(MainActivity.this,"No se pudo ejecutar la operacion",Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                    RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

                    queue.add(request);

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void showSettings(MenuItem item)
    {

        Intent changeWindow = new Intent(MainActivity.this, AppSettings.class);
        startActivity(changeWindow);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void updateCart()
    {
        productAdapter.notifyDataSetChanged();
        totalPrice.setText("$"+productAdapter.getTotal());
        totalProducts.setText("Total de productos: "+productAdapter.getCount());
    }

    private void updateBarcode()
    {
        barcode.setText("");
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            String returnedResult = data.getData().toString();
            Toast.makeText(getApplicationContext(),"Hay que actualizar "+returnedResult+" productos",Toast.LENGTH_SHORT).show();
        }
    }
}