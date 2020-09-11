package com.dev.posystem;

import org.json.JSONException;
import org.json.JSONObject;

public class DailyVisit
{
    private String day;
    private Integer visitCount;
    private Double total;

    public DailyVisit(JSONObject object) throws JSONException
    {
        this.day = object.getString("fecha");
        this.visitCount = object.getInt("count");
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

    public Integer getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Integer visitCount) {
        this.visitCount = visitCount;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}
