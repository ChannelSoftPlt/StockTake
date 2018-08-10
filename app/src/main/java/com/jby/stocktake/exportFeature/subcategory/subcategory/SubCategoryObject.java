package com.jby.stocktake.exportFeature.subcategory.subcategory;


public class SubCategoryObject {
    private String id;
    private String barcode;
    private String quantity;
    private String date;
    private String time;
    private String no;

    public SubCategoryObject(String id, String barcode, String quantity, String date, String time) {
        this.id = id;
        this.barcode = barcode;
        this.quantity = quantity;
        this.date = date;
        this.time = time;
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

    String getNo() {
        return no;
    }
}
