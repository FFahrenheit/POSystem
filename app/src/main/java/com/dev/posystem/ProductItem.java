package com.dev.posystem;

public class ProductItem
{
    private String codeBar;
    private String name;
    private Double stock;
    private Double price;
    private String esp;

    public ProductItem()
    {
        codeBar = "0";
        name = "Sin nombre";
        stock = 0.0;
        price = 0.0;
        esp = "granel/pieza";
    }

    public String getGET()
    {
        return "?code="+codeBar+"&name="+name+"&stock="+stock+"&price="+price+"&esp="+esp;
    }

    public ProductItem(String codeBar, String name, Double stock, Double price, String esp) {
        this.codeBar = codeBar;
        this.name = name;
        this.stock = stock;
        this.price = price;
        this.esp = esp;
    }

    public String getCodeBar() {
        return codeBar;
    }

    public void setCodeBar(String code) {
        this.codeBar = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getStock() {
        return stock;
    }

    public void setStock(Double stock) {
        this.stock = stock;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getEsp() {
        return esp;
    }

    public void setEsp(String esp) {
        this.esp = (esp.equals("pieza"))? esp+"s" : esp;
    }
}
