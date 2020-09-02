package com.dev.posystem;

import org.json.JSONException;
import org.json.JSONObject;

public class Provider
{
    private String name;
    private String number;
    private Integer products;
    private Integer pk;

    public Provider(JSONObject object) throws JSONException
    {
        this.name = object.getString("nombre");
        this.number = object.getString("contacto");
        this.products = object.getInt("total"); //No hay support
        this.pk = object.getInt("clave");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Integer getProducts() {
        return products;
    }

    public void setProducts(Integer products) {
        this.products = products;
    }

    public Integer getPk() {
        return pk;
    }

    public void setPk(Integer pk) {
        this.pk = pk;
    }
}
