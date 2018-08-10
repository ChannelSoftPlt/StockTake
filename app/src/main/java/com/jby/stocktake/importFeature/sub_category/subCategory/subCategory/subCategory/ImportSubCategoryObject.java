package com.jby.stocktake.importFeature.sub_category.subCategory.subCategory.subCategory;


public class ImportSubCategoryObject {
    private String id;
    private String barcode;
    private String quantity;
    private String date;
    private String time;
    private String item_code;
    private String description;
    private String system_quantity;
    private String selling_price;
    private String cost_price;


    ImportSubCategoryObject(String id, String barcode, String quantity, String date, String time, String item_code, String description, String system_quantity, String selling_price, String cost_price) {
        this.id = id;
        this.barcode = barcode;
        this.quantity = quantity;
        this.date = date;
        this.time = time;
        this.item_code = item_code;
        this.description = description;
        this.system_quantity = system_quantity;
        this.selling_price = selling_price;
        this.cost_price = cost_price;
    }

    public String getId() {
        return id;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getItem_code() {
        return item_code;
    }

    public String getDescription() {
        return description;
    }

    public String getSystem_quantity() {
        return system_quantity;
    }

    public String getSelling_price() {
        return selling_price;
    }

    public String getCost_price() {
        return cost_price;
    }
}
