package com.dev.posystem;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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

import java.util.ArrayList;
import java.util.Calendar;

public class DayVisit extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private Button button;
    private TextView date;
    private ListView visitList;
    private TextView empty;
    private TextView total;
    private Utilities util;
    private String server;
    private ArrayList<VisitDayReport> visits;
    private VisitDayReportAdapter adapter;
    private Integer day,month,year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_visit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        button = findViewById(R.id.dayVisitSelectDate);
        date = findViewById(R.id.dayVisitDate);
        visitList = findViewById(R.id.dayVisitList);
        empty = findViewById(R.id.dayVisitEmpty);
        total = findViewById(R.id.dayVisitTotal);

        util = new Utilities(getApplicationContext(),empty);
        visitList.setEmptyView(empty);
        server = util.getServer();

        visits = new ArrayList<>();
        adapter = new VisitDayReportAdapter(getApplicationContext(),visits);

        visitList.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
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

        FloatingActionButton goBack = findViewById(R.id.dayVisitGoBack);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
        visits.clear();
        String dateS = getFormatted(day)+"/"+getFormatted(month+1)+"/"+year;
        String dateQ = year+"-"+(month+1)+"-"+day;
        date.setText(dateS);

        String url = server + "getDayVisits.php?date="+dateQ;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try
                        {
                            visits.clear();
                            for(int i=0; i<response.length();i++)
                            {
                                JSONObject sale = response.getJSONObject(i);
                                VisitDayReport saleC = new VisitDayReport(sale);
                                visits.add(saleC);
                            }
                            total.setText("$"+adapter.getTotal());
                            if(adapter.getTotal()>0)
                            {
                                total.setTextColor(Color.GREEN);
                            }
                            else
                            {
                                total.setTextColor(Color.RED);
                            }
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
                        util.snack("No se pudieron cargar las visitas");
                    }
                }
        );
        RequestQueue queue = Volley.newRequestQueue(DayVisit.this);
        queue.add(request);

        adapter.notifyDataSetChanged();

        visitList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View itemView, final int itemPosition, long itemId)
            {
                Intent intent = new Intent(DayVisit.this, PayVisit.class);
                intent.putExtra("paid",true);
                intent.putExtra("key",visits.get(itemPosition).getPk());
                intent.putExtra("name", visits.get(itemPosition).getProvider());
                startActivity(intent);
            }
        });
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