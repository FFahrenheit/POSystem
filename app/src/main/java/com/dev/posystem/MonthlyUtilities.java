package com.dev.posystem;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

public class MonthlyUtilities extends AppCompatActivity implements DatePickerDialog.OnDateSetListener
{
    private FloatingActionButton goBack;
    private Utilities util;
    private Button setDate;
    private Integer month, year;
    private DatePickerDialog datePicker;
    private TextView date;
    private String server;
    private Button seeSales;
    private Button seeVisits;
    private TextView totalSold;
    private TextView totalPaid;
    private TextView totalEarn;
    private TextView utilityPercentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_utilities);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Ganancias por mes");

        goBack = findViewById(R.id.monthlyUtilitiesGoBack);
        util = new Utilities(getApplicationContext(),goBack);
        server = util.getServer();
        setDate = findViewById(R.id.monthlyUtilitiesSelectDateButton);
        datePicker = createDialogWithoutDateField();
        date = findViewById(R.id.monthlyUtilitiesDate);
        seeSales = findViewById(R.id.monthlyUtilitiesSoldButton);
        seeVisits = findViewById(R.id.monthlyUtilitiesPaidButton);
        totalSold = findViewById(R.id.monthlyUtilitiesSold);
        totalPaid = findViewById(R.id.monthlyUtilitiesPaid);
        totalEarn = findViewById(R.id.monthlyUtilitiesTotal);
        utilityPercentage = findViewById(R.id.monthlyUtilitiesPercentage);

        seeSales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MonthlyUtilities.this, MonthlyReport.class);
                intent.putExtra("set",true);
                intent.putExtra("month",month);
                intent.putExtra("year",year);
                startActivity(intent);
            }
        });

        seeVisits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MonthlyUtilities.this, MonthlyVisits.class);
                intent.putExtra("set",true);
                intent.putExtra("month",month);
                intent.putExtra("year",year);
                startActivity(intent);
            }
        });

        setDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MonthYearPickerDialog pd = new MonthYearPickerDialog();
                pd.setListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        year =  i;
                        month = i1-1;
                        setSales(month,year);
                    }
                });
                pd.show(getSupportFragmentManager(), "MonthYearPickerDialog");
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setSales(Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.YEAR));
    }

    private DatePickerDialog createDialogWithoutDateField()
    {
        DatePickerDialog dpd = new DatePickerDialog(this, this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        try {
            java.lang.reflect.Field[] datePickerDialogFields = dpd.getClass().getDeclaredFields();
            for (java.lang.reflect.Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker"))
                {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(dpd);
                    java.lang.reflect.Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
                    for (java.lang.reflect.Field datePickerField : datePickerFields)
                    {
                        if ("mDaySpinner".equals(datePickerField.getName()))
                        {
                            datePickerField.setAccessible(true);
                            Object dayPicker = datePickerField.get(datePicker);
                            ((View) dayPicker).setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
        }
        return dpd;
    }

    @Override
    public void onDateSet(DatePicker view, int y, int m, int dayOfMonth)
    {
        this.year = y;
        this.month = m;
        setSales(this.month,this.year);
    }

    private void setSales(Integer m, Integer y)
    {
        date.setText(getMonthForInt(m)+" "+y);
        String url = server + "getMonthStats.php?year="+y+"&month="+(m+1);
        final JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Double sold = response.getDouble("sold");
                            Double paid = response.getDouble("paid");
                            Double percentage = (paid/sold - 1)*100;
                            Double total = sold-paid;
                            if(percentage.isNaN()|| percentage.isInfinite())
                            {
                                percentage = 0.0;
                            }
                            if(paid<=0)
                            {
                                percentage = 100.0;
                            }
                            if(percentage>0 && percentage<10)
                            {
                                utilityPercentage.setTextColor(Color.YELLOW);
                            }
                            else if(percentage<0)
                            {
                                utilityPercentage.setTextColor(Color.RED);
                            }
                            else
                            {
                                utilityPercentage.setTextColor(Color.GREEN);
                            }
                            if(total>0)
                            {
                                totalEarn.setTextColor(Color.GREEN);
                                totalEarn.setText("+$"+total);
                            }
                            else
                            {
                                totalEarn.setTextColor(Color.RED);
                                totalEarn.setText("$"+total);
                            }
                            totalSold.setText("$"+util.df2(sold));
                            totalPaid.setText("$"+util.df2(paid));
                            utilityPercentage.setText(util.df2(percentage)+"%");

                        } catch (JSONException e) {
                            util.snack("Error: "+e.getMessage());
                            try {
                                util.toast(util.simpleStatusAlert(response.getInt("status")));
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        util.snack("No se pudo recibir: "+error.getMessage());
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    private String getMonthForInt(int num)
    {
        String month = "Formato incorrecto";
        DateFormatSymbols dfs = new DateFormatSymbols(new Locale("es", "ES"));
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11 )
        {
            month = months[num];
        }
        return month.toUpperCase();
    }
}