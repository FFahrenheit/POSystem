package com.dev.posystem;

import org.json.JSONException;
import org.json.JSONObject;

public class Sale
{
    private String time;
    private Integer pk;
    private Double total;
    private Double paid;
    private String cashier;
    private Integer productCount;

    public Sale(JSONObject sale) throws JSONException {
        productCount = sale.getInt("products");
        total = sale.getDouble("total");
        paid = sale.getDouble("paid");
        pk = sale.getInt("pk");
        time = sale.getString("date");
        cashier = sale.getString("cashier");
    }

    public String getCashier() {
        return cashier;
    }

    public void setCashier(String cashier) {
        this.cashier = cashier;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getPk() {
        return pk;
    }

    public void setPk(Integer pk) {
        this.pk = pk;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getPaid() {
        return paid;
    }

    public void setPaid(Double paid) {
        this.paid = paid;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }

    public String getDate()
    {
        return time.substring(0,time.indexOf(" "));
    }

    public String getHour()
    {
        return time.substring(time.indexOf(" "));
    }
}
