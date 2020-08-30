package com.dev.posystem;

import org.json.JSONException;
import org.json.JSONObject;

public class Product
{
    private String name;
    private Double quantity;
    private Double price;
    private String codeBar;
    private Double total;
    private Integer primaryKey;
    private String esp="pieza";

    public Product(JSONObject product) throws JSONException
    {
        this.setName(product.getString("name"));
        this.setEsp(product.getString("esp"));
        this.setPrimaryKey(product.getInt("pk"));
        this.setQuantity(product.getDouble("qty"));
        this.setCodeBar(product.getString("code"));
        this.setPrice(product.getDouble("price"));
        this.setTotal(this.getPrice() * this.getQuantity());
    }

    public Product()
    {
        name = "Not set";
        quantity = 0.0;
        price = 0.0;
        codeBar = "";
        total = 0.0;
        primaryKey = 0;
        esp = "pieza";
    }

    public String getEsp() {
        return esp;
    }

    public void setEsp(String esp) {
        this.esp = esp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCodeBar() {
        return codeBar;
    }

    public void setCodeBar(String codeBar) {
        this.codeBar = codeBar;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Integer getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Integer primaryKey) {
        this.primaryKey = primaryKey;
    }
}
