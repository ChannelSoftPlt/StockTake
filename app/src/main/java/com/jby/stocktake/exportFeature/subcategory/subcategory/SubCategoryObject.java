package com.jby.stocktake.exportFeature.subcategory.subcategory;


public class SubCategoryObject {
    private String id;
    private String barcode, itemCode, description;
    private String checkQuantity, systemQuantity;
    private String date;
    private String time;
    private String categoryName, sellingPrice, costPrice;


    public SubCategoryObject(String id, String barcode, String checkQuantity, String systemQuantity, String date, String time) {
        this.id = id;
        this.barcode = barcode;
        this.checkQuantity = checkQuantity;
        this.systemQuantity = systemQuantity;
        this.date = date;
        this.time = time;
    }

    public SubCategoryObject(String barcode, String itemCode, String description, String checkQuantity, String systemQuantity, String date, String time, String categoryName, String sellingPrice, String costPrice) {
        this.barcode = barcode;
        this.itemCode = itemCode;
        this.description = description;
        this.checkQuantity = checkQuantity;
        this.systemQuantity = systemQuantity;
        this.date = date;
        this.time = time;
        this.categoryName = categoryName;
        this.sellingPrice = sellingPrice;
        this.costPrice = costPrice;
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

    public String getItemCode() {
        return itemCode;
    }

    public String getDescription() {
        return description;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getSellingPrice() {
        return sellingPrice;
    }

    public String getCostPrice() {
        return costPrice;
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

