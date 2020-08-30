package com.dev.posystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.view.View;
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
import java.util.regex.Pattern;

public class Utilities
{
    private Context context;
    private View someView;

    Utilities(Context ctx, View view)
    {
        this.context = ctx;
        this.someView = view;
    }
    public String getServer()
    {
        SharedPreferences preferences = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        String server = "http://"+preferences.getString("server","localhost")+"/POSystem/";
        return server;
    }

    public String getCashier()
    {
        SharedPreferences preferences = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        String cashier = preferences.getString("cashier","Ivan");
        return cashier;
    }

    public boolean isCode(String strNum)
    {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    public void toast(String message)
    {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

    public void snack(String message)
    {
        Snackbar.make(someView, message, Snackbar.LENGTH_LONG).show();
    }

    public String simpleStatusAlert(int status)
    {
        String message;
        switch (status)
        {
            case 200:
                message = "Operacion completada con exito";
                break;
            case 100:
                message = ("Error al conectar a la base de datos");
                break;
            case 101:
                message = ("Error al realizar la operacion");
                break;
            case 102:
                message = ("Se realizo la operacion pero no se registro en el historial");
                break;
            default:
                message = "Error desconocido";
                break;
        }
        snack(message);
        return message;
    }
}
