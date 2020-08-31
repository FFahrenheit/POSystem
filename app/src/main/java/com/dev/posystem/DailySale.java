package com.dev.posystem;

import org.json.JSONException;
import org.json.JSONObject;

public class DailySale
{
    private String day;
    private Integer saleCount;
    private Double total;

    public DailySale(JSONObject object) throws JSONException {
        this.day = object.getString("fecha");
        this.saleCount = object.getInt("count");
        this.total = object.getDouble("total");
    }

    public String[] getDayParts()
    {
        return day.split("-");
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Integer getSaleCount() {
        return saleCount;
    }

    public void setSaleCount(Integer saleCount) {
        this.saleCount = saleCount;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}
