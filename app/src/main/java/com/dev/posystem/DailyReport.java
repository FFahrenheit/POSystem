package com.dev.posystem;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

import java.util.ArrayList;
import java.util.Calendar;

public class DailyReport extends AppCompatActivity implements  DatePickerDialog.OnDateSetListener
{
    private Button button;
    private TextView date;
    private ListView salesList;
    private TextView empty;
    private TextView total;
    private Utilities util;
    private String server;
    private ArrayList<Sale> sales;
    private SaleAdapter adapter;
    private Integer day,month,year;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        button = findViewById(R.id.dailyReportButton);
        date = findViewById(R.id.dailyReportDate);
        salesList = findViewById(R.id.dailyReportList);
        empty = findViewById(R.id.dailyReportEmpty);
        total = findViewById(R.id.dailyReportTotal);

        util = new Utilities(getApplicationContext(),empty);
        salesList.setEmptyView(empty);
        server = util.getServer();

        sales = new ArrayList<>();
        adapter = new SaleAdapter();
        adapter.context = getApplicationContext();
        adapter.sales = sales;

        salesList.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        FloatingActionButton fab = findViewById(R.id.dailyReportReturn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        month = Calendar.getInstance().get(Calendar.MONTH);
        year = Calendar.getInstance().get(Calendar.YEAR);

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
        sales.clear();
        String dateS = getFormated(day)+"/"+getFormated(month+1)+"/"+year;
        String dateQ = year+"-"+(month+1)+"-"+day;
        date.setText(dateS);

        String url = server + "getDaySales.php?date="+dateQ;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try
                        {
                            sales.clear();
                            for(int i=0; i<response.length();i++)
                            {
                                JSONObject sale = response.getJSONObject(i);
                                Sale saleC = new Sale(sale);
                                sales.add(saleC);
                            }
                            total.setText("$"+adapter.getTotal());
                            adapter.notifyDataSetChanged();
                        }
                        catch(JSONException e)
                        {
                            util.snack("Error al recibir los datos");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        util.snack("No se pudieron cargar las ventas");
                    }
                }
        );
        RequestQueue queue = Volley.newRequestQueue(DailyReport.this);
        queue.add(request);

        adapter.notifyDataSetChanged();
    }

    private String getFormated(Integer n)
    {
        return n<10 ? "0"+n : n.toString();
    }
}