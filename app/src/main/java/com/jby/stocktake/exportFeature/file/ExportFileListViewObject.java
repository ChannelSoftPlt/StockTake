package com.jby.stocktake.exportFeature.file;

/**
 * Created by user on 11/5/2017.
 */

public class ExportFileListViewObject {
    private String id;
    private String file;
    private String numCategory;

    public ExportFileListViewObject(String id, String file, String numCategory) {
        this.id = id;
        this.file = file;
        this.numCategory = numCategory;
    }

    public String getId() {
        return id;
    }

    public String getFile() {
        return file;
    }

    String getCategory_numb() {
        return numCategory;
    }
}
