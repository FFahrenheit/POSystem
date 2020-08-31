package com.dev.posystem;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MonthlyReport extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private Button button;
    private Utilities util;
    private DatePickerDialog datePicker;
    private Integer month, year;
    private TextView date;
    private TextView empty;
    private TextView totalSold;
    private ListView salesList;
    private ArrayList<DailySale> sales;
    private DailySaleAdapter adapter;
    private String server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button = findViewById(R.id.monthlyReportButton);
        util = new Utilities(getApplicationContext(),button);
        datePicker = createDialogWithoutDateField();
        date = findViewById(R.id.monthlyReportDate);
        salesList = findViewById(R.id.monthlyReportList);
        sales = new ArrayList<>();
        adapter = new DailySaleAdapter(getApplicationContext(),sales);
        salesList.setAdapter(adapter);
        empty = findViewById(R.id.monthlyReportEmpty);
        salesList.setEmptyView(empty);
        server = util.getServer();
        totalSold = findViewById(R.id.monthlyReportTotal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //datePicker.show();   Alernativa 1
                // /*
                MonthYearPickerDialog pd = new MonthYearPickerDialog();
                //pd.setDates(month,year);
                pd.setListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        year =  i;
                        month = i1-1;
                        setSales(month,year);
                    }
                });
                pd.show(getSupportFragmentManager(), "MonthYearPickerDialog");
                // */ Alternativa 2
            }
        });


        FloatingActionButton fab = findViewById(R.id.dailySaleReturn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        salesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MonthlyReport.this, DailyReport.class);
                intent.putExtra("set",true);
                String date[] = sales.get(i).getDayParts();
                intent.putExtra("year",Integer.parseInt(date[0]));
                intent.putExtra("month",Integer.parseInt(date[1])-1);
                intent.putExtra("day",Integer.parseInt(date[2]));
                startActivity(intent);
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
        String url = server + "getMonthlySales.php?year="+y+"&month="+(m+1);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Double total = 0.0;
                        sales.clear();
                        for(int i=0; i<response.length();i++)
                        {
                            try
                            {
                                JSONObject sale = response.getJSONObject(i);
                                DailySale saleC = new DailySale(sale);
                                total += saleC.getTotal();
                                sales.add(saleC);
                            } catch (JSONException e)
                            {
                                util.snack("Error al recibir la informacion");
                                e.printStackTrace();
                            }
                        }
                        totalSold.setText("$"+total);

                        if(total>0)
                        {
                            totalSold.setTextColor(Color.GREEN);
                        }
                        else
                        {
                            totalSold.setTextColor(Color.RED);
                        }
                        adapter.notifyDataSetChanged();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        util.snack("No se pudieron recibir las ventas");
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