package com.jby.stocktake.exportFeature.category;

/**
 * Created by user on 11/5/2017.
 */

public class ExportCategoryListViewObject {
    private String id;
    private String category;
    private String subCategory_numb;

    public ExportCategoryListViewObject(String id, String category, String subCategory_numb) {
        this.id = id;
        this.category = category;
        this.subCategory_numb = subCategory_numb;
    }

    public ExportCategoryListViewObject(String id, String category) {
        this.id = id;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    String getSubCategory_numb() {
        return subCategory_numb;
    }

    @Override
    public String toString() {
        return "{" +
                "id:'" + id + '\'' +
                ", category:'" + category + '\'' +
                ", subCategory_numb:'" + subCategory_numb + '\'' +
                '}';
    }
}
