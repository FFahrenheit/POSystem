package com.dev.posystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.WindowManager;
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

        final TextView emptyView = findViewById(R.id.emptyMain);
        productList.setEmptyView(emptyView);

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
                        }); //Keep for future references
                if(productAdapter.products.size()>0)
                {
                    Intent intent = new Intent(MainActivity.this, PayAccount.class);
                    startActivity(intent);
                }
                else
                {
                    Snackbar.make(totalPrice,"Ingrese productos para poder pagar",Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

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
                //Toast.makeText(getApplicationContext(),"Jeje"+item.getItemId(),Toast.LENGTH_SHORT).show();
                Intent intent;
                switch (item.getItemId())
                {
                    case R.id.nav_utilities_monthly:
                        intent = new Intent(MainActivity.this, MonthlyUtilities.class);
                        break;
                    case R.id.nav_utilities_daily:
                        intent = new Intent(MainActivity.this, DailyUtilities.class);
                        break;
                    case R.id.nav_buys_month:
                        intent = new Intent(MainActivity.this, MonthlyVisits.class);
                        break;
                    case R.id.nav_buys_daily:
                        intent = new Intent(MainActivity.this, DayVisit.class);
                        break;
                    case R.id.nav_provider_add:
                        intent = new Intent(MainActivity.this, NewVisit.class);
                        break;
                    case R.id.nav_provider_manage:
                        intent = new Intent(MainActivity.this, ManageProvider.class);
                        break;
                    case R.id.nav_reports_month:
                        intent = new Intent(MainActivity.this, MonthlyReport.class);
                        break;
                    case R.id.nav_reports_daily:
                        intent = new Intent(MainActivity.this, DailyReport.class);
                        break;
                    case R.id.nav_addP:
                        intent = new Intent(MainActivity.this, NewProduct.class);
                        intent.putExtra("edit",false);
                        break;
                    case R.id.nav_editP:
                        intent = new Intent(MainActivity.this,EditProduct.class);
                        break;
                    case R.id.nav_inventory:
                        intent = new Intent(MainActivity.this,AddInventory.class);
                        break;
                    case R.id.nav_print:
                        intent = new Intent(MainActivity.this, PrintBarcode.class);
                        break;
                    default:
                        Snackbar.make(productList, "Aun no implementado", Snackbar.LENGTH_LONG)
                                .show();
                        return true;
                }
                startActivity(intent);

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
                if(charSequence.toString().contains("\n"))   //Cambiar
                {
                    barcode.setText("");
                    String codeBar = charSequence.toString().substring(0,charSequence.length()-1);
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
                                                product.setPrimaryKey(Integer.parseInt(response.getString("pk")));
                                                product.setTotal(product.getPrice());
                                                productAdapter.products.add(product);
                                                productAdapter.notifyDataSetChanged();
                                                Toast.makeText(getApplicationContext(),product.getName()+" agregado",Toast.LENGTH_SHORT).show();
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
                                                message = "Producto no encontrado";
                                                break;
                                            default:
                                                message = "Error desconocido";
                                                break;
                                        }
                                        if(statusCode[0]!=200)
                                        {
                                            Snackbar.make(emptyView,message,Snackbar.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Snackbar.make(emptyView,"No se pudo ejecutar la operacion",Snackbar.LENGTH_SHORT).show();
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
        barcode.setSelection(0);
        barcode.requestFocus();
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

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
                            if(products.size()>0)
                            {
                                productAdapter.notifyDataSetChanged();
                                updateCart();
                            }
                            else
                            {
                                Snackbar.make(productList, "Carrito cargado", Snackbar.LENGTH_SHORT).show();
                                updateCart();
                            }
                        }
                        catch(JSONException e)
                        {
                            Snackbar.make(productList, "El carrito no pudo ser cargado", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Reintentar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            updateList();
                                        }
                                    }).show();                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(productList, "El carrito no pudo ser cargado", Snackbar.LENGTH_INDEFINITE)
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
        updateList();
        updateCart();
        barcode.setSelection(0);
        barcode.requestFocus();
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    public void refresh(MenuItem menuItem)
    {
        updateList();
        Snackbar.make(productList,"Carrito refrescado",Snackbar.LENGTH_SHORT).show();
    }
}