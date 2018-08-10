package com.jby.stocktake.exportFeature.category.searchFeature;

/**
 * Created by user on 11/5/2017.
 */

public class ExportCategorySearchCategoryObject {
    private String id;
    private String category;
    private String subCategory_numb;

    public ExportCategorySearchCategoryObject(String id, String category, String subCategory_numb) {
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
