package com.jby.stocktake.exportFeature.subcategory.subcategory;


public class SubCategoryObject {
    private String id;
    private String barcode;
    private String checkQuantity, systemQuantity;
    private String date;
    private String time;


    public SubCategoryObject(String id, String barcode, String checkQuantity, String systemQuantity, String date, String time) {
        this.id = id;
        this.barcode = barcode;
        this.checkQuantity = checkQuantity;
        this.systemQuantity = systemQuantity;
        this.date = date;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getCheckQuantity() {
        return checkQuantity;
    }

    public String getSystemQuantity() {
        return systemQuantity;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "{" +
                "id:'" + id + '\'' +
                ", barcode:'" + barcode + '\'' +
                ", checkQuantity:'" + checkQuantity + '\'' +
                ", systemQuantity:'" + systemQuantity + '\'' +
                ", date:'" + date + '\'' +
                ", time:'" + time + '\'' +
                "}";
    }
}

