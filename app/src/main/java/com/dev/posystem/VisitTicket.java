package com.dev.posystem;

import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class VisitTicket
{
    private Integer id;
    private String productName;
    private String productCode;
    private Double price;
    private Double qty;
    private Double total;

    public VisitTicket(JSONObject object) throws JSONException
    {
        id = object.getInt("clave");
        productName = object.getString("name");
        productCode = object.getString("producto");
        price = object.getDouble("precio");
        qty = object.getDouble("cantidad");
        total = object.getDouble("total");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

}
