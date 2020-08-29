package com.dev.posystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

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

    public void simpleStatusAlert(int status)
    {
        switch (status)
        {
            case 200:
                snack("Operacion completada con exito");
                break;
            case 100:
                snack("Error al conectar a la base de datos");
                break;
            case 101:
                snack("Error al realizar la operacion");
                break;
            case 102:
                snack("Se realizo la operacion pero no se registro en el historial");
                break;
            default:
                snack("Error desconocido");
                break;
        }
    }
}
