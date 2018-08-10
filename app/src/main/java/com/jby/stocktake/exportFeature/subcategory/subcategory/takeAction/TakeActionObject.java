package com.jby.stocktake.exportFeature.subcategory.subcategory.takeAction;


public class TakeActionObject {
    private String id;
    private String categoryID;
    private String categoryName;
    private String barcode;
    private String quantity;
    private String date;
    private String time;

   public TakeActionObject(String id, String barcode, String quantity, String date, String time, String categoryName, String categoryID) {
        this.id = id;
        this.barcode = barcode;
        this.quantity = quantity;
        this.date = date;
        this.time = time;
        this.categoryName = categoryName;
        this.categoryID = categoryID;
    }

     String getId() {
        return id;
    }

     String getBarcode() {
        return barcode;
    }

     String getQuantity() {
        return quantity;
    }

     String getDate() {
        return date;
    }

     String getTime() {
        return time;
    }

    String getCategoryID() {
        return categoryID;
    }

    String getCategoryName() {
        return categoryName;
    }

}
