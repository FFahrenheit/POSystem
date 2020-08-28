package com.dev.posystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity{

    private AppBarConfiguration mAppBarConfiguration;
    public ArrayList<Product> products;
    private ProductAdapter productAdapter;
    private ListView productList;
    private NavigationView navigationView;
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

        totalProducts = findViewById(R.id.itemsCount);
        totalPrice = findViewById(R.id.totalCount);
        products = new ArrayList<>();
        productAdapter = new ProductAdapter();
        productList = findViewById(R.id.productList);
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
                Snackbar.make(view, "El carrito no pudo ser cargado", Snackbar.LENGTH_LONG)
                        .setAction("Reintentar", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                updateList();
                            }
                        }).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        //NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.bringToFront();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Toast.makeText(getApplicationContext(),"Jeje"+item.getItemId(),Toast.LENGTH_SHORT).show();
                switch (item.getItemId())
                {
                    case R.id.nav_addP:
                        Intent intent = new Intent(MainActivity.this, NewProduct.class);
                        intent.putExtra("edit",false);
                        startActivity(intent);
                        break;
                    default:
                        Snackbar.make(productList, "Aun no implementado", Snackbar.LENGTH_LONG)
                                .show();
                }
                //close navigation drawer
                int size = navigationView.getMenu().size();
                for (int i = 0; i < size; i++) {
                    navigationView.getMenu().getItem(i).setChecked(false);
                }
                ((DrawerLayout)findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
                return true;
            }
        });

        barcode = findViewById(R.id.scannerCode);
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

        productList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> listView, View itemView, final int itemPosition, long itemId)
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                final EditText edittext = new EditText(MainActivity.this);
                edittext.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL |
                        InputType.TYPE_NUMBER_FLAG_SIGNED);
                edittext.setHint("Nueva cantidad");

                alert.setMessage("Editar cantidades o borrar articulo");
                alert.setTitle("Editar "+products.get(itemPosition).getName());

                alert.setView(edittext);

                alert.setPositiveButton("Modificar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        String qty = edittext.getText().toString();
                        if(qty.equals("") || qty == null || qty.equals("0"))
                        {
                            Snackbar.make(productList, "Ingrese una cantidad valida", Snackbar.LENGTH_LONG).show();
                            return;
                        }

                        String url = getServer() + "editQty.php?pk="+products.get(itemPosition).getPrimaryKey()+"&qty="+edittext.getText().toString();
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
                                                    Snackbar.make(productList, "Cantidad modificada", Snackbar.LENGTH_LONG).show();
                                                    updateList();
                                                    break;
                                                case 100:
                                                    Snackbar.make(productList, "Error de conexion", Snackbar.LENGTH_LONG).show();
                                                    break;
                                                case 101:
                                                    Snackbar.make(productList, "No se pudo actualizar la cantidad", Snackbar.LENGTH_LONG).show();
                                                    break;
                                                default:
                                                    Snackbar.make(productList, "Error desconocido", Snackbar.LENGTH_LONG).show();
                                                    break;
                                            }
                                        } catch (JSONException e) {
                                            Snackbar.make(productList, "Error con el servidor", Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Snackbar.make(productList, "Error con la peticion", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                        );
                        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                        queue.add(request);
                    }
                });

                alert.setNegativeButton("Borrar articulo", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        {
                            String url = getServer() + "deleteArticle.php?pk="+products.get(itemPosition).getPrimaryKey();

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
                                                        Snackbar.make(productList, "Articulo borrado", Snackbar.LENGTH_LONG).show();
                                                        updateList();
                                                        break;
                                                    case 100:
                                                        Snackbar.make(productList, "Error de conexion", Snackbar.LENGTH_LONG).show();
                                                        break;
                                                    case 101:
                                                        Snackbar.make(productList, "No se pudo actualizar la cantidad", Snackbar.LENGTH_LONG).show();
                                                        break;
                                                    default:
                                                        Snackbar.make(productList, "Error desconocido", Snackbar.LENGTH_LONG).show();
                                                        break;
                                                }
                                            } catch (JSONException e) {
                                                Snackbar.make(productList, "Error con el servidor", Snackbar.LENGTH_LONG).show();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Snackbar.make(productList, "Error con la peticion", Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                            );
                            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                            queue.add(request);
                        }
                    }
                });

                alert.setNeutralButton("Cancelar",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Snackbar.make(productList, "Operacion cancelada", Snackbar.LENGTH_LONG).show();
                    }
                });

                alert.show();


            }
        });
        updateList();
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
        String cashier = preferences.getString("cashier","Ivan");
        return cashier;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Toast.makeText(getApplicationContext(),"Request: "+requestCode+" Result: "+resultCode,Toast.LENGTH_SHORT).show();
        if (requestCode == 100) {
            if(data.getData() != null)
            {
                String returnedResult = data.getData().toString();
                //Toast.makeText(getApplicationContext(),"Hay que actualizar "+returnedResult+" productos",Toast.LENGTH_SHORT).show();
                if(Integer.parseInt(returnedResult)!=0)
                {
                    updateList();
                }
            }
        }
    }

    private void updateList()
    {
        final String url = getServer()+"getCart.php?user="+getCashier();
        //Toast.makeText(getApplicationContext(),url,Toast.LENGTH_SHORT).show();
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try
                        {
                            products.clear();
                            for (int i=0; i<response.length();i++)
                            {
                                JSONObject product = response.getJSONObject(i);
                                Product prod = new Product();
                                prod.setName(product.getString("name"));
                                prod.setEsp(product.getString("esp"));
                                prod.setPrimaryKey(product.getInt("pk"));
                                prod.setQuantity(product.getDouble("qty"));
                                prod.setCodeBar(product.getString("code"));
                                prod.setPrice(product.getDouble("price"));
                                prod.setTotal(prod.getPrice() * prod.getQuantity());
                                products.add(prod);
                            }
                            productAdapter.notifyDataSetChanged();
                            updateCart();
                        }
                        catch(JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(productList, "El carrito no pudo ser cargado", Snackbar.LENGTH_LONG)
                                .setAction("Reintentar", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        updateList();
                                    }
                                }).show();
                    }
                }
        );
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }
}