package com.dev.posystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppSettings extends AppCompatActivity {

    private TextView ipAddress;
    private TextView cashierName;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pref = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        editor = pref.edit();
        ipAddress = (TextView) findViewById(R.id.serverIP);
        cashierName = (TextView) findViewById(R.id.cashierName);
        ipAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setIP();
            }
        });
        ((TextView) findViewById(R.id.serverIPName)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setIP();
            }
        });

        cashierName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCashier();
            }
        });

        ((TextView) findViewById(R.id.cashierNameName)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCashier();
            }
        });

        initializeData();
    }

    private void setCashier()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(AppSettings.this);
        final EditText edittext = new EditText(AppSettings.this);
        alert.setMessage("Ingrese el nombre de la caja / cajero");
        alert.setTitle("Nombre del cajero");

        alert.setView(edittext);

        alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                if(edittext.getText().toString().length()>0 && edittext.getText().toString().length()<=14)
                {
                    editor.putString("cashier", edittext.getText().toString());
                    editor.commit();
                    initializeData();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"No es valido (1-14 caracteres)",Toast.LENGTH_SHORT).show();
                }
            }
        });

        alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getApplicationContext(), "Operacion cancelada", Toast.LENGTH_SHORT).show();
            }
        });
        alert.show();
    }

    private void setIP()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(AppSettings.this);
        final EditText edittext = new EditText(AppSettings.this);
        alert.setMessage("Ingrese la direccion");
        alert.setTitle("Seleccionar servidor");

        alert.setView(edittext);

        alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                if(isValidIPAddress(edittext.getText().toString().split("/")[0]))
                {
                    Toast.makeText(getApplicationContext(),edittext.getText().toString(),Toast.LENGTH_SHORT).show();
                    editor.putString("server", edittext.getText().toString());
                    editor.commit();
                    initializeData();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"No es valido",Toast.LENGTH_SHORT).show();
                }
            }
        });

        alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getApplicationContext(), "Operacion cancelada", Toast.LENGTH_SHORT).show();
            }
        });
        alert.show();
    }

    private void initializeData()
    {
        ipAddress.setText(pref.getString("server","Click para configurar"));
        cashierName.setText(pref.getString("cashier","Click para configurar"));
    }

    public static boolean isValidIPAddress(String ip)
    {

        String zeroTo255
                = "(\\d{1,2}|(0|1)\\"
                + "d{2}|2[0-4]\\d|25[0-5])";

        String regex
                = zeroTo255 + "\\."
                + zeroTo255 + "\\."
                + zeroTo255 + "\\."
                + zeroTo255;

        Pattern p = Pattern.compile(regex);

        if (ip == null) {
            return false;
        }

        Matcher m = p.matcher(ip);

        return m.matches();
    }
}