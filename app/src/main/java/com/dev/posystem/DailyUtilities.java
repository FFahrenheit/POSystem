package com.dev.posystem;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
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

import java.util.Calendar;

public class DailyUtilities extends AppCompatActivity implements DatePickerDialog.OnDateSetListener
{
    private Button selectDate;
    private Button seeSales;
    private Button seeVisits;
    private TextView showDate;
    private TextView totalSold;
    private TextView totalPaid;
    private TextView totalEarn;
    private TextView utilityPercentage;
    private FloatingActionButton goBack;
    private Utilities util;
    private String server;
    private Integer day, month, year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_utilities);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Utilidades por dia");

        selectDate = findViewById(R.id.dailyUtilitiesButton);
        seeSales = findViewById(R.id.dailyUtilitiesSoldButton);
        seeVisits = findViewById(R.id.dailyUtilitiesPaidButton);
        showDate = findViewById(R.id.dailyUtilitiesDate);
        totalSold = findViewById(R.id.dailyUtilitiesSold);
        totalPaid = findViewById(R.id.dailyUtilitiesPaid);
        totalEarn = findViewById(R.id.dailyUtilitiesTotal);
        utilityPercentage = findViewById(R.id.dailyUtilitiesPercentage);
        goBack = findViewById(R.id.dailyUtilitiesGoBack);
        util = new Utilities(getApplicationContext(),selectDate);
        server = util.getServer();

        totalSold.setTextColor(Color.GREEN);
        totalPaid.setTextColor(Color.RED);

        seeSales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DailyUtilities.this, DailyReport.class);
                intent.putExtra("set",true);
                intent.putExtra("year",year);
                intent.putExtra("month",month);
                intent.putExtra("day",day);
                startActivity(intent);
            }
        });

        seeVisits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DailyUtilities.this, DayVisit.class);
                intent.putExtra("set",true);
                intent.putExtra("year",year);
                intent.putExtra("month",month);
                intent.putExtra("day",day);
                startActivity(intent);
            }
        });

        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent rx = getIntent();
        if(rx.getBooleanExtra("set",false))
        {
            day = rx.getIntExtra("day", Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            month = rx.getIntExtra("month",Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            year = rx.getIntExtra("year",Calendar.getInstance().get(Calendar.YEAR));
        }
        else
        {
            day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            month = Calendar.getInstance().get(Calendar.MONTH);
            year = Calendar.getInstance().get(Calendar.YEAR);
        }

        setSales(day,month,year);
    }

    private void showDatePickerDialog()
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                year,
                month,
                day);
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
    {
        this.day = dayOfMonth;
        this.year = year;
        this.month = month;
        setSales(dayOfMonth,month,year);
    }

    private void setSales(Integer day, Integer month, Integer year)
    {
        String dateS = getFormatted(day)+"/"+getFormatted(month+1)+"/"+year;
        String dateQ = year+"-"+(month+1)+"-"+day;
        showDate.setText(dateS);

        String url = server + "getDayStats.php?date="+dateQ;

        JsonObjectRequest request = new JsonObjectRequest(
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

        RequestQueue queue = Volley.newRequestQueue(DailyUtilities.this);
        queue.add(request);
    }

    private String getFormatted(Integer n)
    {
        return n<10 ? "0"+n : n.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}