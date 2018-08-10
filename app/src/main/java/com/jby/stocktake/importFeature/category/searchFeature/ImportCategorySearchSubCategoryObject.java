package com.jby.stocktake.importFeature.category.searchFeature;

/**
 * Created by user on 11/5/2017.
 */

public class ImportCategorySearchSubCategoryObject {
    private String barcode;
    private String quantity;
    private String category_Name;
    private String category_Id;
    private String date;

    ImportCategorySearchSubCategoryObject(String barcode, String quantity, String category_Name, String category_Id, String date) {
        this.barcode = barcode;
        this.quantity = quantity;
        this.category_Name = category_Name;
        this.category_Id = category_Id;

        this.date = date;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getCategory_Name() {
        return category_Name;
    }

    public String getCategory_Id() {
        return category_Id;
    }

    public String getDate(){
        return date;
    }
}
