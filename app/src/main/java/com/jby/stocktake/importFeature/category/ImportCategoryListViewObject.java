package com.jby.stocktake.importFeature.category;

/**
 * Created by user on 11/5/2017.
 */

public class ImportCategoryListViewObject {
    private String id;
    private String category;
    private String subCategory_numb;

    public ImportCategoryListViewObject(String id, String category, String subCategory_numb) {
        this.id = id;
        this.category = category;
        this.subCategory_numb = subCategory_numb;
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
}
