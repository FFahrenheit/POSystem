package com.dev.posystem;

import org.json.JSONException;
import org.json.JSONObject;

public class VisitDayReport
{
    private String provider;
    private Integer products;
    private Double total;
    private String date;
    private Integer pk;

    public VisitDayReport(JSONObject object) throws JSONException {
        provider = object.getString("provider");
        products = object.getInt("count");
        total = object.getDouble("total");
        date = object.getString("fecha");
        pk = object.getInt("pk");
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Integer getProducts() {
        return products;
    }

    public void setProducts(Integer products) {
        this.products = products;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getPk() {
        return pk;
    }

    public void setPk(Integer pk) {
        this.pk = pk;
    }
}
