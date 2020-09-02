package com.dev.posystem;

import org.json.JSONException;
import org.json.JSONObject;

public class Provision
{
    private String productCode;
    private String name;
    private Double price;
    private Double productPrice;
    private Integer provider;

    public Provision(JSONObject object) throws JSONException {
        this.productCode = object.getString("code");
        this.name = object.getString("name");
        this.price = object.getDouble("price");
        this.productPrice = object.getDouble("original");
        this.provider = object.getInt("provider");
    }

    public Double getUtility()
    {
        return (productPrice/price - 1)*100;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }

    public Integer getProvider() {
        return provider;
    }

    public void setProvider(Integer provider) {
        this.provider = provider;
    }
}
