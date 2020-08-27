package com.dev.posystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class TicketController
{
    private Context context;
    private String server;
    private String cashier;

    public TicketController(Context cont)
    {
        this.context = cont;
        SharedPreferences preferences = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        server = "http://"+preferences.getString("server","localhost")+"/POSystem/";
        cashier = preferences.getString("cashier","Ivan");
    }

    private String getServer()
    {
        SharedPreferences preferences = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        server = "http://"+preferences.getString("server","localhost")+"/POSystem/";
        return this.server;
    }

    public Product addProductWithScanner(String codeBar,final ProductAdapter adapter)
    {
        final int[] statusCode = new int[1];
        final Product product = new Product();
        product.setCodeBar(codeBar);
        String url = getServer() + "addProductScanner.php?user="+cashier+"&codeBar="+codeBar;
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
                                    adapter.products.add(product);
                                    adapter.notifyDataSetChanged();
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
                                Toast.makeText(context,message,Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,"No se pudo ejecutar la operacion",Toast.LENGTH_SHORT).show();
                    }
                }
        );
        RequestQueue queue = Volley.newRequestQueue(context);

        queue.add(request);

        return statusCode[0]==200? product : null;
    }
}