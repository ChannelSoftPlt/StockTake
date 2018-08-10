package com.jby.stocktake.exportFeature.file;

/**
 * Created by user on 11/5/2017.
 */

public class ExportFileListViewObject {
    private String id;
    private String file;
    private String category_numb;

    public ExportFileListViewObject(String id, String file, String category_numb) {
        this.id = id;
        this.file = file;
        this.category_numb = category_numb;
    }

    public String getId() {
        return id;
    }

    public String getFile() {
        return file;
    }

    String getCategory_numb() {
        return category_numb;
    }
}
