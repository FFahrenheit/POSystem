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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MonthlyVisits extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private Button button;
    private Utilities util;
    private DatePickerDialog datePicker;
    private Integer month, year;
    private TextView date;
    private TextView empty;
    private TextView totalSold;
    private ListView visitList;
    private ArrayList<DailyVisit> visits;
    private DailyVisitAdapter adapter;
    private String server;
    private static DecimalFormat df2 = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_visits);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Visitas del mes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        button = findViewById(R.id.monthlyVisitsButton);
        util = new Utilities(getApplicationContext(),button);
        datePicker = createDialogWithoutDateField();
        date = findViewById(R.id.monthlyVisitsDate);
        visitList = findViewById(R.id.monthlyVisitsList);
        visits = new ArrayList<>();
        adapter = new DailyVisitAdapter(getApplicationContext(),visits);
        visitList.setAdapter(adapter);
        empty = findViewById(R.id.monthlyVisitsEmpty);
        visitList.setEmptyView(empty);
        server = util.getServer();
        totalSold = findViewById(R.id.monthlyVisitsTotal);


        button.setOnClickListener(new View.OnClickListener() {
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


        visitList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MonthlyVisits.this, DayVisit.class);
                intent.putExtra("set",true);
                String date[] = visits.get(i).getDayParts();
                intent.putExtra("year",Integer.parseInt(date[0]));
                intent.putExtra("month",Integer.parseInt(date[1])-1);
                intent.putExtra("day",Integer.parseInt(date[2]));
                startActivity(intent);
            }
        });

        setSales(Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.YEAR));

        FloatingActionButton goBack = findViewById(R.id.dailyVisitReturn);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
        String url = server + "getMonthlyVisits.php?year="+y+"&month="+(m+1);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Double total = 0.0;
                        visits.clear();
                        for(int i=0; i<response.length();i++)
                        {
                            try
                            {
                                JSONObject sale = response.getJSONObject(i);
                                DailyVisit saleC = new DailyVisit(sale);
                                total += saleC.getTotal();
                                visits.add(saleC);
                            } catch (JSONException e)
                            {
                                util.snack("Error al recibir la informacion");
                                e.printStackTrace();
                            }
                        }

                        totalSold.setText("$"+df2.format(total));

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